package org.kriba.summarize.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.kriba.users.dto.LoginRequest;

public record SummarizeRequest(
        @NotNull(message = "Los datos de autenticación son obligatorios") @Valid LoginRequest loginRequest,

        @NotBlank(message = "El contenido del texto a resumir es obligatorio") String textContent,

        @NotBlank(message = "La URL del articulo es obligatoria") @Size(max = 1000, message = "La URL del articulo no puede superar los 1000 caracteres") String articleUrl,

        @NotBlank(message = "La categoria es obligatoria") @Size(max = 30, message = "La categoria no puede superar los 30 caracteres") String category) {
}