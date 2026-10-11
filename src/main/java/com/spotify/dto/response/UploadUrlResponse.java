package com.spotify.dto.response;

public record UploadUrlResponse(
        String uploadUrl,
        String storageKey
) {
}
