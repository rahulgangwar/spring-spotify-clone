package com.spotify.service.s3;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.spotify.dto.request.CompleteMultipartUploadRequest;
import com.spotify.dto.request.MultipartPartUrlRequest;
import com.spotify.dto.request.MultipartUploadRequest;
import com.spotify.dto.response.MultipartPartUrlResponse;
import com.spotify.dto.response.MultipartUploadResponse;
import com.spotify.dto.response.UploadUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@Service
public class S3StorageService {
  private final S3Presigner s3Presigner;
  private final String bucketName;
  private final S3Client s3Client;

  public S3StorageService(
      S3Presigner s3Presigner,
      @Value("${aws.s3.bucket-name}") String bucketName,
      S3Client s3Client) {
    this.s3Presigner = s3Presigner;
    this.bucketName = bucketName;
    this.s3Client = s3Client;
  }

  public UploadUrlResponse generateUploadUrl(String fileName, String contentType) {
    String extension = getFileExtension(fileName);
    String storageKey = "audio/" + UUID.randomUUID() + "_" + fileName + extension;

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

  public MultipartUploadResponse initiateMultipartUpload(MultipartUploadRequest request) {
    String storageKey = "audio/" + UUID.randomUUID() + getFileExtension(request.fileName());

    var result =
        s3Client.createMultipartUpload(
            CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .contentType(request.contentType())
                .build());

    return new MultipartUploadResponse(storageKey, result.uploadId());
  }

  public MultipartPartUrlResponse generatePartUploadUrl(MultipartPartUrlRequest request) {
    UploadPartRequest uploadPartRequest =
        UploadPartRequest.builder()
            .bucket(bucketName)
            .key(request.storageKey())
            .uploadId(request.uploadId())
            .partNumber(request.partNumber())
            .build();

    var presignRequest =
        UploadPartPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .uploadPartRequest(uploadPartRequest)
            .build();

    String url = s3Presigner.presignUploadPart(presignRequest).url().toString();
    return new MultipartPartUrlResponse(request.partNumber(), url);
  }

  public void completeMultipartUpload(CompleteMultipartUploadRequest request) {
    List<CompletedPart> completedParts =
        request.parts().stream()
            .map(
                part ->
                    CompletedPart.builder().partNumber(part.partNumber()).eTag(part.eTag()).build())
            .toList();

    s3Client.completeMultipartUpload(
        software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(request.storageKey())
            .uploadId(request.uploadId())
            .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
            .build());
  }
}
