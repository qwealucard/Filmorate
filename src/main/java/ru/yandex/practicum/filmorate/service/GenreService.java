package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
public class GenreService {

    private final GenreDbStorage genreStorage;

    public GenreService(@Qualifier("genreStorage") GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre addGenre(Genre genre) {
        return genreStorage.addGenre(genre);
    }

    public Genre updateGenre(Genre genre) {
        Genre update = genreStorage.update(genre);
        if (update == null) {
            throw new GenreNotFoundException("Жанр с id " + genre.getId() + " не найден");
        }
        return update;
    }

    public Genre getGenreById(Integer id) {
        try {
            return genreStorage.getGenreById(id);
        } catch (GenreNotFoundException e) {
            throw new GenreNotFoundException("Жанр не найден");
        }
    }

    public List<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public boolean deleteGenreById(Integer id) {
        try {
            return genreStorage.deleteGenre(id);
        } catch (GenreNotFoundException e) {
            throw new GenreNotFoundException("Жанр не найден");
        }
    }
}
