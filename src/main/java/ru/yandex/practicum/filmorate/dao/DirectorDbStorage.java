package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exceptions.DirectorsException;
import ru.yandex.practicum.filmorate.exceptions.DuplicateException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> findAll() {
        String sqlQuery = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlQuery, new DirectorRowMapper());
    }

    @Override
    public Director findById(Integer id) {
        String sqlQuery = "SELECT * FROM directors WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, new DirectorRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссер по ид : " + id + " не найден. Ошибка:" + e);
        }
    }

    @Override
    public Director create(Director director) {

        try {
            String selectQuery = "SELECT id FROM directors WHERE name = ?";
            List<Integer> idDirectors = jdbcTemplate.queryForList(selectQuery, Integer.class, director.getName());

            if (!idDirectors.isEmpty()) {
                director.setId(idDirectors.get(0));
                log.debug("Режиссер с именем " + director.getName() + " уже существует с ID: {}", director.getId());
                throw new DuplicateException("Ошибка с дублированием director");
            }

            String sqlQuery = "INSERT INTO directors (name) VALUES (?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"ID"});
                stmt.setString(1, director.getName());
                return stmt;
            }, keyHolder);
            director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        } catch (DirectorsException e) {
            throw new DirectorsException("ошибка сохранения режиссера " + e);
        }
        log.debug("Режиссер успешно создан");
        return director;
    }

    @Override
    public Director update(Director director) {
        try {
            String sqlQuery = "UPDATE directors SET name = ? WHERE id = ?";
            jdbcTemplate.update(
                    sqlQuery,
                    director.getName(), director.getId());

            log.debug("Режиссер успешно изменен");
            return director;
        } catch (DirectorsException e) {
            throw new DirectorsException("Ошибка при изменении режиссера: " + e);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            String sqlQuery = "DELETE FROM directors WHERE id = ?";
            jdbcTemplate.update(
                    sqlQuery,
                    id);

            log.debug("Режиссер успешно удален");
        } catch (DirectorsException e) {
            throw new DirectorsException("Ошибка при удалении режиссера: " + e);
        }
    }
}
