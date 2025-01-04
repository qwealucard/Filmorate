package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;
    private final FriendshipStorage friendshipStorage;
    private final LikeStorage likeStorage;
    private JdbcTemplate jdbc;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage, MPAStorage mpaStorage, GenreStorage genreStorage,
                       FriendshipStorage friendshipStorage, LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.likeStorage = likeStorage;
    }

    public void addLike(Integer userId, Integer filmId) {
        likeStorage.addLike(userId, filmId);
    }

    public void removeLike(Integer userId, Integer filmId) {
        likeStorage.removeLike(userId, filmId);
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id)
                          .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден."));
    }

    ;


    public List<Film> getTopFilms(int count) {
        try {
            return filmStorage.getPopularFilms(count);
        } catch (FilmNotFoundException e) {
            throw new FilmNotFoundException("Фильм не найден");
        }
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
