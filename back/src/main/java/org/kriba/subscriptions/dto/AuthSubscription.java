package org.kriba.subscriptions.dto;
import lombok.Builder;


@Builder
public record AuthSubscription(long userId, String externarlSourceId, String sourceName) {

}
