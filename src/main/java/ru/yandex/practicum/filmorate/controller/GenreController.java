package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/genres")
@Validated
@AllArgsConstructor
public class GenreController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Genre> addGenre(@Valid @RequestBody Genre genre) {
        Genre addedGenre = filmService.addGenre(genre);
        return new ResponseEntity<>(addedGenre, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Genre> updateGenre(@Valid @RequestBody Genre genre) {
        Optional<Genre> updatedGenre = filmService.updateGenre(genre);
        return updatedGenre.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                           .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable Integer id) {
        try {
            Genre genre = filmService.getGenreById(id);
            return new ResponseEntity<>(genre, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteGenreById(@PathVariable Integer id) {
        boolean deleted = filmService.deleteGenreById(id);
        return deleted ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        List<Genre> genres = filmService.getAllGenres();
        return new ResponseEntity<>(genres, HttpStatus.OK);
    }
}

