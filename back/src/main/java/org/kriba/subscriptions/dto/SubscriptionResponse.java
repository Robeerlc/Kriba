package org.kriba.subscriptions.dto;

import lombok.Builder;

@Builder
public record SubscriptionResponse(String externalSourceId, String sourceName) {}