package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ReleaseDateException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {


    private Map<Long, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        log.info("Создание нового фильма");
        film.setLikeCount(0L);
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ReleaseDateException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм добавлен: {}", film);
        return film;
    }

    public Film update(Film film) {
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

    public Film getFilmById(long id) {
        return films.entrySet().stream()
                    .filter(entry -> entry.getKey() == id)
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                                 .stream()
                                 .mapToLong(id -> id)
                                 .peek(id -> log.info("ID сгенерирован: {}", id))
                                 .max()
                                 .orElse(0L) + 1;
        return currentMaxId;
    }
}
