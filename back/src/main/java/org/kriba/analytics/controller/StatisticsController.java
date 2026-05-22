package org.kriba.analytics.controller;

import org.kriba.analytics.dto.StatisticsResponse;
import org.kriba.analytics.service.StatisticsService;
import org.kriba.users.dto.AuthResponse;
import org.kriba.users.dto.LoginRequest;
import org.kriba.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    public StatisticsController(StatisticsService statisticsService, UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<StatisticsResponse> getStatistics(@RequestBody LoginRequest request) {
        if (request == null) return ResponseEntity.badRequest().build();
        AuthResponse authUser = userService.login(request);
        return ResponseEntity.ok(statisticsService.getUserStatistics(authUser.userId()));
    }
}
