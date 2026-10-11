package com.spotify.service;

import com.spotify.entity.Song;
import com.spotify.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

  private final SongRepository songRepository;

  public SongService(SongRepository songRepository) {
    this.songRepository = songRepository;
  }

  public Song saveSong(Song song) {
    return songRepository.save(song);
  }

  public List<Song> getAllSongs() {
    return songRepository.findAll();
  }
}
