package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.dao.mappers.MPARatingMapper;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MPAStorage;

import java.sql.PreparedStatement;
import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@AllArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbc;
    private final MPARatingMapper mpaRatingMapper;
    private final GenreMapper genreMapper;

    @Override
    public Film create(Film film) {

        String sql = "INSERT INTO films (name, description, RELEASE_DATE, duration) VALUES (?, ?, ?, ?)";
        //MpaIsNotNull(film.getMpaRating());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            //ps.setInt(5, film.getMpaRating().getId());
            return ps;
        }, keyHolder);
        if(keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }
      /*  else {
            throw new ValidationException("Ошибка добавления в базу");
        }*/
        //film.setId(Objects.requireNonNull(keyHolder.getKey().intValue()));
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, MPA_Rating, WHERE id = ?";
        int rowsAffected;
        try {
            rowsAffected = jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getMpaRating().getId());
                ps.setInt(6, film.getId());
                return ps;
            });
        } catch (DataAccessException e) {
            System.out.println("Ошибка при обновлении фильма " + film.getId() + ": " + e.getMessage());
            return Optional.empty();
        }
        if (rowsAffected > 0) {
            return Optional.of(film);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = "SELECT id, name, description, release_date, duration, mpa_rating FROM films WHERE id = ?";
        try {
            Film film = jdbc.queryForObject(sql, (rs, rowNum) -> {
                Film film1 = new Film();
                film1.setId(rs.getInt("id"));
                film1.setName(rs.getString("name"));
                film1.setDescription(rs.getString("description"));
                film1.setReleaseDate(rs.getDate("release_date").toLocalDate());
                film1.setDuration(rs.getInt("duration"));
                MPARating mpaRating = mpaRatingMapper.mapRow(rs, rowNum);
                film1.setMpaRating(mpaRating);
                return film1;
            }, id);
            return Optional.of(film);
        } catch (DataAccessException e) {
            System.out.println("Ошибка при поиске фильма по id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public Collection<Film> findAll() {
        String sql = "SELECT id, name, description, release_date, duration, mpa_rating FROM films";
        try {
            return jdbc.query(sql, (rs, rowNum) -> {
                Film film = new Film();
                film.setId(rs.getInt("id"));
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                film.setDuration(rs.getInt("duration"));
                MPARating mpaRating = mpaRatingMapper.mapRow(rs, rowNum);
                film.setMpaRating(mpaRating);
                return film;
            });
        } catch (DataAccessException e) {
            System.out.println("Ошибка при получении всех фильмов: " + e.getMessage());
            return List.of();
        }
    }

    private void MpaIsNotNull(MPARating mpaRating) {
        if (mpaRating != null) {
            String sql = "SELECT COUNT(*) FROM MPA_Rating WHERE MPARating_id = ?";
            Integer count = jdbc.queryForObject(sql, Integer.class, mpaRating.getId());
            if (count == null || count == 0) {
                throw new ValidationException("Mpa равен 0");
            }
        }
        else {
            throw new ValidationException("mpa равен null");
        }
    }
    private void GenreIsNotNull(Genre genre) {
        if(genre != null) {
            String sql = "SELECT COUNT(*) FROM genre WHERE genre_id = ?";
            Integer count = jdbc.queryForObject(sql, Integer.class, genre.getId());
            if(count == null || count == 0) {
                throw new ValidationException("Жанр равен null");
            }
        }
        else {
            throw new ValidationException("Жанр равен null");
        }
    }
}

