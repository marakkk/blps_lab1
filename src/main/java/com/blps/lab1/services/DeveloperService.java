package com.blps.lab1.services;

import com.blps.lab1.dto.AppDto;
import com.blps.lab1.dto.DeveloperDto;
import com.blps.lab1.entities.App;
import com.blps.lab1.entities.Developer;
import com.blps.lab1.entities.Payment;
import com.blps.lab1.enums.AppStatus;
import com.blps.lab1.enums.DevAccount;
import com.blps.lab1.enums.MonetizationType;
import com.blps.lab1.enums.PaymentStatus;
import com.blps.lab1.repo.AppRepository;
import com.blps.lab1.repo.DeveloperRepository;
import com.blps.lab1.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DeveloperService {
    private static final double PUBLISHING_FEE = 25.0;

    private static final Logger logger = LoggerFactory.getLogger(DeveloperService.class);

    private final DeveloperRepository repository;
    private final AppRepository appRepository;
    private final PaymentRepository paymentRepository;
    private final GooglePlayService googlePlayService;
    private final DeveloperRepository developerRepository;


    public void register(Long developerId) {
        Developer dev = repository.findById(developerId).orElseThrow();
        dev.setAccStatus(DevAccount.PAID);
        dev.setPaymentProfile(true);
        repository.save(dev);
    }

    @Transactional
    public void validateApp(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (app.getName() == null || app.getName().isEmpty()) {
            throw new IllegalStateException("App name is required.");
        }

        if (app.getVersion() == 0) {
            throw new IllegalStateException("App version is required.");
        }

        if (!isValidVersion(app.getVersion())) {
            throw new IllegalStateException("App version must be a valid number.");
        }

        if (!app.isCorrectPermissions()) {
            throw new IllegalStateException("App has incorrect or excessive permissions.");
        }

        app.setStatus(AppStatus.VALIDATED);
        appRepository.save(app);

        logger.info("App with ID {} has been successfully validated.", appId);
    }

    private boolean isValidVersion(double version) {
        return version > 0;
    }


    @Transactional
    public App submitApp(Long developerId, Long appId, boolean wantsToMonetize, boolean wantsToCharge) {
        Developer developer = repository.findById(developerId)
                .orElseThrow(() -> new IllegalStateException("Developer with ID " + developerId + " not found."));

        if (developer.getAccStatus() == DevAccount.UNPAID) {
            register(developerId);
        }

        if (!developer.isPaymentProfile()) {
            throw new IllegalStateException("Developer must set up a payment profile before submitting apps.");
        }

        App app = appRepository.findById(appId).orElseThrow(() ->
                new IllegalStateException("App with ID " + appId + " not found."));

        try {
            validateApp(appId);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("App validation failed: " + e.getMessage());
        }

        MonetizationType monetizationType = determineMonetizationType(wantsToMonetize, wantsToCharge);
        app.setMonetizationType(monetizationType);

        Payment payment = createPaymentForAppSubmission(app, monetizationType);

        if (!processPayment(payment)) {
            throw new IllegalStateException("Payment failed, unable to submit app.");
        }

        app.setStatus(AppStatus.PENDING);
        app.setDeveloper(developer);

        app = appRepository.save(app);
        notifyDeveloperPaymentSuccess(developer, payment);

        return app;
    }

    private Payment createPaymentForAppSubmission(App app, MonetizationType monetizationType) {
        Payment payment = new Payment();
        payment.setAmount(PUBLISHING_FEE);
        payment.setMonetizationType(monetizationType);
        payment.setApp(app);
        paymentRepository.save(payment);
        return payment;
    }


    private boolean processPayment(Payment payment) {
        if (payment.getAmount() > 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            return true;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return false;
        }
    }

    private void notifyDeveloperPaymentSuccess(Developer developer, Payment payment) {
        logger.info("Developer {} has successfully paid the fee: {}", developer.getName(), payment.getAmount());
    }


    private MonetizationType determineMonetizationType(boolean wantsToMonetize, boolean wantsToCharge) {
        if (!wantsToMonetize && !wantsToCharge) {
            return MonetizationType.FREE;
        } else if (wantsToCharge) {
            return MonetizationType.FOR_MONEY;
        } else {
            return MonetizationType.IN_APP_PURCHASES;
        }
    }



    @Transactional
    public Map<String, String> publishApp(Long developerId, Long appId, boolean approvedByModerator, String moderatorComment) {
        Developer developer = repository.findById(developerId)
                .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (!app.getDeveloper().equals(developer)) {
            throw new IllegalStateException("Developer does not own this app.");
        }

        if (app.getStatus() != AppStatus.PENDING) {
            throw new IllegalStateException("App must be submitted for review before finalizing.");
        }

        Map<String, String> reviewResult = googlePlayService.autoReviewApp(appId);

        if ("App approved automatically.".equals(reviewResult.get("message"))) {
            googlePlayService.publishApp(appId);
            return Map.of("message", "App successfully published after automatic review.");
        }

        if ("App requires manual review.".equals(reviewResult.get("message"))) {
            Map<String, String> manualReviewResult = googlePlayService.manualReviewApp(appId, approvedByModerator, moderatorComment);

            if (manualReviewResult.containsKey("reason")) {
                return Map.of("error", "App rejected by moderator", "reason", manualReviewResult.get("reason"));
            }

            googlePlayService.publishApp(appId);
            return Map.of("message", "App successfully published after manual review.");
        }

        return Map.of("error", "Unexpected issue during review process.");
    }


    public DeveloperDto getDeveloperInfo(Long developerId) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new RuntimeException("Developer not found"));

        return new DeveloperDto(
                developer.getId(),
                developer.getName(),
                developer.getEmail(),
                developer.isPaymentProfile(),
                developer.getAccStatus(),
                developer.getEarnings(),
                developer.getApps().stream().map(app -> new AppDto(
                        app.getId(),
                        app.getName(),
                        app.getVersion(),
                        app.getStatus(),
                        app.getDownloads(),
                        app.getRevenue(),
                        app.isInAppPurchases(),
                        app.isNotFree(),
                        app.getAppPrice(),
                        app.getMonetizationType()
                )).collect(Collectors.toList())
        );
    }

}
