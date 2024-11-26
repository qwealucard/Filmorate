package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DuplicateException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    public final FilmStorage filmStorage;
    public final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private Map<Long, Set<Long>> userLikes = new HashMap<>();

    public void addLike(Long userId, Long filmId) {
        Film film = filmStorage.getFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден"));
        if (!filmStorage.exists(filmId)) {
            log.error("Ошибка при нахождении фильма для добавлении лайка");
            throw new NotFoundException("Такого фильма не существует");
        }
        if (!userStorage.exists(userId)) {
            log.error("Ошибка при нахождении пользователя для добавления лайка");
            throw new NotFoundException("Такого пользователя нет");
        }
        Set<Long> likes = userLikes.computeIfAbsent(filmId, k -> new HashSet<>());
        if (!likes.contains(userId)) {
            likes.add(userId);
            film.addLike(film);
            log.info("Лайк поставлен");
        } else {
            log.error("Ошибка в установлении лайка");
            throw new DuplicateException("Пользователь уже поставил лайк");
        }
    }

    public void removeLike(Long userId, Long filmId) {
        Film film = filmStorage.getFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден"));
        Set<Long> likes = userLikes.get(filmId);
        if (likes != null && likes.contains(userId)) {
            likes.remove(userId);
            film.removeLike(film);
            log.info("Лайк удален");
        } else {
            log.error("Ошибка с удалением лайка");
            throw new NotFoundException("Пользователь не ставил лайк");
        }
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.findAll().stream()
                          .sorted(Comparator.comparing(Film::getLikeCount).reversed())
                          .limit(count)
                          .collect(Collectors.toList());
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }
}
