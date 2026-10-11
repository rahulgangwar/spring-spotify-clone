package com.spotify.service;

import java.util.List;
import java.util.Map;

import com.spotify.dto.request.UpdateSongRequest;
import com.spotify.service.s3.S3StorageService;
import org.springframework.stereotype.Service;
import com.spotify.entity.Song;
import com.spotify.repository.SongRepository;

@Service
public class SongService {
    private final SongRepository songRepository;
    private final S3StorageService s3StorageService;

    public SongService(SongRepository songRepository, S3StorageService s3StorageService) {
        this.songRepository = songRepository;
        this.s3StorageService = s3StorageService;
    }

    public Song save(Song song) {
        return songRepository.save(song);
    }

    public Song getById(Long id) {
        return songRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found: " + id));
    }

    public List<Song> getAll() {
        return songRepository.findAll();
    }

    public void delete(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found: " + id));

        s3StorageService.deleteObject(song.getStorageKey());
        songRepository.delete(song);
    }

    public Song update(Long id, UpdateSongRequest request) {
        Song song =
                songRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("Song not found: " + id));

        song.setTitle(request.title());
        song.setStorageKey(request.storageKey());
        song.setContentType(request.contentType());
        song.setDurationSeconds(request.durationSeconds());

        return songRepository.save(song);
    }

    public Song patch(Long id, Map<String, Object> updates) {
        Song song =
                songRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("Song not found: " + id));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            switch (entry.getKey()) {
                case "title" -> {
                    if (!(entry.getValue() instanceof String value) || value.isBlank()) {
                        throw new IllegalArgumentException("title must be a non-empty string");
                    }
                    song.setTitle(value);
                }
                case "storageKey" -> {
                    if (!(entry.getValue() instanceof String value) || value.isBlank()) {
                        throw new IllegalArgumentException("storageKey must be a non-empty string");
                    }
                    song.setStorageKey(value);
                }
                case "contentType" -> {
                    if (!(entry.getValue() instanceof String value) || value.isBlank()) {
                        throw new IllegalArgumentException(
                                "contentType must be a non-empty string");
                    }
                    song.setContentType(value);
                }
                case "durationSeconds" -> {
                    Object value = entry.getValue();
                    if (value != null && !(value instanceof Number)) {
                        throw new IllegalArgumentException(
                                "durationSeconds must be numeric or null");
                    }
                    if (value instanceof Number number && number.longValue() < 0) {
                        throw new IllegalArgumentException("durationSeconds cannot be negative");
                    }
                    song.setDurationSeconds(value == null ? null : ((Number) value).intValue());
                }
                default ->
                        throw new IllegalArgumentException("Unsupported field: " + entry.getKey());
            }
        }

        return songRepository.save(song);
    }
}
