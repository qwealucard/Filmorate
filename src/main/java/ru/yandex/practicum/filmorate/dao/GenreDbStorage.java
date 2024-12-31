package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private JdbcTemplate jdbc;

    @Override
    public Genre addGenre(Genre genre) {
        String sql = "INSERT INTO genres (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, genre.getName());
            return ps;
        }, keyHolder);
        return genre;
    }

    @Override
    public Optional<Genre> update(Genre genre) {
        String sql = "UPDATE genres SET name = ? WHERE id = ?";
        int rowsAffected;
        try {
            rowsAffected = jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, genre.getName());
                ps.setInt(2, genre.getId());
                return ps;
            });
        } catch (DataAccessException e) {
            System.out.println("Ошибка при обновлении жанра " + genre.getId() + ": " + e.getMessage());
            return Optional.empty();
        }
        if (rowsAffected > 0) {
            return Optional.of(genre);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Genre> findGenre(Integer id) {
        String sql = "SELECT genre_id, genre_name FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbc.queryForObject(sql, (rs, rowNum) ->
                    new Genre(rs.getInt("genre_id"), rs.getString("genre_name")), id);
            return Optional.of(genre);
        } catch (DataAccessException e) {
            System.err.println("Ошибка при поиске жанра по id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT genre_id, genre_name FROM genres";
        return jdbc.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name")
        ));
    }

    @Override
    public boolean deleteGenre(Integer id) {
        String sql = "DELETE FROM genres WHERE id = ?";
        int rowsAffected;
        try {
            rowsAffected = jdbc.update(sql, id);
        } catch (DataAccessException e) {
            System.out.println("Ошибка при удалении жанра по id " + id + ": " + e.getMessage());
            return false;
        }
        return rowsAffected > 0;
    }

    @Override
    public Genre getGenreById(Integer id) {
        return findGenre(id).orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}
