package ru.yandex.practicum.filmorate.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, DirectorDbStorage.class})
public class FilmTest {

    private final FilmDbStorage filmDbStorage;
    private final DirectorDbStorage directorDbStorage;

    @Test
    public void getDirectorSort() {

        Director director = new Director();
        director.setName("director");

        Director directorCreate = directorDbStorage.create(director);

        Film film1 = new Film();
        film1.setName("filmName1");
        film1.setDescription("Descr1");
        film1.setReleaseDate(LocalDate.of(2023, 12, 15));
        film1.setDuration(168);
        film1.setMpa(new MPARating(1, null));
        film1.setDirectors(List.of(directorCreate));

        Film film2 = new Film();
        film2.setName("filmName2");
        film2.setDescription("Descr2");
        film2.setReleaseDate(LocalDate.of(2019, 12, 15));
        film2.setDuration(168);
        film2.setMpa(new MPARating(1, null));
        film2.setDirectors(List.of(directorCreate));

        Film film3 = new Film();
        film3.setName("filmName3");
        film3.setDescription("Descr3");
        film3.setReleaseDate(LocalDate.of(2022, 12, 15));
        film3.setDuration(168);
        film3.setMpa(new MPARating(1, null));
        film3.setDirectors(List.of(directorCreate));

        filmDbStorage.create(film1);
        filmDbStorage.create(film2);
        filmDbStorage.create(film3);

        List<Film> filmsList = filmDbStorage.getDirectorSort(directorCreate.getId(), "year");

        assertEquals(filmsList.size(), 3);
        assertEquals(filmsList.get(0).getId(), 2);
    }

    @Test
    public void getSearchTest() {
        Director director = new Director();
        director.setName("director");

        Director directorCreate = directorDbStorage.create(director);

        Film film1 = new Film();
        film1.setName("filmName1");
        film1.setDescription("Descr1");
        film1.setReleaseDate(LocalDate.of(2023, 12, 15));
        film1.setDuration(168);
        film1.setMpa(new MPARating(1, null));
        film1.setDirectors(List.of(directorCreate));

        Film film2 = new Film();
        film2.setName("filmName2");
        film2.setDescription("Descr2");
        film2.setReleaseDate(LocalDate.of(2019, 12, 15));
        film2.setDuration(168);
        film2.setMpa(new MPARating(1, null));

        Film film3 = new Film();
        film3.setName("filmName3Dir");
        film3.setDescription("Descr3");
        film3.setReleaseDate(LocalDate.of(2022, 12, 15));
        film3.setDuration(168);
        film3.setMpa(new MPARating(1, null));

        filmDbStorage.create(film1);
        filmDbStorage.create(film2);
        filmDbStorage.create(film3);

        List<Film> filmsDirList = filmDbStorage.getSearch("DIR", "director");

        assertEquals(filmsDirList.size(), 1);
        assertEquals(filmsDirList.get(0).getName(), "filmName1");

        List<Film> filmsTitleList = filmDbStorage.getSearch("E2", "title");

        assertEquals(filmsTitleList.size(), 1);
        assertEquals(filmsTitleList.get(0).getName(), "filmName2");

        List<Film> filmsAllList = filmDbStorage.getSearch("diR", "title,director");

        assertEquals(filmsAllList.size(), 2);
    }
}
