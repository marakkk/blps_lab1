package com.blps.lab1.controllers;

import com.blps.lab1.services.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-actions")
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping("/{userId}/download/{appId}")
    public ResponseEntity<String> downloadAndUseApp(@PathVariable Long userId, @PathVariable Long appId) {
        String result = appUserService.downloadAndUseApp(userId, appId);
        return ResponseEntity.ok(result);
    }
}
