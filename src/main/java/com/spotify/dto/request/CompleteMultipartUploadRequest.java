package com.spotify.dto.request;

import java.util.List;

public record CompleteMultipartUploadRequest(
    String storageKey, String uploadId, List<UploadedPart> parts) {
  public record UploadedPart(int partNumber, String eTag) {}
}
