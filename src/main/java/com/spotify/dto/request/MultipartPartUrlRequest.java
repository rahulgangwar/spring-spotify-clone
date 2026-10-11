package com.spotify.dto.request;

public record MultipartPartUrlRequest(String storageKey, String uploadId, int partNumber) {}
