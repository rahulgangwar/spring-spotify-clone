package com.spotify.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.spotify.entity.Song;
import com.spotify.repository.SongRepository;

@Service
public class SongService {
  private final SongRepository songRepository;

  public SongService(SongRepository songRepository) {
    this.songRepository = songRepository;
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
}
