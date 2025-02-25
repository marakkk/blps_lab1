package com.blps.lab1.services;

import com.blps.lab1.dto.AppDto;
import com.blps.lab1.entities.App;
import com.blps.lab1.entities.Payment;
import com.blps.lab1.repo.AppRepository;
import com.blps.lab1.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public App updateAnalytics(Long appId) {
        App app = appRepository.findById(appId).orElseThrow();

        double spentByUsers = paymentRepository.findByAppId(appId)
                .stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        app.setDownloads(app.getDownloads());
        app.setRevenue(app.getRevenue() + spentByUsers);

        app.getDeveloper().setEarnings(app.getDeveloper().getEarnings() + spentByUsers);

        appRepository.save(app);

        return app;
    }

    public AppDto getAppInfo(Long appId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        return new AppDto(
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
        );
    }


}
