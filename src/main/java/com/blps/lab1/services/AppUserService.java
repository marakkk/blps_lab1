package com.blps.lab1.services;

import com.blps.lab1.dto.AppDto;
import com.blps.lab1.entities.App;
import com.blps.lab1.entities.AppUser;
import com.blps.lab1.entities.Payment;
import com.blps.lab1.enums.PaymentStatus;
import com.blps.lab1.repo.AppRepository;
import com.blps.lab1.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AppUserService {

    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final PaymentService paymentService;

    public List<AppDto> viewAppCatalog() {
        return appRepository.findAll().stream()
                .map(app -> new AppDto(
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
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String downloadApp(Long userId, Long appId) {
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

        return "User " + user.getUsername() + " successfully downloaded " + app.getName() + ".";
    }

    @Transactional
    public String useApp(Long userId, Long appId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("App not found"));

        String result = "User " + user.getUsername() + " started using " + app.getName() + ".";

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
