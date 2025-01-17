package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserFeedEventDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class UserService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final LikeStorage likeStorage;
    private final UserFeedEventDbStorage userFeedEventStorage;

    public UserService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("FriendshipDbStorage") FriendshipStorage friendshipStorage, LikeStorage likeStorage,
                       UserFeedEventDbStorage userFeedEventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.likeStorage = likeStorage;
        this.userFeedEventStorage = userFeedEventStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        friendshipStorage.addFriend(userId, friendId);

        addUserEvent(userId, "FRIEND", "ADD", friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getUserById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        friendshipStorage.removeFriend(userId, friendId);
        log.info("Друг удален из списка друзей пользователя");
        log.info("Пользователь удален из списка друзей друга");

        addUserEvent(userId, "FRIEND", "REMOVE", friendId);
    }

    private void addUserEvent(Integer userId, String eventType, String operation, Integer entityId) {
        log.info("Создание события типа \"{}\" для операции \"{}\", для пользователя с id = {}", eventType, operation, userId);

        UserFeedEvent event = new UserFeedEvent(
                0, // eventId будет сгенерирован базой данных
                userId,
                eventType,
                operation,
                entityId,
                Instant.now().toEpochMilli()
        );
        userFeedEventStorage.addUserEvent(event);
        log.info("Событие типа \"{}\" для операции \"{}\" для пользователя с id = {} внесено в БД", eventType, operation, userId);
    }

    public List<User> checkFriends(Integer userId) {
        Set<User> friendList = new HashSet<>();
        for (Integer friendId : userStorage.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден")).getFriendList()) {
            User friend = userStorage.getUserById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
            friendList.add(friend);
        }
        return friendshipStorage.getAllFriends(userId);

    }

    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        return friendshipStorage.getCommonFriends(userId1, userId2);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void deleteById(Integer id) {
        userStorage.deleteUserById(id);
    }

    public Optional<User> getUserById(Integer id) {
        return userStorage.getUserById(id);
    }
}

