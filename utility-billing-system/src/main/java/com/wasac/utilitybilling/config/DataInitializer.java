package com.wasac.utilitybilling.config;

import com.wasac.utilitybilling.entity.Penalty;
import com.wasac.utilitybilling.entity.Tariff;
import com.wasac.utilitybilling.entity.Tax;
import com.wasac.utilitybilling.entity.User;
import com.wasac.utilitybilling.entity.enums.AccountStatus;
import com.wasac.utilitybilling.entity.enums.MeterType;
import com.wasac.utilitybilling.entity.enums.PenaltyType;
import com.wasac.utilitybilling.entity.enums.Role;
import com.wasac.utilitybilling.entity.enums.TariffModel;
import com.wasac.utilitybilling.repository.PenaltyRepository;
import com.wasac.utilitybilling.repository.TariffRepository;
import com.wasac.utilitybilling.repository.TaxRepository;
import com.wasac.utilitybilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    // Repository used to check whether the default admin already exists.
    private final UserRepository userRepository;
    // Repository used to seed default WATER and ELECTRICITY tariffs for billing tests.
    private final TariffRepository tariffRepository;
    // Repository used to seed a VAT tax so generated bills include tax data.
    private final TaxRepository taxRepository;
    // Repository used to seed a late-payment penalty for overdue bill handling.
    private final PenaltyRepository penaltyRepository;
    // Password encoder stores the seeded admin password as BCrypt, not plain text.
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner bootstrapAdmin(@Value("${app.bootstrap.admin-email}") String email,
                                     @Value("${app.bootstrap.admin-phone}") String phone,
                                     @Value("${app.bootstrap.admin-password}") String password) {
        // ApplicationRunner executes once when Spring Boot finishes starting.
        return args -> {
            // First try to find the configured admin email.
            var existingByEmail = userRepository.findByEmail(email);
            // If the configured email already exists, ensure it still has admin access.
            if (existingByEmail.isPresent()) {
                User admin = existingByEmail.get();
                // Keep the seeded admin active after restarts.
                admin.setStatus(AccountStatus.ACTIVE);
                // Keep the seeded admin email verified.
                admin.setEmailVerified(true);
                // Ensure ROLE_ADMIN is present even if the record was edited manually.
                admin.getRoles().add(Role.ROLE_ADMIN);
                // Save the repaired admin row before seeding billing configuration.
                userRepository.save(admin);
                // Seed default billing configuration needed for exam testing.
                seedBillingConfiguration();
                return;
            }
            // If the old seeded admin phone already exists, update that row instead of inserting a duplicate.
            var existingByPhone = userRepository.findByPhoneNumber(phone);
            // This handles changing the seeded admin email from the previous default to belyse457@gmail.com.
            if (existingByPhone.isPresent()) {
                User admin = existingByPhone.get();
                // Move the existing seeded admin to the configured email.
                admin.setEmail(email);
                // Keep a readable seeded admin name.
                admin.setFullName("WASAC REG Administrator");
                // Hash the configured password so the requested credentials work.
                admin.setPasswordHash(passwordEncoder.encode(password));
                // Seeded admin should not be forced to change password.
                admin.setPasswordChangeRequired(false);
                // Seeded admin temporary password expiry is not used.
                admin.setPasswordExpiresAt(null);
                // Keep the seeded admin active.
                admin.setStatus(AccountStatus.ACTIVE);
                // Keep the seeded admin email verified.
                admin.setEmailVerified(true);
                // Ensure administrator permission exists.
                admin.getRoles().add(Role.ROLE_ADMIN);
                // Save the repaired admin row before seeding billing configuration.
                userRepository.save(admin);
                // Seed default billing configuration needed for exam testing.
                seedBillingConfiguration();
                return;
            }
            // Create the seeded administrator account used to manage staff and billing setup.
            User admin = new User();
            // Set a clear display name for the default admin.
            admin.setFullName("WASAC REG Administrator");
            // Use the configured admin email; default is belyse457@gmail.com.
            admin.setEmail(email);
            // Store the configured phone number so the unique user phone rule is satisfied.
            admin.setPhoneNumber(phone);
            // Hash the configured password before saving it to the database.
            admin.setPasswordHash(passwordEncoder.encode(password));
            // Seeded admin should not be forced to change password.
            admin.setPasswordChangeRequired(false);
            // Seeded admin is active immediately because no public admin registration is allowed.
            admin.setStatus(AccountStatus.ACTIVE);
            // Mark email verified so the admin can login immediately.
            admin.setEmailVerified(true);
            // Give the seeded user administrator permissions.
            admin.getRoles().add(Role.ROLE_ADMIN);
            // Persist the admin only after all required fields are valid.
            userRepository.save(admin);
            // Seed default billing configuration needed for exam testing.
            seedBillingConfiguration();
        };
    }

    private void seedBillingConfiguration() {
        // Use a past effective date so existing 2026 meter readings can be billed immediately.
        LocalDate effectiveFrom = LocalDate.of(2026, 1, 1);
        // Seed a simple WATER tariff when no bill-usable WATER tariff exists.
        tariffRepository.findEffectiveTariff(MeterType.WATER, effectiveFrom)
                .orElseGet(() -> createTariff("Default WASAC Water Tariff", MeterType.WATER, new BigDecimal("500.00"), new BigDecimal("1000.00"), effectiveFrom));
        // Seed a simple ELECTRICITY tariff when no bill-usable ELECTRICITY tariff exists.
        tariffRepository.findEffectiveTariff(MeterType.ELECTRICITY, effectiveFrom)
                .orElseGet(() -> createTariff("Default REG Electricity Tariff", MeterType.ELECTRICITY, new BigDecimal("300.00"), new BigDecimal("1500.00"), effectiveFrom));
        // Seed VAT only when it does not already exist.
        taxRepository.findTopByNameIgnoreCaseOrderByVersionDesc("VAT")
                .ifPresentOrElse(this::repairSeededVat, () -> createTax("VAT", new BigDecimal("0.1800"), effectiveFrom));
        // Seed a late payment penalty only when it does not already exist.
        penaltyRepository.findTopByNameIgnoreCaseOrderByVersionDesc("Default Late Payment Penalty")
                .ifPresentOrElse(this::repairSeededPenalty, () -> createPenalty("Default Late Payment Penalty", PenaltyType.PERCENTAGE, new BigDecimal("0.05"), effectiveFrom));
    }

    private Tariff createTariff(String name, MeterType meterType, BigDecimal rate, BigDecimal fixedCharge, LocalDate effectiveFrom) {
        // Build the minimum active flat tariff needed by bill generation.
        Tariff tariff = new Tariff();
        // Human-readable tariff name shown in Swagger responses.
        tariff.setName(name);
        // Meter type controls whether this tariff is used for WATER or ELECTRICITY readings.
        tariff.setMeterType(meterType);
        // Flat tariff means consumption charge = units consumed * rate.
        tariff.setModel(TariffModel.FLAT);
        // Price per consumed unit.
        tariff.setRate(rate);
        // Fixed service charge added to each generated bill.
        tariff.setFixedCharge(fixedCharge);
        // Effective date must be before or equal to the reading billing date.
        tariff.setEffectiveFrom(effectiveFrom);
        // Null effectiveTo means this is the current active tariff.
        tariff.setEffectiveTo(null);
        // First seeded version for this meter type.
        tariff.setVersion(1);
        // Active flag is required by the tariff lookup used during bill generation.
        tariff.setActive(true);
        // Save and return the tariff so the initializer remains idempotent.
        return tariffRepository.save(tariff);
    }

    private Tax createTax(String name, BigDecimal rate, LocalDate effectiveFrom) {
        // Build a VAT tax used by bill generation.
        Tax tax = new Tax();
        // Human-readable tax name.
        tax.setName(name);
        // Tax rate percentage, for example 18.0000 means 18%.
        tax.setRate(rate);
        // Effective date must be before or equal to the billing date.
        tax.setEffectiveFrom(effectiveFrom);
        // Null effectiveTo means this tax is active.
        tax.setEffectiveTo(null);
        // First seeded version.
        tax.setVersion(1);
        // Active flag is required by the tax lookup.
        tax.setActive(true);
        // Save and return the seeded tax.
        return taxRepository.save(tax);
    }

    private Penalty createPenalty(String name, PenaltyType type, BigDecimal value, LocalDate effectiveFrom) {
        // Build a default late-payment penalty used when bills become overdue.
        Penalty penalty = new Penalty();
        // Human-readable penalty name.
        penalty.setName(name);
        // Percentage means the value is applied as a percentage of the outstanding balance.
        penalty.setType(type);
        // Penalty value, for example 5.00 means 5%.
        penalty.setValue(value);
        // Effective date must be before or equal to the overdue processing date.
        penalty.setEffectiveFrom(effectiveFrom);
        // Null effectiveTo means this penalty is active.
        penalty.setEffectiveTo(null);
        // First seeded version.
        penalty.setVersion(1);
        // Active flag is required by the penalty lookup.
        penalty.setActive(true);
        // Save and return the seeded penalty.
        return penaltyRepository.save(penalty);
    }

    private void repairSeededVat(Tax tax) {
        // VAT is stored as decimal rate, so 0.1800 means 18%.
        if ("VAT".equalsIgnoreCase(tax.getName()) && tax.getRate().compareTo(BigDecimal.ONE) > 0) {
            // Correct old seeded value if it was stored as 18 instead of 0.18.
            tax.setRate(new BigDecimal("0.1800"));
            // Save the corrected rate for future bill generation.
            taxRepository.save(tax);
        }
    }

    private void repairSeededPenalty(Penalty penalty) {
        // Percentage penalty is stored as decimal rate, so 0.05 means 5%.
        if ("Default Late Payment Penalty".equalsIgnoreCase(penalty.getName())
                && penalty.getType() == PenaltyType.PERCENTAGE
                && penalty.getValue().compareTo(BigDecimal.ONE) > 0) {
            // Correct old seeded value if it was stored as 5 instead of 0.05.
            penalty.setValue(new BigDecimal("0.05"));
            // Save the corrected value for future overdue processing.
            penaltyRepository.save(penalty);
        }
    }
}
