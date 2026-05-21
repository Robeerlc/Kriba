package org.kriba.bookmarks.dto;

import org.kriba.users.dto.LoginRequest;

public record SaveRequest(LoginRequest loginRequest, ArticleInputDto savedNew) {}
