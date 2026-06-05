package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.ConfigDtos;
import com.wasac.utilitybilling.entity.Penalty;
import com.wasac.utilitybilling.entity.Tariff;
import com.wasac.utilitybilling.entity.TariffTier;
import com.wasac.utilitybilling.entity.Tax;
import com.wasac.utilitybilling.entity.enums.TariffModel;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.PenaltyRepository;
import com.wasac.utilitybilling.repository.TariffRepository;
import com.wasac.utilitybilling.repository.TaxRepository;
import com.wasac.utilitybilling.service.ConfigurationService;
import com.wasac.utilitybilling.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {
    private final TariffRepository tariffRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;

    @Override
    @Transactional
    public ConfigDtos.TariffResponse createTariff(ConfigDtos.TariffRequest request) {
        // New tariff version is one higher than the latest tariff for that utility type.
        int version = tariffRepository.findTopByMeterTypeOrderByVersionDesc(request.meterType()).map(t -> t.getVersion() + 1).orElse(1);
        // Close the tariff version that covers the new effective date.
        tariffRepository.findEffectiveTariff(request.meterType(), request.effectiveFrom())
                .ifPresent(old -> {
                    // Old version remains stored for historical bills.
                    old.setActive(false);
                    // Old version stops the day before the new version starts.
                    old.setEffectiveTo(request.effectiveFrom().minusDays(1));
                });
        // Create a new tariff record instead of overwriting historical tariff values.
        Tariff tariff = new Tariff();
        // Apply validated tariff fields and tier rules.
        apply(tariff, request, version);
        // Save and return the created tariff DTO.
        return tariffResponse(tariffRepository.save(tariff));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigDtos.TariffResponse> tariffs(int page, int size, String sortField, String sortDirection) {
        return tariffRepository.findAll(PageUtils.pageable(page, size, sortField, sortDirection)).map(this::tariffResponse);
    }

    @Override
    @Transactional
    public ConfigDtos.TariffResponse updateTariff(UUID id, ConfigDtos.TariffRequest request) {
        Tariff tariff = tariffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tariff not found"));
        apply(tariff, request, tariff.getVersion());
        return tariffResponse(tariff);
    }

    @Override
    public void deleteTariff(UUID id) {
        tariffRepository.delete(tariffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tariff not found")));
    }

    @Override
    @Transactional
    public ConfigDtos.TaxResponse createTax(ConfigDtos.TaxRequest request) {
        int version = taxRepository.findTopByNameIgnoreCaseOrderByVersionDesc(request.name()).map(t -> t.getVersion() + 1).orElse(1);
        Tax tax = new Tax();
        apply(tax, request, version);
        return taxResponse(taxRepository.save(tax));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigDtos.TaxResponse> taxes(int page, int size, String sortField, String sortDirection) {
        return taxRepository.findAll(PageUtils.pageable(page, size, sortField, sortDirection)).map(this::taxResponse);
    }

    @Override
    @Transactional
    public ConfigDtos.TaxResponse updateTax(UUID id, ConfigDtos.TaxRequest request) {
        Tax tax = taxRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax not found"));
        apply(tax, request, tax.getVersion());
        return taxResponse(tax);
    }

    @Override
    public void deleteTax(UUID id) {
        taxRepository.delete(taxRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax not found")));
    }

    @Override
    @Transactional
    public ConfigDtos.PenaltyResponse createPenalty(ConfigDtos.PenaltyRequest request) {
        int version = penaltyRepository.findTopByNameIgnoreCaseOrderByVersionDesc(request.name()).map(p -> p.getVersion() + 1).orElse(1);
        Penalty penalty = new Penalty();
        apply(penalty, request, version);
        return penaltyResponse(penaltyRepository.save(penalty));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigDtos.PenaltyResponse> penalties(int page, int size, String sortField, String sortDirection) {
        return penaltyRepository.findAll(PageUtils.pageable(page, size, sortField, sortDirection)).map(this::penaltyResponse);
    }

    @Override
    @Transactional
    public ConfigDtos.PenaltyResponse updatePenalty(UUID id, ConfigDtos.PenaltyRequest request) {
        Penalty penalty = penaltyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Penalty not found"));
        apply(penalty, request, penalty.getVersion());
        return penaltyResponse(penalty);
    }

    @Override
    public void deletePenalty(UUID id) {
        penaltyRepository.delete(penaltyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Penalty not found")));
    }

    private void apply(Tariff tariff, ConfigDtos.TariffRequest request, int version) {
        // Effective end date cannot be before start date.
        validateEffectiveRange(request.effectiveFrom(), request.effectiveTo());
        // Tiered tariffs must have non-overlapping tier ranges.
        validateTiers(request);
        // Store tariff name for business identification.
        tariff.setName(request.name());
        // Store whether this tariff applies to WATER or ELECTRICITY.
        tariff.setMeterType(request.meterType());
        // Store FLAT or TIERED billing model.
        tariff.setModel(request.model());
        // Store unit price; DTO validation requires it to be greater than zero.
        tariff.setRate(request.rate());
        // Store fixed monthly service charge; DTO validation prevents negative values.
        tariff.setFixedCharge(request.fixedCharge());
        // Store effective start date; DTO validation prevents past dates on new requests.
        tariff.setEffectiveFrom(request.effectiveFrom());
        // Null effectiveTo means this version is currently active.
        tariff.setEffectiveTo(request.effectiveTo());
        // Version keeps historical tariff records separate.
        tariff.setVersion(version);
        // A tariff is active only when it has no effective end date.
        tariff.setActive(request.effectiveTo() == null);
        // Clear old child tiers when updating a tariff.
        tariff.getTiers().clear();
        // Add tier rows only when tiers are supplied.
        if (request.tiers() != null) {
            request.tiers().forEach(t -> {
                // Create child tier linked to this tariff.
                TariffTier tier = new TariffTier();
                // Back-reference is required for JPA relationship persistence.
                tier.setTariff(tariff);
                // Store starting unit for the tier.
                tier.setFromUnits(t.fromUnits());
                // Store ending unit; null means open-ended final tier.
                tier.setToUnits(t.toUnits());
                // Store tier rate; DTO validation requires it to be positive.
                tier.setRate(t.rate());
                // Attach tier to tariff collection.
                tariff.getTiers().add(tier);
            });
        }
    }

    private void apply(Tax tax, ConfigDtos.TaxRequest request, int version) {
        validateEffectiveRange(request.effectiveFrom(), request.effectiveTo());
        tax.setName(request.name());
        tax.setRate(request.rate());
        tax.setEffectiveFrom(request.effectiveFrom());
        tax.setEffectiveTo(request.effectiveTo());
        tax.setVersion(version);
        tax.setActive(request.effectiveTo() == null);
    }

    private void apply(Penalty penalty, ConfigDtos.PenaltyRequest request, int version) {
        validateEffectiveRange(request.effectiveFrom(), request.effectiveTo());
        penalty.setName(request.name());
        penalty.setType(request.type());
        penalty.setValue(request.value());
        penalty.setEffectiveFrom(request.effectiveFrom());
        penalty.setEffectiveTo(request.effectiveTo());
        penalty.setVersion(version);
        penalty.setActive(request.effectiveTo() == null);
    }

    private ConfigDtos.TariffResponse tariffResponse(Tariff t) {
        var tiers = new ArrayList<ConfigDtos.TariffTierRequest>();
        t.getTiers().forEach(tier -> tiers.add(new ConfigDtos.TariffTierRequest(tier.getFromUnits(), tier.getToUnits(), tier.getRate())));
        return new ConfigDtos.TariffResponse(t.getId(), t.getName(), t.getMeterType(), t.getModel(), t.getRate(), t.getFixedCharge(), t.getEffectiveFrom(), t.getEffectiveTo(), t.getVersion(), t.isActive(), tiers);
    }

    private ConfigDtos.TaxResponse taxResponse(Tax t) {
        return new ConfigDtos.TaxResponse(t.getId(), t.getName(), t.getRate(), t.getEffectiveFrom(), t.getEffectiveTo(), t.getVersion(), t.isActive());
    }

    private ConfigDtos.PenaltyResponse penaltyResponse(Penalty p) {
        return new ConfigDtos.PenaltyResponse(p.getId(), p.getName(), p.getType(), p.getValue(), p.getEffectiveFrom(), p.getEffectiveTo(), p.getVersion(), p.isActive());
    }

    private void validateEffectiveRange(java.time.LocalDate effectiveFrom, java.time.LocalDate effectiveTo) {
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new BusinessRuleException("Effective end date cannot be before effective start date");
        }
    }

    private void validateTiers(ConfigDtos.TariffRequest request) {
        // Flat tariffs do not require tier validation.
        if (request.model() != TariffModel.TIERED) {
            return;
        }
        // Tiered tariffs need at least one tier so billing can calculate consumption charge.
        if (request.tiers() == null || request.tiers().isEmpty()) {
            throw new BusinessRuleException("Tiered tariffs require at least one tier");
        }

        // Sort tiers by start unit so overlap checks are deterministic.
        var sorted = request.tiers().stream()
                .sorted(Comparator.comparing(ConfigDtos.TariffTierRequest::fromUnits))
                .toList();
        // Tracks the previous tier's end boundary.
        java.math.BigDecimal previousEnd = null;
        // Validate each tier against itself and the previous tier.
        for (ConfigDtos.TariffTierRequest tier : sorted) {
            // End unit must be greater than start unit when an end is provided.
            if (tier.toUnits() != null && tier.toUnits().compareTo(tier.fromUnits()) <= 0) {
                throw new BusinessRuleException("Tariff tier end units must be greater than start units");
            }
            // Adjacent ranges must start after the previous end; equal boundaries overlap.
            if (previousEnd != null && tier.fromUnits().compareTo(previousEnd) <= 0) {
                throw new BusinessRuleException("Tariff tier ranges must not overlap");
            }
            // Store current end as the boundary for the next tier.
            previousEnd = tier.toUnits();
        }
    }
}
