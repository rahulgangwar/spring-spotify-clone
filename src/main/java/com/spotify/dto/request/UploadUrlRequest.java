package com.spotify.dto.request;

public record UploadUrlRequest(
        String fileName,
        String contentType
) {
}
