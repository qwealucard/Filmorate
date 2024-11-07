package ru.yandex.practicum.filmorate.controller;

import lombok.Data;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@Data
@RequestMapping("/films")
public class FilmController {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(FilmController.class);
    private final HashMap<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Создание нового фильма");
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма должно быть указано");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма должно не превышать 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.info("Обновление фильма с id: {}", film.getId());
        Film existingFilm = films.get(film.getId());
        if (existingFilm != null) {
            if (existingFilm.getName() != null) {
                existingFilm.setName(film.getName());
            }
            if (existingFilm.getDuration() != null) {
                existingFilm.setDuration(film.getDuration());
            }
            if (existingFilm.getDescription() != null) {
                existingFilm.setDescription(film.getDescription());
            }
            if (existingFilm.getReleaseDate() != null) {
                existingFilm.setReleaseDate(film.getReleaseDate());
            }
            films.put(film.getId(), existingFilm);
            log.info("Фильм с id {} обновлен: {}", film.getId(), film);
            return existingFilm;
        }
        log.error("Ошибка валидации при обновлении фильма");
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    public long getNextId() {
        long currentMaxId = films.keySet()
                                 .stream()
                                 .mapToLong(id -> id)
                                 .max()
                                 .orElse(0L);
        return currentMaxId + 1;
    }
}
