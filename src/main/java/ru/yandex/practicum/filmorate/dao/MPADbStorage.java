package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.MPAStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class MPADbStorage implements MPAStorage {
    private JdbcTemplate jdbc;

    @Override
    public MPARating addRating(MPARating mpaRating) {
        String sql = "INSERT INTO MPA_Ratings (MPA_Rating_name) VALUES (?)"; ///??? (name)
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, mpaRating.getName());
            return ps;
        }, keyHolder);
        return mpaRating;
    }

    @Override
    public MPARating updateRating(MPARating mpaRating) {
        String sql = "UPDATE mpa_ratings SET name = ? WHERE id = ?";
        int rowsAffected;
        try {
            rowsAffected = jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, mpaRating.getName());
                ps.setInt(2, mpaRating.getId());
                return ps;

            });
            if (rowsAffected > 0) {
                return mpaRating;
            }
        } catch (DataAccessException e) {
            System.out.println("Ошибка при обновлении рейтинга " + mpaRating.getId() + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public MPARating findRatingById(Integer id) {
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
        try {
            MPARating mpaRating = jdbc.queryForObject(sql, (rs, rowNum) -> {
                MPARating mpaRating1 = new MPARating();
                mpaRating1.setId(rs.getInt("id"));
                mpaRating1.setName(rs.getString("name"));
                return mpaRating1;
            }, id);
            return mpaRating;
        } catch (DataAccessException e) {
            System.out.println("Ошибка при поиске рейтинга по id " + id + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<MPARating> findAll() {
        String sql = "SELECT id, name FROM mpa_ratings";
        try {
            return jdbc.query(sql, (rs, rowNum) -> {
                MPARating mpaRating = new MPARating();
                mpaRating.setId(rs.getInt("id"));
                mpaRating.setName(rs.getString("name"));
                return mpaRating;
            });
        } catch (DataAccessException e) {
            System.out.println("Ошибка при получении всех рейтингов: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Integer deleteRating(Integer id) {
        String sql = "DELETE FROM mpa_ratings WHERE id = ?";
        int rowsAffected;
            rowsAffected = jdbc.update(sql, id);
        return rowsAffected;
    }
}
