package com.spotify.dto.request;

public record UpdateSongRequest(
        String title,
        String storageKey,
        String contentType,
        Integer durationSeconds
) {}
