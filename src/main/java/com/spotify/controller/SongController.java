package com.spotify.controller;

import com.spotify.dto.CreateSongRequest;
import com.spotify.dto.UploadUrlRequest;
import com.spotify.dto.UploadUrlResponse;
import com.spotify.entity.Song;
import com.spotify.service.SongService;
import com.spotify.service.s3.S3StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
  @ResponseStatus(HttpStatus.CREATED)
  public Song create(@RequestBody CreateSongRequest request) {
    Song song = new Song();
    song.setTitle(request.title());
    song.setStorageKey(request.storageKey());
    song.setContentType(request.contentType());
    song.setDurationSeconds(request.durationSeconds());

    return songService.saveSong(song);
  }

  @GetMapping("/{id}")
  public Song getById(@PathVariable Long id) {
    return songService.getSongById(id);
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<Song> getAll() {
    return songService.getAllSongs();
  }

  @PostMapping("/upload-url")
  public UploadUrlResponse generateUploadUrl(@RequestBody UploadUrlRequest request) {
    return s3StorageService.generateUploadUrl(request);
  }
}
