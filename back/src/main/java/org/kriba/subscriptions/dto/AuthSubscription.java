package org.kriba.subscriptions.dto;

import org.kriba.users.dto.LoginRequest;
import lombok.Builder;

@Builder
public record AuthSubscription(LoginRequest loginRequest, String externalSourceId, String sourceName) {}
