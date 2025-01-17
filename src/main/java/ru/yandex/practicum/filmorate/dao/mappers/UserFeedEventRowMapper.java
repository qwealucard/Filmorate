package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class UserFeedEventRowMapper implements RowMapper<UserFeedEvent> {
    @Override
    public UserFeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserFeedEvent(
                rs.getInt("event_id"),
                rs.getInt("user_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                rs.getInt("entity_id"),
                rs.getLong("timestamp")
        );
    }
}
