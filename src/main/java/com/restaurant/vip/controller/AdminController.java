package com.restaurant.vip.controller;

import com.restaurant.vip.service.SessionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private SessionManagementService sessionManagementService;

    @GetMapping("/sessions/statistics")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SessionManagementService.SessionStatistics> getSessionStatistics() {
        SessionManagementService.SessionStatistics stats = sessionManagementService.getSessionStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/sessions/staff/{staffId}/logout")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> forceLogoutStaff(@PathVariable Long staffId) {
        sessionManagementService.removeAllSessionsForStaff(staffId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/staff/{staffId}/count")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Long> getActiveSessionCount(@PathVariable Long staffId) {
        long count = sessionManagementService.getActiveSessionCount(staffId);
        return ResponseEntity.ok(count);
    }
}