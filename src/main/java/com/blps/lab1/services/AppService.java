package com.blps.lab1.services;

import com.blps.lab1.dto.AppDto;
import com.blps.lab1.dto.DeveloperDto;
import com.blps.lab1.entities.App;
import com.blps.lab1.entities.Developer;
import com.blps.lab1.entities.Payment;
import com.blps.lab1.repo.AppRepository;
import com.blps.lab1.repo.DeveloperRepository;
import com.blps.lab1.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;
    private final PaymentRepository paymentRepository;
    private final DeveloperRepository developerRepository;

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

        AppDto dto = new AppDto();
        dto.setId(app.getId());
        dto.setName(app.getName());
        dto.setVersion(app.getVersion());
        dto.setStatus(app.getStatus());
        dto.setDownloads(app.getDownloads());
        dto.setRevenue(app.getRevenue());
        dto.setInAppPurchases(app.isInAppPurchases());
        dto.setNotFree(app.isNotFree());
        dto.setAppPrice(app.getAppPrice());
        dto.setMonetizationType(app.getMonetizationType());

        return dto;
    }

}
