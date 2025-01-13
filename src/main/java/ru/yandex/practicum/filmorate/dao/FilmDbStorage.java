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
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exceptions.GenreException;
import ru.yandex.practicum.filmorate.exceptions.MPAException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ReleaseDateException;
import ru.yandex.practicum.filmorate.exceptions.UpdateFilmsException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        if (film.getDirectors() != null) {
            addDirectorToFilm(film);
        }
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
            if (film.getDirectors() != null) {
                addDirectorToFilm(film);
            }

            return film;
        } catch (RuntimeException e) {
            log.error("Ошибка при обновлении фильма " + film.getId() + ": " + e.getMessage());
            throw new UpdateFilmsException("Ошибка при обновлении фильма");
        }
    }

    public Collection<Film> findAll() {
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
                ) : null,
                new ArrayList<>()
        ));
        Map<Integer, List<Genre>> allFilmGenres = getAllFilmGenres();
        films.forEach(film -> film.setGenres(allFilmGenres.getOrDefault(film.getId(), List.of())));
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

    private void addDirectorToFilm(Film film) {
        try {
            String sql = "merge into film_directors (film_id, directors_id) values (?, ?)";

            film.getDirectors().forEach(
                    director -> jdbc.update(
                            sql,
                            film.getId(), director.getId())
            );
        } catch (RuntimeException e) {
            throw new RuntimeException("Ошибка при добавлении режиссеров к фильму: " + e);
        }
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
                        ),
                        new ArrayList<>()
                );
            }, id);
            List<Genre> genres = getFilmGenresById(film.getId());
            if (genres != null) {
                film.setGenres(genres);
                log.info("жанр добавлен");
            } else {
                film.setGenres(new ArrayList<>());
            }
            List<Director> directors = getFilmDirectorsById(film.getId());
            if (!directors.isEmpty()) {
                film.setDirectors(directors);
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

    private List<Director> getFilmDirectorsById(Integer filmId) {
        String sql = "SELECT d.id, d.name FROM directors d JOIN film_directors fd on d.id = fd.directors_id WHERE fd.film_id = ?";
        return jdbc.query(sql, new DirectorRowMapper(), filmId);
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
                    ) : null,
                    new ArrayList<>()
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

    @Override
    public List<Film> getDirectorSort(Integer directorId, String sortBy) {

        List<Film> films = new ArrayList<>();
        if (sortBy.equals("year")) {
            films.addAll(getFilmSortYear(directorId));
        } else {
            films.addAll(getFilmSortLike(directorId));
        }
        films.forEach(f -> {
            List<Director> directors = getFilmDirectorsById(f.getId());
            if (!directors.isEmpty()) {
                f.setDirectors(directors);
            }
        });
        return films;
    }

    @Override
    public List<Film> getSearch(String query, String by) {

        Set<Film> set = new HashSet<>();

        if (by.contains("director")) {
            set.addAll(getSearchBy("%" + query + "%", "director"));
        }
        if (by.contains("title")) {
            set.addAll(getSearchBy("%" + query + "%", "title"));
        }

        return new ArrayList<>(set);
    }

    private List<Film> getSearchBy(String query, String by) {
        String sql = "WITH res AS (SELECT COUNT(user_id) likes, film_id FROM film_likes GROUP BY film_id)\n" +
                "SELECT f.*, m.MPARating_id, m.MPA_Rating_name FROM films f\n" +
                "LEFT JOIN film_directors fd ON f.id = fd.film_id \n" +
                "LEFT JOIN directors d ON d.id = fd.directors_id \n" +
                "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id \n" +
                "LEFT JOIN res AS r ON r.film_id = f.id\n" +
                "WHERE CASE WHEN ? = 'director' THEN UPPER(d.name) LIKE ? ELSE UPPER(f.name) LIKE ? END ORDER BY r.likes DESC";
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
                    ) : null,
                    new ArrayList<>()
            );
            film.setDirectors(getDirectorByIdFilm(film.getId()));
            film.setGenres(getGenresByIdFilm(film.getId()));
            return film;
        }, by, query.toUpperCase(), query.toUpperCase());
    }

    private List<Director> getDirectorByIdFilm(Integer idFilm) {
        String sql = "SELECT d.* FROM directors d JOIN film_directors fd ON fd.directors_id = d.id WHERE fd.film_id = ?";
        return jdbc.query(sql, new DirectorRowMapper(), idFilm);
    }

    private List<Genre> getGenresByIdFilm(Integer idFilm) {
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        return jdbc.query(sql, new GenreRowMapper(), idFilm);
    }

    private List<Film> getFilmSortYear(Integer directorId) {
        String sql = "SELECT f.* FROM films f JOIN film_directors fd ON f.id = fd.film_id WHERE fd.directors_id = ? ORDER BY release_date";
        return jdbc.query(sql, (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new ArrayList<>(),
                null,
                new ArrayList<>()
        ), directorId);
    }

    private List<Film> getFilmSortLike(Integer directorId) {
        String sql = "WITH res AS (SELECT COUNT(user_id) likes, film_id FROM film_likes GROUP BY film_id)\n" +
                "SELECT f.* FROM films f JOIN film_directors fd ON f.id = fd.film_id LEFT JOIN res AS r ON r.FILM_ID = f.ID " +
                "WHERE fd.directors_id = ? ORDER BY likes DESC";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new ArrayList<>(),
                null,
                new ArrayList<>()
        ), directorId);
        return films;
    }
}

