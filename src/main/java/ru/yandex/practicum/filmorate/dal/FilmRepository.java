package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_NAME_QUERY = "SELECT * FROM films WHERE name = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, like_count, genre," +
            "mpa_rating)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?) returning id";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
            "duration, genre, mpa_rating WHERE id = ?";

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Film> findByName(String name) {
        return findOne(FIND_BY_NAME_QUERY, name);
    }

    public Optional<Film> findById(long filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    public Film save(Film film) {
        Integer id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating()
        );
        film.setId(id);
        return film;
    }

    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating()
        );
        return film;
    }
}
