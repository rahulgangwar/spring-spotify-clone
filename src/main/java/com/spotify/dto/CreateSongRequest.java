package com.spotify.dto;

public record CreateSongRequest(
        String title,
        String storageKey,
        String contentType,
        Integer durationSeconds
) {
}