package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmWithGenresAndDirectorsRowMapper")
@Primary
@Slf4j
public class FilmWithGenresAndDirectorsRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Mapping row number: {}", rowNum);

        // Создаем объект Film
        Film film = new Film();
        film.setId(resultSet.getInt("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        // MPA
        film.setMpa(new MPARating(
                resultSet.getInt("MPARating_id"),
                resultSet.getString("mpa_name")
        ));
        // Genres
        Object[] genresArray = (Object[]) resultSet.getArray("genres").getArray();

        Set<Genre> genres = Arrays.stream(genresArray)
                .map(genre -> {
                    String[] parts = genre.toString().split(":");
                    log.debug("Parsed Genre: ID = {}, Name = {}", parts[0], parts[1]);
                    return new Genre(Integer.parseInt(parts[0]), parts[1]);
                })
                .collect(Collectors.toSet());
        film.setGenres(genres);
        log.debug("Genres: {}", genres);
        log.debug("Mapped Film: {}", film);
        return film;
    }
}