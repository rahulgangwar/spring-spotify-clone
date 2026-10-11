package com.spotify.dto;

public record UploadUrlRequest(
        String fileName,
        String contentType
) {
}
