package com.spotify.service.s3;

import java.time.Duration;
import java.util.UUID;

import com.spotify.dto.UploadUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3StorageService {
  private final S3Presigner s3Presigner;
  private final String bucketName;

  public S3StorageService(
      S3Presigner s3Presigner, @Value("${aws.s3.bucket-name}") String bucketName) {
    this.s3Presigner = s3Presigner;
    this.bucketName = bucketName;
  }

  public UploadUrlResponse generateUploadUrl(String fileName, String contentType) {
    String extension = getFileExtension(fileName);
    String storageKey = "audio/" + UUID.randomUUID() + extension;

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(storageKey)
            .contentType(contentType)
            .build();

    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(putObjectRequest)
            .build();

    String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

    return new UploadUrlResponse(uploadUrl, storageKey);
  }

  public String generateDownloadUrl(String storageKey) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(storageKey).build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(getObjectRequest)
            .build();

    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }

  private String getFileExtension(String fileName) {
    int index = fileName.lastIndexOf('.');
    return index >= 0 ? fileName.substring(index) : "";
  }
}
