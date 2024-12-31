package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ReleaseDateException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Getter
@Slf4j
@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private Map<Integer, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        log.info("Создание нового фильма");

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка при вводе даты");
            throw new ReleaseDateException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм добавлен: {}", film);
        return film;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.error("Ошибка при нахождении фильма для обновления");
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

    public Optional<Film> getFilmById(Integer id) {
        Film film = films.get(id);
        return Optional.ofNullable(film);
    }


    private Integer getNextId() {
        Integer currentMaxId = films.keySet()
                                    .stream()
                                    .mapToInt(id -> id)
                                    .peek(id -> log.info("ID сгенерирован: {}", id))
                                    .max()
                                    .orElse(0) + 1;
        return currentMaxId;
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return new ArrayList<>();
    }
}
