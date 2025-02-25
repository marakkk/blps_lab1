package com.blps.lab1.controllers;

import com.blps.lab1.dto.AppDto;
import com.blps.lab1.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-actions")
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping("/catalog")
    public ResponseEntity<List<AppDto>> viewAppCatalog() {
        List<AppDto> catalog = appUserService.viewAppCatalog();
        return ResponseEntity.ok(catalog);
    }

    @PostMapping("/{userId}/download/{appId}")
    public ResponseEntity<String> downloadApp(@PathVariable Long userId, @PathVariable Long appId) {
        String result = appUserService.downloadApp(userId, appId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/use/{appId}")
    public ResponseEntity<String> useApp(@PathVariable Long userId, @PathVariable Long appId) {
        String result = appUserService.useApp(userId, appId);
        return ResponseEntity.ok(result);
    }
}
