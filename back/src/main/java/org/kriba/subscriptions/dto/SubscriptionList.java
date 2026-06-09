package org.kriba.subscriptions.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SubscriptionList(List<SubscriptionResponse> subscriptions) {
}