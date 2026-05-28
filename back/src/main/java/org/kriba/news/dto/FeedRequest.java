package org.kriba.news.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.kriba.users.dto.LoginRequest;

public record FeedRequest(@Valid LoginRequest loginRequest,
                          @Size(max = 30, message = "La categoria no puede superar los 30 caracteres") String category, Integer pageNo, Integer pageSize) {
}