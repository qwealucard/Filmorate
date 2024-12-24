package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MPARatingMapper implements RowMapper<MPARating> {
    @Override
    public MPARating mapRow(ResultSet rs, int rowNum) throws SQLException {
        MPARating mpaRating = new MPARating();
        mpaRating.setId(rs.getInt("id"));
        mpaRating.setName(rs.getString("name"));
        return mpaRating;
    }
}