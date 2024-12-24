package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

@Repository
@AllArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void addLike(Integer userId, Integer filmId) {
        if (!hasUserLikedFilm(filmId, userId)) {
            String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            try {
                jdbc.update(sql, filmId, userId);
                updateLikeCount(filmId, 1);
            } catch (DataAccessException e) {
                System.out.println("Ошибка при добавлении лайка фильму с id " + filmId + " от пользователя с id " + userId + ": " + e.getMessage());
            }
        } else {
            System.out.println("Пользователь с id " + userId + " уже поставил лайк фильму с id " + filmId);
        }
    }

    @Override
    public void removeLike(Integer userId, Integer filmId) {
        if (hasUserLikedFilm(userId, filmId)) {
            String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
            try {
                int rowsAffected = jdbc.update(sql, filmId, userId);
                if (rowsAffected > 0) {
                    updateLikeCount(filmId, -1);
                }
            } catch (DataAccessException e) {
                System.out.println("Ошибка при удалении лайка у фильма с id " + filmId + " от пользователя с id " + userId + ": " + e.getMessage());
            }
        } else {
            System.out.println("Пользователь с id " + userId + " не ставил лайк фильму с id " + filmId);
        }

    }

    private void updateLikeCount(Integer filmId, int increment) {
        String sql = "UPDATE films SET like_count = COALESCE(like_count, 0) + ? WHERE id = ?";
        try {
            jdbc.update(sql, increment, filmId);
        } catch (DataAccessException e) {
            System.out.println("Ошибка при обновлении кол-ва лайков у фильма с id " + filmId + ": " + e.getMessage());
        }
    }

    private boolean hasUserLikedFilm(Integer userId, Integer filmId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        try {
            Integer count = jdbc.queryForObject(sql, Integer.class, filmId, userId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            System.out.println("Ошибка при проверке лайка у фильма с id " + filmId + " от пользователя с id " + userId + ": " + e.getMessage());
            return false;
        }
    }
}