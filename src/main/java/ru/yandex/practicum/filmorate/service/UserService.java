package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DuplicateException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getUserById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!userStorage.exists(userId)) {
            log.error("Ошибка нахождения пользователя для добавления друга");
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userStorage.exists(friendId)) {
            log.error("Ошибка нахождения друга для добавления его в друзья к пользователю");
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден.");
        }
        if (user.getFriendList().contains(friendId)) {
            log.error("Ошибка при повторном добавлении друга");
            throw new DuplicateException("Этот пользователь уже есть в списке ваших друзей");
        }
        if (userId.equals(friendId)) {
            log.error("Ошибка при добавлении себя в друзья");
            throw new DuplicateException("Вы не можете добавить себя в друзья");
        }
        user.addFriend(friend);
        friend.addFriend(user);
        log.info("Добавлен друг в список друзей пользователя");
        log.info("Добавлен пользователь в список друзей друга");
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getUserById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        user.removeFriend(friend);
        friend.removeFriend(user);
        log.info("Друг удален из списка друзей пользователя");
        log.info("Пользователь удален из списка друзей друга");
    }

    public Set<User> getFriends(Set<Long> friendList) {
        Set<User> friends = new HashSet<>();
        for (Long friendId : friendList) {
            User friend = userStorage.getUserById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
            friends.add(friend);
        }
        return friends;
    }

    public Set<User> checkFriends(Long userId) {
        Set<User> friendList = new HashSet<>();
        for (Long friendId : userStorage.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден")).getFriendList()) {
            User friend = userStorage.getUserById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
            friendList.add(friend);
        }
        log.info("Выводим список друзей");
        return friendList;
    }

    public Set<User> getCommonFriends(Long userId1, Long userId2) {
        if (Objects.equals(userId1, userId2)) return new HashSet<>();

        Set<User> friends1 = getFriends(userStorage.getUserById(userId1).orElseThrow(() -> new NotFoundException("Пользователь не найден")).getFriendList());
        Set<User> friends2 = getFriends(userStorage.getUserById(userId2).orElseThrow(() -> new NotFoundException("Пользователь не найден")).getFriendList());

        friends1.retainAll(friends2);
        return new HashSet<>(friends1);
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
}

