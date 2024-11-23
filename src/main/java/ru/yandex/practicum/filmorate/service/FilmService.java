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
        if (!filmStorage.getFilms().containsKey(filmId)) {
            throw new NotFoundException("Такого фильма не существует");
        }
        if (!userStorage.getUsers().containsKey(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        }
        Set<Long> likes = userLikes.computeIfAbsent(filmId, k -> new HashSet<>());
        if (!likes.contains(userId)) {
            likes.add(userId);
            Long likeCount = filmStorage.getFilmById(filmId).getLikeCount();
            likeCount = likeCount + 1;
            filmStorage.getFilmById(filmId).setLikeCount(likeCount);
            log.info("Лайк поставлен");
        } else {
            log.error("Ошибка в установлении лайка");
            throw new DuplicateException("Пользователь уже поставил лайк");
        }
    }

    public void removeLike(Long userId, Long filmId) {
        Set<Long> likes = userLikes.get(filmId);
        if (likes != null && likes.contains(userId)) {
            likes.remove(userId);
            Long likeCount = filmStorage.getFilmById(filmId).getLikeCount();
            likeCount--;
            filmStorage.getFilmById(filmId).setLikeCount(likeCount);
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
}
