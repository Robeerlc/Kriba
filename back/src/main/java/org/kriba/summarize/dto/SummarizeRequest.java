package org.kriba.summarize.dto;

import org.kriba.users.dto.LoginRequest;

public record SummarizeRequest(LoginRequest loginRequest, String textContent, String articleUrl, String category) {
}
