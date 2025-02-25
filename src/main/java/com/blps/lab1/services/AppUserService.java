package com.blps.lab1.services;

import com.blps.lab1.entities.App;
import com.blps.lab1.entities.AppUser;
import com.blps.lab1.entities.Payment;
import com.blps.lab1.enums.PaymentStatus;
import com.blps.lab1.repo.AppRepository;
import com.blps.lab1.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AppUserService {

    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final PaymentService paymentService;

    @Transactional
    public String downloadAndUseApp(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        if (app.isNotFree()) {
            Payment payment = paymentService.payForApp(userId, appId);
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                return "Payment for app failed.";
            }
        }

        String result = "User " + user.getUsername() + " downloaded and started using " + app.getName() + ".";

        if (app.isInAppPurchases()) {
            result += "\nIn-app purchases detected.";
            Payment inAppPayment = paymentService.payForInAppPurchase(userId, appId);
            if (inAppPayment.getStatus() == PaymentStatus.SUCCESS) {
                result += "\nUser purchased in-app content and continues using the app.";
            } else {
                result += "\nUser could not purchase in-app content.";
            }
        }

        return result;
    }
}
