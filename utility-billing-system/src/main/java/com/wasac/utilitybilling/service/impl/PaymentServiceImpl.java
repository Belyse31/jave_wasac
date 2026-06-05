package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.BillingDtos;
import com.wasac.utilitybilling.entity.Bill;
import com.wasac.utilitybilling.entity.Payment;
import com.wasac.utilitybilling.entity.enums.BillStatus;
import com.wasac.utilitybilling.entity.enums.MeterStatus;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.PaymentRepository;
import com.wasac.utilitybilling.service.AuditService;
import com.wasac.utilitybilling.service.EmailService;
import com.wasac.utilitybilling.service.PaymentService;
import com.wasac.utilitybilling.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Override
    @Transactional
    public BillingDtos.PaymentResponse record(BillingDtos.PaymentRequest request) {
        // Payment must be linked to an existing bill.
        Bill bill = billRepository.findById(request.billId()).orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        // A fully paid bill cannot receive another payment.
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessRuleException("Bill is already paid");
        }
        // Future payment dates are not allowed because money has not been received yet.
        if (request.paymentDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Future payment dates are not allowed");
        }
        // Overpayment is rejected instead of creating credit balance.
        if (request.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessRuleException("Payment amount cannot exceed outstanding balance");
        }
        // Create payment record after all validations pass.
        Payment payment = new Payment();
        // Generate unique payment reference.
        payment.setPaymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        // Link payment to its bill.
        payment.setBill(bill);
        // Store validated positive payment amount.
        payment.setAmountPaid(request.amountPaid());
        // Store validated payment method: MOMO, BANK, CARD, or CASH.
        payment.setPaymentMethod(request.paymentMethod());
        // Store validated non-future payment date.
        payment.setPaymentDate(request.paymentDate());
        // Reduce outstanding balance by the amount paid.
        bill.setOutstandingBalance(bill.getOutstandingBalance().subtract(request.amountPaid()));
        // Mark bill paid when balance is zero, otherwise partially paid.
        bill.setStatus(bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0 ? BillStatus.PAID : BillStatus.PARTIALLY_PAID);
        // Reconnect disconnected meter when the related bill becomes fully paid.
        if (bill.getStatus() == BillStatus.PAID && bill.getMeter().getStatus() == MeterStatus.DISCONNECTED) {
            bill.getMeter().setStatus(MeterStatus.ACTIVE);
        }
        // Save payment after bill balance/status have been updated in the transaction.
        Payment saved = paymentRepository.save(payment);
        // Notify customer about successful payment.
        emailService.sendPaymentConfirmationEmail(bill.getCustomer().getEmail(), saved.getPaymentReference(), saved.getAmountPaid().toPlainString());
        // Audit payment for finance traceability.
        auditService.record("system", "PAYMENT_PROCESSING", null, "Recorded payment " + saved.getPaymentReference());
        // Return payment DTO including updated bill status and balance.
        return response(saved);
    }

    @Override
    public Page<BillingDtos.PaymentResponse> search(String query, int page, int size, String sortField, String sortDirection) {
        String q = query == null ? "" : query;
        return paymentRepository.findByPaymentReferenceContainingIgnoreCaseOrBill_BillReferenceContainingIgnoreCase(q, q, PageUtils.pageable(page, size, sortField, sortDirection)).map(this::response);
    }

    private BillingDtos.PaymentResponse response(Payment p) {
        return new BillingDtos.PaymentResponse(p.getId(), p.getPaymentReference(), p.getBill().getBillReference(), p.getAmountPaid(), p.getPaymentMethod(), p.getPaymentDate(),
                p.getBill().getOutstandingBalance(), p.getBill().getStatus());
    }
}
