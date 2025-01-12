package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
public class FilmDirectorsService {

    private final FilmStorage filmStorage;

    public FilmDirectorsService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getDirectorSort(Integer directorId, String sortBy) {
        if (directorId == null) {
            throw new ValidationException("Укажите ид режиссера для сортировки");
        }
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new ValidationException("Критерий сортировки указан не верно");
        }
        return filmStorage.getDirectorSort(directorId, sortBy);
    }
}
