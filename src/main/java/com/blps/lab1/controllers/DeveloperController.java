package com.blps.lab1.controllers;

import com.blps.lab1.dto.AppDto;
import com.blps.lab1.dto.DeveloperDto;
import com.blps.lab1.entities.App;
import com.blps.lab1.errors.ErrorResponse;
import com.blps.lab1.services.AppService;
import com.blps.lab1.services.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/developer-actions")
class DeveloperController {

    private final DeveloperService developerService;
    private final AppService appService;

    @PostMapping("/{developerId}/apps/{appId}/submit")
    public ResponseEntity<Object> submitApp(
            @PathVariable Long developerId,
            @PathVariable Long appId,
            @RequestParam boolean wantsToMonetize,
            @RequestParam boolean wantsToCharge) {

        try {
            App submittedApp = developerService.submitApp(developerId, appId, wantsToMonetize, wantsToCharge);
            return ResponseEntity.ok(submittedApp);
        } catch (IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @PostMapping("/{developerId}/apps/{appId}/publish")
    public ResponseEntity<Map<String, String>> publishApp(@PathVariable Long developerId,
                                                          @PathVariable Long appId,
                                                          @RequestParam boolean approvedByModerator,
                                                          @RequestParam String moderatorComment) {
        Map<String, String> result = developerService.publishApp(developerId, appId, approvedByModerator, moderatorComment);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/dev-info")
    public ResponseEntity<DeveloperDto> getDeveloperInfo(@PathVariable Long id) {
        DeveloperDto developer = developerService.getDeveloperInfo(id);
        return ResponseEntity.ok(developer);
    }


    @PutMapping("/{id}/analytics")
    public App updateAnalytics(@PathVariable Long id) {
        return appService.updateAnalytics(id);
    }

    @GetMapping("/{id}/app-info")
    public ResponseEntity<AppDto> getAppInfo(@PathVariable Long id) {
        AppDto app = appService.getAppInfo(id);
        return ResponseEntity.ok(app);
    }

}
