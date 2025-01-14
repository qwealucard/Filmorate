package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Collection<Film> findAll();

    Optional<Film> getFilmById(Integer id);

    List<Film> getPopularFilms(Integer count);

    List<Film> getCommonFilms(Integer userId, Integer friendId);
}
