package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.MeterReadingDtos;
import com.wasac.utilitybilling.entity.Meter;
import com.wasac.utilitybilling.entity.MeterReading;
import com.wasac.utilitybilling.entity.enums.MeterStatus;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.DuplicateResourceException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.MeterReadingRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.service.MeterReadingService;
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
public class MeterReadingServiceImpl implements MeterReadingService {
    private final MeterReadingRepository readingRepository;
    private final MeterRepository meterRepository;

    @Override
    @Transactional
    public MeterReadingDtos.MeterReadingResponse create(MeterReadingDtos.MeterReadingRequest request) {
        // Validate meter existence, active status, date, reading order, and monthly uniqueness.
        Meter meter = validate(request, null);
        // Create a new reading only after all business rules pass.
        MeterReading reading = new MeterReading();
        // Copy request fields and derive billing month/year.
        apply(reading, request, meter);
        // Save reading and return API-safe DTO.
        return toResponse(readingRepository.save(reading));
    }

    @Override
    @Transactional
    public MeterReadingDtos.MeterReadingResponse update(UUID id, MeterReadingDtos.MeterReadingRequest request) {
        MeterReading reading = entity(id);
        Meter meter = validate(request, reading);
        apply(reading, request, meter);
        return toResponse(reading);
    }

    @Override
    public MeterReadingDtos.MeterReadingResponse get(UUID id) {
        return toResponse(entity(id));
    }

    @Override
    public Page<MeterReadingDtos.MeterReadingResponse> list(int page, int size, String sortField, String sortDirection) {
        return readingRepository.findAll(PageUtils.pageable(page, size, sortField, sortDirection)).map(this::toResponse);
    }

    @Override
    public void delete(UUID id) {
        readingRepository.delete(entity(id));
    }

    private MeterReading entity(UUID id) {
        return readingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));
    }

    private Meter validate(MeterReadingDtos.MeterReadingRequest request, MeterReading current) {
        // Meter must exist before a reading can be recorded.
        Meter meter = meterRepository.findById(request.meterId()).orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
        // Only active meters can receive readings for billing.
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Meter must be active to capture readings");
        }
        // Future readings are rejected because consumption cannot be known in advance.
        if (request.readingDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Future meter reading dates are not allowed");
        }
        // Current reading must be greater than previous reading to avoid negative consumption.
        if (request.currentReading().compareTo(request.previousReading()) <= 0) {
            throw new BusinessRuleException("Current reading must be greater than previous reading");
        }
        // Billing period is derived from the reading date.
        int year = request.readingDate().getYear();
        // Month is stored separately to support unique meter/month/year checks.
        int month = request.readingDate().getMonthValue();
        // During update, uniqueness is checked only if meter or billing period changed.
        boolean changedMonth = current == null || !current.getMeter().getId().equals(request.meterId()) || current.getBillingYear() != year || current.getBillingMonth() != month;
        // Prevent duplicate readings for the same meter in the same month/year.
        if (changedMonth && readingRepository.existsByMeterAndBillingYearAndBillingMonth(meter, year, month)) {
            throw new DuplicateResourceException("Only one reading per meter per month is allowed");
        }
        // Return the validated meter so the caller can attach it to the reading.
        return meter;
    }

    private void apply(MeterReading reading, MeterReadingDtos.MeterReadingRequest request, Meter meter) {
        // Link reading to the validated active meter.
        reading.setMeter(meter);
        // Store previous reading used for consumption calculation.
        reading.setPreviousReading(request.previousReading());
        // Store current reading used for consumption calculation.
        reading.setCurrentReading(request.currentReading());
        // Store the actual date the reading was captured.
        reading.setReadingDate(request.readingDate());
        // Store billing year for querying and uniqueness constraint.
        reading.setBillingYear(request.readingDate().getYear());
        // Store billing month for querying and uniqueness constraint.
        reading.setBillingMonth(request.readingDate().getMonthValue());
    }

    private MeterReadingDtos.MeterReadingResponse toResponse(MeterReading r) {
        BigDecimal consumption = r.getCurrentReading().subtract(r.getPreviousReading());
        return new MeterReadingDtos.MeterReadingResponse(r.getId(), r.getMeter().getId(), r.getMeter().getMeterNumber(), r.getPreviousReading(), r.getCurrentReading(), consumption, r.getReadingDate(), r.getBillingYear(), r.getBillingMonth());
    }
}
