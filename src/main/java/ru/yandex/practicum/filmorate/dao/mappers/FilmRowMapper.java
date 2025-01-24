package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component("filmRowMapper")
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        // Извлечение MPA рейтинга, с учётом возможных null
        Integer mpaRatingId = resultSet.getObject("MPARating_id", Integer.class);
        String mpaRatingName = resultSet.getString("MPA_Rating_name");

        MPARating mpaRating = null;
        if (mpaRatingId != null) {
            mpaRating = new MPARating(mpaRatingId, mpaRatingName);
        }

        // Извлечение жанров (предполагается, что жанры возвращаются в формате "id:name,id:name")
        String genresData = resultSet.getString("genres");
        Set<Genre> genres = new HashSet<>();
        if (genresData != null && !genresData.isBlank()) {
            Arrays.stream(genresData.split(","))
                    .map(genre -> genre.split(":"))
                    .filter(parts -> parts.length == 2) // Убедиться, что формат корректный
                    .forEach(parts -> {
                        Integer genreId = Integer.parseInt(parts[0].trim());
                        String genreName = parts[1].trim();
                        genres.add(new Genre(genreId, genreName));
                    });
        }

        // Создание объекта Film
        return new Film(
                resultSet.getInt("id"),                      // ID фильма
                resultSet.getString("name"),                 // Название фильма
                resultSet.getString("description"),          // Описание фильма
                resultSet.getDate("release_date").toLocalDate(), // Дата выхода
                resultSet.getInt("duration"),                // Продолжительность
                genres,                                      // Жанры
                mpaRating,                                   // MPA рейтинг
                new HashSet<>()                            // Пустой список лайков (заполняется отдельно)
        );
    }
}