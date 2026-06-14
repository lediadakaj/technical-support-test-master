package com.orderdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 500, message = "must be at most 500 characters")
        String body) {
}
