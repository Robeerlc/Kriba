package org.kriba.analytics.dto;

import org.kriba.users.dto.LoginRequest;

public record InteractionRequest(LoginRequest loginRequest, String articleCategory, String interactionType) {}