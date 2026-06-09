package org.kriba.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message = "El nombre de usuario no puede estar vacío") String username,

                              @NotBlank(message = "El email no puede estar vacío") @Email(message = "El formato del email no es válido") String email,

                              @NotBlank(message = "La contraseña no puede estar vacía") @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password) {
}