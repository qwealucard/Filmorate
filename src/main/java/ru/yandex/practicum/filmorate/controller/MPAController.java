package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Validated
@AllArgsConstructor
public class MPAController {
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<MPARating> addMPARating(@Valid @RequestBody MPARating mpaRating) {
        MPARating addedRating = filmService.addMPARating(mpaRating);
        return new ResponseEntity<>(addedRating, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MPARating> getMPARatingById(@PathVariable Integer id) {
        try {
            MPARating mpaRating = filmService.getMPARatingById(id);
            return new ResponseEntity<>(mpaRating, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<MPARating>> getAllMPARatings() {
        List<MPARating> mpaRatings = filmService.getAllMPARatings();
        return new ResponseEntity<>(mpaRatings, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMPARatingById(@PathVariable Integer id) {
        try {
            filmService.deleteMPARatingById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}