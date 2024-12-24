package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

@Repository
@AllArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userMapper;

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String USER_QUERY = "SELECT COUNT(*) FROM users WHERE id = ? ";
        String CHECK_QUERY = "SELECT COUNT(*) FR0M friendship WHERE (user_id = ? AND friend_id = ?)" +
                "OR (user_id = ? AND friend_id = ?)";
        String INSERT_QUERY = "INSERT INTO friendship SET status = TRUE WHERE user_id = ? AND friend_id = ?";
        String UPDATE_QUERY = "UPDATE friendship SET status = TRUE WHERE user_id = ? AND friend_id = ?";
        String UPDATE_NEGATIVE_QUERY = "UPDATE friendship SET status = FALSE WHERE user_id = ? AND friend_id = ?";

        try {
            Integer userCount = jdbc.queryForObject(USER_QUERY, Integer.class, userId);
            if (userCount == null || userCount == 0) {
                throw new NotFoundException("Пользователь с id " + userId + " не найден");
            }
            Integer friendCount = jdbc.queryForObject(USER_QUERY, Integer.class, friendId);
            if (friendCount == null || friendCount == 0) {
                throw new NotFoundException("Пользователь с id " + friendId + " не найден");
            }

            int count = jdbc.queryForObject(CHECK_QUERY, Integer.class, userId, friendId, friendId, userId);

            if (count > 0) {
                return;
            }

            jdbc.update(INSERT_QUERY, userId, friendId, false);
            count = jdbc.queryForObject(CHECK_QUERY, Integer.class, friendId, userId, userId, friendId);
            if (count > 0) {
                jdbc.update(UPDATE_QUERY, userId, friendId);
                jdbc.update(UPDATE_NEGATIVE_QUERY, userId, friendId);
            }
        } catch (NotFoundException e) {
            throw new NotFoundException("Ошибка при добавлении друга");
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        String DELETE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        String CHECK_USER = "SELECT COUNT(*) FROM users WHERE id = ?";
        try {
            Integer userCount = jdbc.queryForObject(CHECK_USER, Integer.class, userId);
            if (userCount == null || userCount == 0) {
                throw new NotFoundException("Пользователь с id " + userId + " не найден");
            }
            Integer friendCount = jdbc.queryForObject(CHECK_USER, Integer.class, friendId);
            if (friendCount == null || friendCount == 0) {
                throw new NotFoundException("Пользователь с id " + friendId + " не найден");
            }
            jdbc.update(DELETE_FRIEND, userId, friendId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Ошибка при удалении друга");
        }
    }

    @Override
    public List<User> getAllFriends(Integer id) {
        String GET_FRIENDS = "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                "FROM users u " +
                "JOIN friendship f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";
        String CHECK_USER = "SELECT COUNT(*) FROM users WHERE id = ?";
        try {
            Integer userCount = jdbc.queryForObject(CHECK_USER, Integer.class, id);
            if (userCount == null || userCount == 0) {
                throw new NotFoundException("Пользователь с id " + id + " не найден");
            }
            return jdbc.query(GET_FRIENDS, userMapper, id);
        } catch (DataAccessException e) {
            System.out.println("Ошибка при получении списка друзей пользователя с id " + id + ": " + e.getMessage());
            return List.of();
        }
    }
}
