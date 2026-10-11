package com.spotify.service.s3;

import com.spotify.dto.UploadUrlRequest;
import com.spotify.dto.UploadUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3StorageService {

  private final S3Presigner s3Presigner;
  private final String bucketName;

  public S3StorageService(
      S3Presigner s3Presigner, @Value("${aws.s3.bucket-name}") String bucketName) {
    this.s3Presigner = s3Presigner;
    this.bucketName = bucketName;
  }

  public UploadUrlResponse generateUploadUrl(UploadUrlRequest request) {
    String storageKey = "audio/" + UUID.randomUUID() + getFileExtension(request.fileName());
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(storageKey)
            .contentType(request.contentType())
            .build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(putObjectRequest)
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
    return new UploadUrlResponse(presignedRequest.url().toString(), storageKey);
  }

  private String getFileExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
  }
}
