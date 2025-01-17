package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserFeedEventRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserFeedEventDbStorage {

    private final JdbcTemplate jdbc;

    public List<UserFeedEvent> getUserFeed(int userId) {
        String sql = "SELECT * FROM user_feed WHERE user_id = ? ORDER BY timestamp ASC";

        return jdbc.query(sql, new UserFeedEventRowMapper(), userId);

//        try {
//            return jdbc.query(sql, new UserFeedEventRowMapper(), userId);
//        } catch (DataAccessException e) {
//            log.error("Ошибка при поиске пользователя с id:" + userId + ": " + e.getMessage());
//            throw new NotFoundException("Пользователь не найден");
//        }
    }



    public void addUserEvent(UserFeedEvent event) {
        String sql = "INSERT INTO user_feed (user_id, event_type, operation, entity_id, timestamp) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, event.getUserId());
            ps.setString(2, event.getEventType());
            ps.setString(3, event.getOperation());
            ps.setInt(4, event.getEntityId());
            ps.setLong(5, event.getTimestamp());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            event.setEventId(keyHolder.getKey().intValue());
        }
    }
}
