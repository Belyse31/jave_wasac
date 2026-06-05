package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.ConfigDtos;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ConfigurationService {
    ConfigDtos.TariffResponse createTariff(ConfigDtos.TariffRequest request);
    Page<ConfigDtos.TariffResponse> tariffs(int page, int size, String sortField, String sortDirection);
    ConfigDtos.TariffResponse updateTariff(UUID id, ConfigDtos.TariffRequest request);
    void deleteTariff(UUID id);
    ConfigDtos.TaxResponse createTax(ConfigDtos.TaxRequest request);
    Page<ConfigDtos.TaxResponse> taxes(int page, int size, String sortField, String sortDirection);
    ConfigDtos.TaxResponse updateTax(UUID id, ConfigDtos.TaxRequest request);
    void deleteTax(UUID id);
    ConfigDtos.PenaltyResponse createPenalty(ConfigDtos.PenaltyRequest request);
    Page<ConfigDtos.PenaltyResponse> penalties(int page, int size, String sortField, String sortDirection);
    ConfigDtos.PenaltyResponse updatePenalty(UUID id, ConfigDtos.PenaltyRequest request);
    void deletePenalty(UUID id);
}
