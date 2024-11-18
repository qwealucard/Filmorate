package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private final FilmController filmController = new FilmController();

    @Test
    public void create_validFilm_shouldReturnCreatedFilm() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        Film createdFilm = filmController.create(film);
        assertEquals(film, createdFilm);
        assertEquals(1, filmController.getFilms().size());
        assertEquals(film, filmController.getFilms().get(1L));
    }

    @Test
    public void update_validFilm_shouldReturnUpdatedFilm() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        filmController.getFilms().put(film.getId(), film);
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film Title");
        updatedFilm.setDescription("Updated Film description");
        updatedFilm.setReleaseDate(LocalDate.of(2020, 12, 27));
        updatedFilm.setDuration(150);
        Film resultFilm = filmController.update(updatedFilm);
        assertEquals(updatedFilm, resultFilm);
        assertEquals(updatedFilm, filmController.getFilms().get(1L));
    }

    @Test
    public void update_invalidId_shouldThrowValidationException() {
        Film film = new Film();
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        assertThrows(ValidationException.class, () -> filmController.update(film));
    }

    @Test
    public void update_notFound_shouldThrowNotFoundException() {
        Film film = new Film();
        film.setId(100L);
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }
}
