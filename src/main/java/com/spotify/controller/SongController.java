package com.spotify.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spotify.dto.CreateSongRequest;
import com.spotify.dto.PlaySongResponse;
import com.spotify.dto.UploadUrlRequest;
import com.spotify.dto.UploadUrlResponse;
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
}
