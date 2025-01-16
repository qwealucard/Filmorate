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
        log.debug("Film ID: {}", film.getId());

        film.setName(resultSet.getString("name"));
        log.debug("Film Name: {}", film.getName());

        film.setDescription(resultSet.getString("description"));
        log.debug("Film Description: {}", film.getDescription());

        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        log.debug("Film Release Date: {}", film.getReleaseDate());

        film.setDuration(resultSet.getInt("duration"));
        log.debug("Film Duration: {}", film.getDuration());

        // MPA
        film.setMpa(new MPARating(
                resultSet.getInt("MPARating_id"),
                resultSet.getString("mpa_name")
        ));
        log.debug("MPA: {}", film.getMpa());

        // Genres
        log.debug("Processing genres...");
        Object[] genresArray = (Object[]) resultSet.getArray("genres").getArray();
        log.debug("Raw Genres Array: {}", Arrays.toString(genresArray));

        Set<Genre> genres = Arrays.stream(genresArray)
                .map(genre -> {
                    String[] parts = genre.toString().split(":");
                    log.debug("Parsed Genre: ID = {}, Name = {}", parts[0], parts[1]);
                    return new Genre(Integer.parseInt(parts[0]), parts[1]);
                })
                .collect(Collectors.toSet());
        film.setGenres(genres);
        log.debug("Genres: {}", genres);

        // Directors
//        log.debug("Processing directors...");
//        Object[] directorsArray = (Object[]) resultSet.getArray("directors").getArray();
//        log.debug("Raw Directors Array: {}", Arrays.toString(directorsArray));
//
//        List<Director> directors = Arrays.stream(directorsArray)
//                .map(director -> {
//                    String[] parts = director.toString().split(":");
//                    log.debug("Parsed Director: ID = {}, Name = {}", parts[0], parts[1]);
//                    return new Director(Integer.parseInt(parts[0]), parts[1]);
//                })
//                .collect(Collectors.toList());
//        film.setDirectors(directors);
//        log.debug("Directors: {}", directors);
//
        log.debug("Mapped Film: {}", film);
        return film;
    }
}