package com.spotify.controller;

import java.util.List;
import java.util.Map;

import com.spotify.dto.request.*;
import com.spotify.dto.response.MultipartPartUrlResponse;
import com.spotify.dto.response.MultipartUploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.spotify.dto.response.PlaySongResponse;
import com.spotify.dto.response.UploadUrlResponse;
import com.spotify.entity.Song;
import com.spotify.service.s3.S3StorageService;
import com.spotify.service.SongService;

@RestController
@RequestMapping("/api/songs")
public class SongController {
    private final SongService songService;
    private final S3StorageService s3StorageService;

    public SongController(SongService songService, S3StorageService s3StorageService) {
        this.songService = songService;
        this.s3StorageService = s3StorageService;
    }

    @PostMapping
    public Song create(@RequestBody CreateSongRequest request) {
        Song song = new Song();
        song.setTitle(request.title());
        song.setStorageKey(request.storageKey());
        song.setContentType(request.contentType());
        song.setDurationSeconds(request.durationSeconds());
        return songService.save(song);
    }

    @GetMapping
    public List<Song> getAll() {
        return songService.getAll();
    }

    @GetMapping("/{id}")
    public Song getById(@PathVariable Long id) {
        return songService.getById(id);
    }

    @PutMapping("/{id}")
    public Song update(@PathVariable Long id, @RequestBody UpdateSongRequest request) {
        return songService.update(id, request);
    }

    @PatchMapping("/{id}")
    public Song patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return songService.patch(id, updates);
    }

    @PostMapping("/upload-url")
    public UploadUrlResponse generateUploadUrl(@RequestBody UploadUrlRequest request) {
        return s3StorageService.generateUploadUrl(request.fileName(), request.contentType());
    }

    @GetMapping("/{id}/play")
    public PlaySongResponse play(@PathVariable Long id) {
        Song song = songService.getById(id);
        String playUrl = s3StorageService.generateDownloadUrl(song.getStorageKey());
        return new PlaySongResponse(playUrl);
    }

    @PostMapping("/multipart/initiate")
    public MultipartUploadResponse initiateMultipartUpload(
            @RequestBody MultipartUploadRequest request) {
        return s3StorageService.initiateMultipartUpload(request);
    }

    @PostMapping("/multipart/upload-part")
    public MultipartPartUrlResponse generatePartUploadUrl(
            @RequestBody MultipartPartUrlRequest request) {
        return s3StorageService.generatePartUploadUrl(request);
    }

    @PostMapping("/multipart/complete")
    public ResponseEntity<Void> completeMultipartUpload(
            @RequestBody CompleteMultipartUploadRequest request) {
        s3StorageService.completeMultipartUpload(request);
        return ResponseEntity.ok().build();
    }
}
