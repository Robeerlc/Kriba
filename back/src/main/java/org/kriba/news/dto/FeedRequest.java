package org.kriba.news.dto;

import org.kriba.users.dto.LoginRequest;

public record FeedRequest(LoginRequest loginRequest, String category) {
}