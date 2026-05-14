package org.kriba.users.dto;

import lombok.Builder;

@Builder
public record AuthResponse(long userId, String username, int dailyAiLimit) {}