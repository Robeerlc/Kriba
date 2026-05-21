package org.kriba.summarize.dto;

import lombok.Builder;

@Builder
public record SummarizeOutDTO(String summary, int remainingDailyUses) {
}
