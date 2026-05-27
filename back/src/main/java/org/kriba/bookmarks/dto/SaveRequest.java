package org.kriba.bookmarks.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.kriba.users.dto.LoginRequest;

public record SaveRequest(
        @NotNull(message = "Los datos de autenticación son obligatorios") @Valid LoginRequest loginRequest,
        @NotNull(message = "El articulo es obligatorio") @Valid ArticleInput savedNew) {
}
