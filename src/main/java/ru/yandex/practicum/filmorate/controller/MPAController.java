package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.service.MPARatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Validated
@AllArgsConstructor
public class MPAController {
    private final MPARatingService mpaRatingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MPARating addMPARating(@Valid @RequestBody MPARating mpaRating) {
        return mpaRatingService.addMPARating(mpaRating);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MPARating getMPARatingById(@PathVariable Integer id) {
        return mpaRatingService.getMPARatingById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MPARating> getAllMPARatings() {
        return mpaRatingService.getAllMPARatings();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMPARatingById(@PathVariable Integer id) {
        mpaRatingService.deleteMPARatingById(id);
    }
}