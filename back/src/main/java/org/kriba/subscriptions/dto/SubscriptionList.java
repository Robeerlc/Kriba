package org.kriba.subscriptions.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record SubscriptionList(
    List<SubscriptionResponse> subscriptions
) {}