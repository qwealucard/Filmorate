package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

        // Создание объекта Film
        return new Film(
                resultSet.getInt("id"),                      // ID фильма
                resultSet.getString("name"),                 // Название фильма
                resultSet.getString("description"),          // Описание фильма
                resultSet.getDate("release_date").toLocalDate(), // Дата выхода
                resultSet.getInt("duration"),                // Продолжительность
                new ArrayList<>(),                           // Пустой список жанров (заполняется отдельно)
                mpaRating,                                   // MPA рейтинг
                new ArrayList<>()                            // Пустой список лайков (заполняется отдельно)
        );
    }
}
