package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import java.util.List;

@Slf4j
@Service
public class FilmLikeService {

    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;

    public FilmLikeService(@Qualifier("filmDbStorage") FilmStorage filmStorage, LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;

    }

    public void addLike(Integer userId, Integer filmId) {
        likeStorage.addLike(userId, filmId);
   }

    public void removeLike(Integer userId, Integer filmId) {
            likeStorage.removeLike(userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        try {
            return filmStorage.getPopularFilms(count);
        } catch (FilmNotFoundException e) {
            throw new FilmNotFoundException("Фильм не найден");
        }
    }
}
