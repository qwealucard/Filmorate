package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private final FilmStorage filmStorage = new InMemoryFilmStorage();
    private final UserStorage userStorage = new InMemoryUserStorage();
    private final FilmService filmService = new FilmService(filmStorage, userStorage);
    private final FilmController filmController = new FilmController(filmService, filmStorage);

    @Test
    public void create_validFilm_shouldReturnCreatedFilm() {

        Film film = new Film(0L, "Интерстеллар", "Научный фильм", LocalDate.of(2006, 12, 20), 120L, 0L);
        film.setId(1L);
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120L);
        Film createdFilm = filmController.create(film);
        assertEquals(film, createdFilm);
        assertEquals(1, filmStorage.getFilms().size());
        assertEquals(film, filmStorage.getFilms().get(1L));
    }

    @Test
    public void update_validFilm_shouldReturnUpdatedFilm() {
        Film film = new Film(0L, "Интерстеллар", "Научный фильм", LocalDate.of(2006, 12, 20), 120L, 0L);
        film.setId(1L);
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120L);
        filmStorage.getFilms().put(film.getId(), film);
        Film updatedFilm = new Film(0L, "Интерстеллар", "Научный фильм", LocalDate.of(2006, 12, 20), 120L, 0L);
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film Title");
        updatedFilm.setDescription("Updated Film description");
        updatedFilm.setReleaseDate(LocalDate.of(2020, 12, 27));
        updatedFilm.setDuration(150L);
        Film resultFilm = filmController.update(updatedFilm);
        assertEquals(updatedFilm, resultFilm);
        assertEquals(updatedFilm, filmStorage.getFilms().get(1L));
    }

    @Test
    public void update_notFound_shouldThrowNotFoundException() {
        Film film = new Film(0L, "Интерстеллар", "Научный фильм", LocalDate.of(2006, 12, 20), 120L, 0L);
        film.setId(100L);
        film.setName("Film Title");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }
}
