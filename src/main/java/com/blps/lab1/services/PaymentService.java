package com.blps.lab1.services;

import com.blps.lab1.entities.App;
import com.blps.lab1.entities.AppUser;
import com.blps.lab1.entities.Payment;
import com.blps.lab1.enums.MonetizationType;
import com.blps.lab1.enums.PaymentStatus;
import com.blps.lab1.repo.AppRepository;
import com.blps.lab1.repo.PaymentRepository;
import com.blps.lab1.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Transactional
    public Payment payForApp(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (!app.isNotFree()) {
            throw new IllegalStateException("This app is free. No payment required.");
        }

        return processPayment(user, app, app.getAppPrice(), MonetizationType.FOR_MONEY);
    }

    @Transactional
    public Payment payForInAppPurchase(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (!app.isInAppPurchases()) {
            throw new IllegalStateException("This app does not have in-app purchases.");
        }

        return processPayment(user, app, app.getAppPrice(), MonetizationType.IN_APP_PURCHASES);
    }

    private Payment processPayment(AppUser user, App app, double amount, MonetizationType type) {

        Payment payment = new Payment();

        if (random.nextDouble() < 0.6) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new IllegalStateException("Payment failed due to incorrect input data. Please try again later.");
        }

        if (random.nextDouble() < 0.1) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new IllegalStateException("Payment failed due to a technical error. Please try again later.");
        }

        if (user.getBalance() < amount) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new IllegalStateException("Payment failed due to insufficient funds.");
        }

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        app.setRevenue(app.getRevenue() + amount);
        app.getDeveloper().setEarnings(app.getDeveloper().getEarnings() + amount);
        appRepository.save(app);


        payment.setDeveloper(app.getDeveloper());
        payment.setApp(app);
        payment.setAmount(amount);
        payment.setMonetizationType(type);
        payment.setStatus(PaymentStatus.SUCCESS);

        return paymentRepository.save(payment);
    }
}
