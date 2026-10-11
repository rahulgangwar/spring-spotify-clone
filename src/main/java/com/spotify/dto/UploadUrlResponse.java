package com.spotify.dto;

public record UploadUrlResponse(
        String uploadUrl,
        String storageKey
) {
}
