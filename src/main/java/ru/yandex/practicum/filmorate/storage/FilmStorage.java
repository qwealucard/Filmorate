package ru.yandex.practicum.filmorate.storage;

import jakarta.persistence.criteria.CriteriaBuilder;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> findAll();

    Optional<Film> getFilmById(Integer id);

    List<Film> getPopularFilms(Integer count);

    void deleteFilmById(Integer id);
}
