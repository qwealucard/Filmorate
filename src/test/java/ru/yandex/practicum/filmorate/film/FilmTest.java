package ru.yandex.practicum.filmorate.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, DirectorDbStorage.class, FilmRowMapper.class})
public class FilmTest {

    private final FilmDbStorage filmDbStorage;
    private final DirectorDbStorage directorDbStorage;

    @Test
    public void getDirectorSort() {
        Director director = createDirector("Director Name");

        Film film1 = createFilm("Film 1", "Description 1", LocalDate.of(2023, 12, 15), Set.of(director));
        Film film2 = createFilm("Film 2", "Description 2", LocalDate.of(2019, 12, 15), Set.of(director));
        Film film3 = createFilm("Film 3", "Description 3", LocalDate.of(2022, 12, 15), Set.of(director));

        // Сохраняем фильмы
        filmDbStorage.create(film1);
        filmDbStorage.create(film2);
        filmDbStorage.create(film3);

        // Получаем фильмы, отсортированные по году
        List<Film> filmsList = filmDbStorage.getDirectorSort(director.getId(), "year");

        // Проверяем размер и порядок сортировки
        assertEquals(3, filmsList.size());
        assertEquals(film2.getName(), filmsList.get(0).getName());
        assertEquals(film3.getName(), filmsList.get(1).getName());
        assertEquals(film1.getName(), filmsList.get(2).getName());

        // Проверяем, что режиссер корректно установлен
        filmsList.forEach(film -> assertEquals(Set.of(director), film.getDirectors()));
    }

    @Test
    public void getSearchTest() {
        Director director = createDirector("Search Director");

        Film film1 = createFilm("FilmName1", "Description 1", LocalDate.of(2023, 12, 15), Set.of(director));
        Film film2 = createFilm("FilmName2", "Description 2", LocalDate.of(2019, 12, 15), new HashSet<>());
        Film film3 = createFilm("FilmName3Dir", "Description 3", LocalDate.of(2022, 12, 15), new HashSet<>());

        // Сохраняем фильмы
        filmDbStorage.create(film1);
        filmDbStorage.create(film2);
        filmDbStorage.create(film3);

        // Поиск по режиссеру
        List<Film> filmsDirList = filmDbStorage.getSearch("Search", "director");
        assertEquals(1, filmsDirList.size());
        assertEquals("FilmName1", filmsDirList.get(0).getName());

        // Поиск по названию
        List<Film> filmsTitleList = filmDbStorage.getSearch("Name2", "title");
        assertEquals(1, filmsTitleList.size());
        assertEquals("FilmName2", filmsTitleList.get(0).getName());

        // Поиск по названию и режиссеру
        List<Film> filmsAllList = filmDbStorage.getSearch("diR", "title,director");
        assertEquals(2, filmsAllList.size());
    }

    private Director createDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorDbStorage.create(director);
    }

    private Film createFilm(String name, String description, LocalDate releaseDate, Set<Director> directors) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(120);
        film.setMpa(new MPARating(1, "G"));
        film.setDirectors(directors);
        return film;
    }
}