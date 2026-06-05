package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.MeterDtos;
import com.wasac.utilitybilling.entity.Customer;
import com.wasac.utilitybilling.entity.Meter;
import com.wasac.utilitybilling.entity.enums.CustomerStatus;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.DuplicateResourceException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.service.MeterService;
import com.wasac.utilitybilling.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {
    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public MeterDtos.MeterResponse create(MeterDtos.MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new DuplicateResourceException("Meter number is already registered");
        }
        Meter meter = new Meter();
        apply(meter, request);
        return toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterDtos.MeterResponse update(UUID id, MeterDtos.MeterRequest request) {
        Meter meter = entity(id);
        if (!meter.getMeterNumber().equals(request.meterNumber()) && meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new DuplicateResourceException("Meter number is already registered");
        }
        apply(meter, request);
        return toResponse(meter);
    }

    @Override
    public MeterDtos.MeterResponse get(UUID id) {
        return toResponse(entity(id));
    }

    @Override
    public Page<MeterDtos.MeterResponse> search(String query, int page, int size, String sortField, String sortDirection) {
        String q = query == null ? "" : query;
        return meterRepository.findByMeterNumberContainingIgnoreCase(q, PageUtils.pageable(page, size, sortField, sortDirection)).map(this::toResponse);
    }

    @Override
    public void delete(UUID id) {
        meterRepository.delete(entity(id));
    }

    private Meter entity(UUID id) {
        return meterRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
    }

    private void apply(Meter meter, MeterDtos.MeterRequest request) {
        Customer customer = customerRepository.findById(request.customerId()).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive customers cannot receive active meters");
        }
        meter.setMeterNumber(request.meterNumber());
        meter.setMeterType(request.meterType());
        meter.setInstallationDate(request.installationDate());
        meter.setStatus(request.status());
        meter.setCustomer(customer);
    }

    private MeterDtos.MeterResponse toResponse(Meter m) {
        return new MeterDtos.MeterResponse(m.getId(), m.getMeterNumber(), m.getMeterType(), m.getInstallationDate(), m.getStatus(), m.getCustomer().getId(), m.getCustomer().getFullName());
    }
}
