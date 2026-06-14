package com.orderdesk.dto;

public record NoteDto(
        Long id,
        String author,
        String body,
        String createdAt) {
}
