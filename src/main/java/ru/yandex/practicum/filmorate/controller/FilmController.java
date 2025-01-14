package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmDirectorsService;
import ru.yandex.practicum.filmorate.service.FilmLikeService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Getter
@AllArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final FilmLikeService filmLikeService;
    private final FilmDirectorsService filmDirectorsService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Received GET request to fetch all films");
        Collection<Film> films = filmService.findAll();
        log.info("Returning {} films", films.size());
        return films;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Received POST request to create film: {}", film);
        Film createdFilm = filmService.create(film);
        log.info("Film created successfully with ID: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Received PUT request to update film: {}", film);
        Film updatedFilm = filmService.update(film);
        log.info("Film updated successfully with ID: {}", updatedFilm.getId());
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Received PUT request to add like for film ID: {} by user ID: {}", id, userId);
        filmLikeService.addLike(id, userId);
        log.info("Like added successfully for film ID: {} by user ID: {}", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Received DELETE request to remove like for film ID: {} by user ID: {}", id, userId);
        filmLikeService.removeLike(id, userId);
        log.info("Like removed successfully for film ID: {} by user ID: {}", id, userId);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        log.info("Received GET request to fetch film by ID: {}", id);
        Film film = filmService.getFilmById(id);
        log.info("Returning film: {}", film);
        return film;
    }

//    @GetMapping("/popular")
//    @ResponseStatus(HttpStatus.OK)
//    public List<Film> getPopularFilms(@RequestParam int count) {
//        log.info("Received GET request to fetch top {} popular films", count);
//        List<Film> popularFilms = filmLikeService.getTopFilms(count);
//        log.info("Returning {} popular films", popularFilms.size());
//        return popularFilms;
//    }

    @GetMapping("/director/{directorId}")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getDirectorSort(@PathVariable Integer directorId, @RequestParam @NonNull String sortBy) {
        log.info("Received GET request to fetch films by director ID: {} sorted by: {}", directorId, sortBy);
        List<Film> films = filmDirectorsService.getDirectorSort(directorId, sortBy);
        log.info("Returning {} films for director ID: {}", films.size(), directorId);
        return films;
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getSearch(@RequestParam String query, @RequestParam @NonNull String by) {
        log.info("Received GET request to search films by query: '{}' and filter: '{}'", query, by);
        List<Film> films = filmService.getSearch(query, by);
        log.info("Returning {} search results for query: '{}'", films.size(), query);
        return films;
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "genreId", required = false) Integer genreId,
            @RequestParam(value = "year", required = false) Integer year) {

        log.info("Received GET request to fetch top {} popular films with genreId: {} and year: {}", count, genreId, year);

        // Проверка значения count
        if (count <= 0) {
            log.error("Invalid 'count' parameter: {}", count);
            throw new IllegalArgumentException("Parameter 'count' must be greater than 0.");
        }

        List<Film> popularFilms = filmService.getPopularFilms(count, genreId, year);
        log.info("Returning {} popular films with genreId: {} and year: {}", popularFilms.size(), genreId, year);
        return popularFilms;
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Integer id) {
        filmService.deleteFilmById(id);
    }

    @GetMapping("/common")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getCommonFilms(
            @RequestParam Integer userId,
            @RequestParam Integer friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}
