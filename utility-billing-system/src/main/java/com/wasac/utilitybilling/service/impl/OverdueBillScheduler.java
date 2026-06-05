package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.entity.Bill;
import com.wasac.utilitybilling.entity.Penalty;
import com.wasac.utilitybilling.entity.enums.BillStatus;
import com.wasac.utilitybilling.entity.enums.MeterStatus;
import com.wasac.utilitybilling.entity.enums.PenaltyType;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.PenaltyRepository;
import com.wasac.utilitybilling.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class OverdueBillScheduler {
    private final BillRepository billRepository;
    private final PenaltyRepository penaltyRepository;
    private final AuditService auditService;

    @Scheduled(cron = "0 15 1 * * *")
    @Transactional
    public void applyOverdueRules() {
        LocalDate today = LocalDate.now();
        var unpaidStatuses = EnumSet.of(BillStatus.PENDING, BillStatus.PARTIALLY_PAID, BillStatus.OVERDUE);
        for (Bill bill : billRepository.findByStatusInAndDueDateBefore(unpaidStatuses, today.minusDays(30))) {
            markOverdueAndApplyPenalty(bill, today);
            disconnectLongOverdueMeter(bill, today);
        }
    }

    private void markOverdueAndApplyPenalty(Bill bill, LocalDate today) {
        bill.setStatus(BillStatus.OVERDUE);
        BigDecimal penalty = BigDecimal.ZERO;
        for (Penalty configured : penaltyRepository.findByActiveIsTrueAndEffectiveFromLessThanEqualAndEffectiveToIsNull(today)) {
            penalty = penalty.add(configured.getType() == PenaltyType.FIXED
                    ? configured.getValue()
                    : bill.getOutstandingBalance().multiply(configured.getValue()));
        }
        if (penalty.compareTo(BigDecimal.ZERO) > 0) {
            if (bill.getLatePenaltyAppliedAt() != null) {
                return;
            }
            BigDecimal roundedPenalty = penalty.setScale(2, RoundingMode.HALF_UP);
            bill.setPenaltyAmount(bill.getPenaltyAmount().add(roundedPenalty));
            bill.setTotalAmount(bill.getTotalAmount().add(roundedPenalty));
            bill.setOutstandingBalance(bill.getOutstandingBalance().add(roundedPenalty));
            bill.setLatePenaltyAppliedAt(Instant.now());
            auditService.record("system", "LATE_PAYMENT_PENALTY", null, "Applied penalty to bill " + bill.getBillReference());
        }
    }

    private void disconnectLongOverdueMeter(Bill bill, LocalDate today) {
        if (bill.getDueDate().isBefore(today.minusDays(60)) && bill.getMeter().getStatus() == MeterStatus.ACTIVE) {
            bill.getMeter().setStatus(MeterStatus.DISCONNECTED);
            auditService.record("system", "METER_DISCONNECTED", null, "Disconnected meter " + bill.getMeter().getMeterNumber() + " for overdue bill " + bill.getBillReference());
        }
    }
}
