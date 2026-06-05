package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.BillingDtos;
import com.wasac.utilitybilling.entity.Bill;
import com.wasac.utilitybilling.entity.MeterReading;
import com.wasac.utilitybilling.entity.Penalty;
import com.wasac.utilitybilling.entity.Tariff;
import com.wasac.utilitybilling.entity.Tax;
import com.wasac.utilitybilling.entity.enums.BillStatus;
import com.wasac.utilitybilling.entity.enums.CustomerStatus;
import com.wasac.utilitybilling.entity.enums.PenaltyType;
import com.wasac.utilitybilling.entity.enums.TariffModel;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.DuplicateResourceException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.repository.MeterReadingRepository;
import com.wasac.utilitybilling.repository.PenaltyRepository;
import com.wasac.utilitybilling.repository.TariffRepository;
import com.wasac.utilitybilling.repository.TaxRepository;
import com.wasac.utilitybilling.service.AuditService;
import com.wasac.utilitybilling.service.BillService;
import com.wasac.utilitybilling.service.EmailService;
import com.wasac.utilitybilling.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final MeterReadingRepository readingRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Override
    @Transactional
    public BillingDtos.BillResponse generate(BillingDtos.BillGenerateRequest request) {
        // A bill can only be generated from an existing meter reading.
        MeterReading reading = readingRepository.findById(request.readingId()).orElseThrow(() -> {
            if (meterRepository.existsById(request.readingId())) {
                return new BusinessRuleException("You entered a meter ID. Bill generation requires the meter reading ID returned by POST /api/meter-readings.");
            }
            return new ResourceNotFoundException("Meter reading not found. First create a reading using POST /api/meter-readings, then use the returned reading id.");
        });
        // Inactive or suspended customers cannot receive new bills.
        if (reading.getMeter().getCustomer().getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive customers cannot receive bills");
        }
        // Only one bill per meter per billing month/year is allowed.
        if (billRepository.existsByMeterAndBillingYearAndBillingMonth(reading.getMeter(), reading.getBillingYear(), reading.getBillingMonth())) {
            throw new DuplicateResourceException("A bill already exists for this meter and billing month");
        }
        // Build the bill using current tariff/tax/penalty rules.
        Bill bill = buildBill(reading, request.dueDate());
        // Save the generated bill.
        Bill saved = billRepository.save(bill);
        // Notify the customer that the bill has been processed.
        emailService.sendBillNotificationEmail(saved.getCustomer().getEmail(), monthYear(saved.getBillingMonth(), saved.getBillingYear()), saved.getTotalAmount().toPlainString());
        // Audit bill generation for reporting and exam traceability.
        auditService.record("system", "BILL_GENERATION", null, "Generated bill " + saved.getBillReference());
        // Return bill DTO to avoid exposing the entity.
        return response(saved);
    }

    @Override
    @Transactional
    public int generateMonthly(BillingDtos.MonthlyBillingRequest request) {
        int count = 0;
        for (MeterReading reading : readingRepository.findByBillingYearAndBillingMonth(request.year(), request.month())) {
            if (!billRepository.existsByMeterAndBillingYearAndBillingMonth(reading.getMeter(), request.year(), request.month())) {
                billRepository.save(buildBill(reading, request.dueDate()));
                count++;
            }
        }
        auditService.record("system", "MONTHLY_BILLING", null, "Generated " + count + " monthly bills for " + request.month() + "/" + request.year());
        return count;
    }

    @Override
    @Transactional
    public BillingDtos.BillResponse approve(UUID id) {
        // Bill must exist before finance can approve it.
        Bill bill = entity(id);
        // Approval cannot be repeated.
        if (bill.isApproved()) {
            throw new BusinessRuleException("Bill is already approved");
        }
        // Mark the bill as approved by finance.
        bill.setApproved(true);
        // Email the customer the final amount payable after approval.
        emailService.sendApprovedBillEmail(bill.getCustomer().getEmail(), bill.getCustomer().getFullName(), bill.getBillReference(),
                monthYear(bill.getBillingMonth(), bill.getBillingYear()), bill.getOutstandingBalance().toPlainString(), bill.getDueDate().toString());
        // Audit approval action.
        auditService.record("system", "BILL_APPROVAL", null, "Approved bill " + bill.getBillReference());
        // Return updated bill DTO.
        return response(bill);
    }

    @Override
    public BillingDtos.BillResponse get(UUID id) {
        return response(entity(id));
    }

    @Override
    public Page<BillingDtos.BillResponse> search(String query, int page, int size, String sortField, String sortDirection) {
        String q = query == null ? "" : query;
        return billRepository.findByBillReferenceContainingIgnoreCase(q, PageUtils.pageable(page, size, sortField, sortDirection)).map(this::response);
    }

    private Bill entity(UUID id) {
        return billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    private Bill buildBill(MeterReading reading, LocalDate dueDate) {
        // Billing date is the first day of the meter reading month.
        LocalDate billingDate = LocalDate.of(reading.getBillingYear(), reading.getBillingMonth(), 1);
        // Tariff is chosen by billing period, so old months keep old tariff versions.
        Tariff tariff = tariffRepository.findEffectiveTariff(reading.getMeter().getMeterType(), billingDate)
                .orElseThrow(() -> new BusinessRuleException("No active tariff configured for meter type " + reading.getMeter().getMeterType()));
        // Consumption is current reading minus previous reading.
        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());
        // Consumption must be positive before generating a bill.
        if (consumption.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Consumption must be greater than zero before bill generation");
        }
        // Charge is calculated with either flat or tiered tariff logic.
        BigDecimal consumptionCharge = tariff.getModel() == TariffModel.FLAT ? consumption.multiply(tariff.getRate()) : tieredCharge(tariff, consumption);
        // Taxable amount includes consumption charge and fixed service charge.
        BigDecimal taxable = consumptionCharge.add(tariff.getFixedCharge());
        // Start tax total at zero.
        BigDecimal taxAmount = BigDecimal.ZERO;
        // Add every active tax effective for the billing date.
        for (Tax tax : taxRepository.findEffectiveTaxes(billingDate)) {
            taxAmount = taxAmount.add(taxable.multiply(tax.getRate()));
        }
        // Penalty starts at zero unless the bill is already overdue.
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        // Apply configured penalties only if due date is already in the past.
        if (dueDate.isBefore(LocalDate.now())) {
            for (Penalty penalty : penaltyRepository.findEffectivePenalties(billingDate)) {
                penaltyAmount = penaltyAmount.add(penalty.getType() == PenaltyType.FIXED ? penalty.getValue() : taxable.multiply(penalty.getValue()));
            }
        }
        // Total amount is all charges rounded to two decimal places.
        BigDecimal total = taxable.add(taxAmount).add(penaltyAmount).setScale(2, RoundingMode.HALF_UP);
        // Negative bills are invalid and should never be persisted.
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Bill amount cannot be negative");
        }
        // Create bill entity after calculations pass validation.
        Bill bill = new Bill();
        // Generate readable unique bill reference.
        bill.setBillReference("BILL-" + reading.getBillingYear() + String.format("%02d", reading.getBillingMonth()) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Link bill to customer for search and notifications.
        bill.setCustomer(reading.getMeter().getCustomer());
        // Link bill to meter for duplicate prevention and reporting.
        bill.setMeter(reading.getMeter());
        // Link bill to the source meter reading.
        bill.setReading(reading);
        bill.setBillingYear(reading.getBillingYear());
        bill.setBillingMonth(reading.getBillingMonth());
        bill.setConsumption(consumption);
        bill.setConsumptionCharge(consumptionCharge.setScale(2, RoundingMode.HALF_UP));
        bill.setFixedCharge(tariff.getFixedCharge());
        bill.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setPenaltyAmount(penaltyAmount.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalAmount(total);
        bill.setOutstandingBalance(total);
        bill.setDueDate(dueDate);
        bill.setStatus(dueDate.isBefore(LocalDate.now()) ? BillStatus.OVERDUE : BillStatus.PENDING);
        return bill;
    }

    private BigDecimal tieredCharge(Tariff tariff, BigDecimal consumption) {
        BigDecimal total = BigDecimal.ZERO;
        for (var tier : tariff.getTiers()) {
            BigDecimal upper = tier.getToUnits() == null ? consumption : tier.getToUnits().min(consumption);
            BigDecimal units = upper.subtract(tier.getFromUnits()).max(BigDecimal.ZERO);
            total = total.add(units.multiply(tier.getRate()));
        }
        return total;
    }

    private String monthYear(int month, int year) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + year;
    }

    private BillingDtos.BillResponse response(Bill b) {
        return new BillingDtos.BillResponse(b.getId(), b.getBillReference(), b.getCustomer().getId(), b.getCustomer().getFullName(), b.getMeter().getId(),
                b.getMeter().getMeterNumber(), b.getMeter().getMeterType(), b.getBillingYear(), b.getBillingMonth(), b.getConsumption(),
                b.getConsumptionCharge(), b.getFixedCharge(), b.getTaxAmount(), b.getPenaltyAmount(), b.getTotalAmount(), b.getOutstandingBalance(),
                b.getDueDate(), b.getStatus(), b.isApproved());
    }
}
