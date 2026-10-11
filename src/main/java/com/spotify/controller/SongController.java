package com.spotify.controller;

import com.spotify.entity.Song;
import com.spotify.service.SongService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

  private final SongService songService;

  public SongController(SongService songService) {
    this.songService = songService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Song create(@RequestBody Song song) {
    return songService.saveSong(song);
  }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Song> getAll() {
        return songService.getAllSongs();
    }
}
