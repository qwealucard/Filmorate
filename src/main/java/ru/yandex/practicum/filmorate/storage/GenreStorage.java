package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> findAll();

    Genre update(Genre genre);

    Genre addGenre(Genre genre);

    Optional<Genre> findGenre(Integer id);

    boolean deleteGenre(Integer id);

    Genre getGenreById(Integer id);
}
