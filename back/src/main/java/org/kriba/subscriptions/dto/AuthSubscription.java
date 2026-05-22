package org.kriba.subscriptions.dto;

import lombok.Builder;
import org.kriba.users.dto.LoginRequest;

@Builder
public record AuthSubscription(LoginRequest loginRequest, String externalSourceId, String sourceName) {
}
