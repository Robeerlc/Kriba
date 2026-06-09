package org.kriba.users.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ModifyRequest(
        @NotNull(message = "Los datos de autenticación son obligatorios")
        @Valid
        LoginRequest loginRequest,

        String newUsername,
        String newEmail,
        String newPassword
) {
}