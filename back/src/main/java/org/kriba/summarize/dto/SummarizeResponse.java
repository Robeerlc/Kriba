package org.kriba.summarize.dto;

import lombok.Builder;

@Builder
public record SummarizeResponse(String summary, int remainingDailyUses) {
}
