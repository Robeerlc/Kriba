package org.kriba.bookmarks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleInput(@NotBlank(message = "El id externo del articulo es obligatorio") String externalArticleId,
                           @NotBlank(message = "El titulo es obligatorio") String title,
                           @NotBlank(message = "La URL es obligatoria") @Size(max = 1000, message = "La URL no puede superar los 1000 caracteres") String url,
                           @NotBlank(message = "La categoria es obligatoria") @Size(max = 30, message = "La categoria no puede superar los 30 caracteres") String category,
                           @NotBlank(message = "La descripción es obligatoria") String description,
                           @NotBlank(message = "El contenido es obligatorio") String content,
                           @NotBlank(message = "La imagen es obligatoria") @Size(max = 1000, message = "La URL de la imagen no puede superar los 1000 caracteres") String image) {
}