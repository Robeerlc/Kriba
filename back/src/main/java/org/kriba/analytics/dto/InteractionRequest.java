package org.kriba.analytics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.kriba.users.dto.LoginRequest;

public record InteractionRequest(
        @NotNull(message = "Los datos de autenticación son obligatorios") @Valid LoginRequest loginRequest,
        @NotBlank(message = "La categoria del articulo es obligatoria") String articleCategory,
        @NotBlank(message = "El tipo de interacción es obligatorio") @Pattern(regexp = "CLICK|SAVE|SUMMARIZE", message = "interactionType debe ser CLICK, SAVE o SUMMARIZE") String interactionType) {
}