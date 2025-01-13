package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
@AllArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbc;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, RELEASE_DATE, duration, mpa) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(minReleaseDate)) {
                throw new ReleaseDateException("Ошибка при создании фильма, связанная с датой");
            }
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() == null) {
                ps.setObject(5, null);
            } else {
                validateMpaRating(film.getMpa());
                ps.setInt(5, film.getMpa().getId());
            }
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }
        if (film.getMpa() != null) {
            film.setMpa(getMpaRatingById(film.getMpa().getId()));
        }
        addGenreToFilm(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE id = ?";
        try {
            if (film.getMpa() != null) {
                validateMpaRating(film.getMpa());
            }
            jdbc.update(sql,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa() != null ? film.getMpa().getId() : null,
                    film.getId());
            if (film.getMpa() != null) {
                film.setMpa(getMpaRatingById(film.getMpa().getId()));
            }

            return film;
        } catch (RuntimeException e) {
            log.error("Ошибка при обновлении фильма " + film.getId() + ": " + e.getMessage());
            throw new UpdateFilmsException("Ошибка при обновлении фильма");
        }
    }

    public List<Film> findAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name " +
                "FROM films f LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new ArrayList<>(),
                rs.getObject("MPARating_id") != null ? new MPARating(
                        rs.getInt("MPARating_id"),
                        rs.getString("MPA_Rating_name")
                ) : null
        ));
        Map<Integer, List<Genre>> allFilmGenres = getAllFilmGenres();
        films.forEach(film -> film.setGenres(allFilmGenres.getOrDefault(film.getId(), List.of())));
        System.out.println("Размер списка фильмов" + films.size());
        return films;
    }

    private void validateMpaRating(MPARating mpaRating) {
        String sql = "SELECT COUNT(*) FROM MPA_Ratings WHERE MPARating_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mpaRating.getId());
        if (count == null || count == 0) {
            throw new MPAException("Ошибка с заполнением рейтинга");
        }
    }

    private void genreIsNotNull(Genre genre) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, genre.getId());
        if (count == null || count == 0) {
            throw new GenreException("Ошибка с заполнением жанра");
        }
    }

    private MPARating getMpaRatingById(Integer id) {
        String sql = "SELECT MPARating_id, MPA_Rating_name FROM MPA_Ratings WHERE MPARating_id = ?";
        return jdbc.queryForObject(sql, (rs, rowNum) -> new MPARating(
                rs.getInt("MPARating_id"),
                rs.getString("MPA_Rating_name")
        ), id);
    }

    private void addGenreToFilm(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Genre> genres = film.getGenres();
        List<Genre> genresToAdd = new ArrayList<>();
        for (Genre genre : genres) {
            genreIsNotNull(genre);
            if (!isGenreAlreadyAdded(film.getId(), genre.getId())) {
                genresToAdd.add(genre);
            }
        }
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = genres.get(i);
                ps.setInt(1, film.getId());
                ps.setInt(2, genre.getId());
                log.info("жанр добавлен");
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private boolean filmExists(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name " +
                "FROM films AS f " +
                "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id WHERE f.id = ?";
        try {
            Film film = jdbc.queryForObject(sql, (rs, rowNum) -> {
                return new Film(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("release_date").toLocalDate(),
                        rs.getInt("duration"),
                        new ArrayList<>(),
                        new MPARating(
                                rs.getInt("MPARating_id"),
                                rs.getString("MPA_Rating_name")
                        )
                );
            }, id);
            List<Genre> genres = getFilmGenresById(film.getId());
            if (genres != null) {
                film.setGenres(genres);
                log.info("жанр добавлен");
            } else {
                film.setGenres(new ArrayList<>());
            }
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка при поиске фильма по id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private List<Genre> getFilmGenresById(Integer filmId) {
        String sql = "SELECT g.genre_id, g.genre_name FROM film_genres AS fg LEFT JOIN genres AS g ON fg.genre_id = g.genre_id WHERE film_id = ?";
        List<Genre> genresList = jdbc.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name")
        ), filmId);
        Set<Genre> uniqueGenres = new HashSet<>(genresList);
        return new ArrayList<>(uniqueGenres);
    }

    private boolean isGenreAlreadyAdded(Integer filmId, Integer genreId) {
        String sql = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId, genreId);
        return count != null && count > 0;
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name, COUNT(l.user_id) as likesCount " +
                "FROM films f LEFT JOIN film_likes l ON f.id = l.film_id " +
                "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id " +
                "GROUP BY f.id ORDER BY likesCount DESC, f.id DESC LIMIT ?";
        return jdbc.query(sql, (rs, rowNum) -> {
            Film film = new Film(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new ArrayList<>(),
                    rs.getObject("mpa") != null ? new MPARating(
                            rs.getInt("MPARating_id"),
                            rs.getString("MPA_Rating_name")
                    ) : null
            );
            addGenreToFilm(film);
            return film;
        }, count);
    }

    private Map<Integer, List<Genre>> getAllFilmGenres() {
        String sql = "SELECT fg.film_id, g.genre_id, g.genre_name FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id";
        Map<Integer, List<Genre>> allFilmGenres = new HashMap<>();
        jdbc.query(sql, (rs, rowNum) -> {
            Integer filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
            allFilmGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            return null;
        });
        return allFilmGenres;
    }
}

