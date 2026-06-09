package org.kriba.subscriptions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.kriba.users.dto.LoginRequest;

@Builder
public record SubscriptionRequest(
        @NotNull(message = "Los datos de autenticación son obligatorios") @Valid LoginRequest loginRequest,
        @NotBlank(message = "El id de la fuente es obligatorio") String externalSourceId,
        @NotBlank(message = "El nombre de la fuente es obligatorio") String sourceName) {
}
