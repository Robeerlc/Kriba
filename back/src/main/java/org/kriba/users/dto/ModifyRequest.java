package org.kriba.users.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ModifyRequest(@Valid @NotNull (message = "Los datos de autenticación son obligatorios") 
LoginRequest loginRequest, String newUsername, String newEmail, String newPassword) {
}