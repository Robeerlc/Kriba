package org.kriba.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El email no puede estar vacío") @Email(message = "El formato del email no es válido") String email,
        @NotBlank(message = "La contraseña no puede estar vacía") String password) {
}