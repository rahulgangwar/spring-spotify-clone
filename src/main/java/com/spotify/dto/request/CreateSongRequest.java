package com.spotify.dto.request;

public record CreateSongRequest(
        String title,
        String storageKey,
        String contentType,
        Integer durationSeconds
) {
}