package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DuplicateException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    public final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (!userStorage.getUsers().containsValue(user)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userStorage.getUsers().containsValue(friend)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден.");
        }
        if (user.getFriendList().contains(friendId)) {
            throw new DuplicateException("Этот пользователь уже есть в списке ваших друзей");
        }
        if (userId.equals(friendId)) {
            throw new DuplicateException("Вы не можете добавить себя в друзья");
        }
        user.getFriendList().add(friendId);
        log.info("Добавлен друг в список друзей пользователя");
        friend.getFriendList().add(userId);
        log.info("Добавлен пользователь в список друзей друга");
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (friend == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден.");
        }
        user.getFriendList().remove(friend.getId());
        log.info("Друг удален из списка друзей пользователя");
        friend.getFriendList().remove(user.getId());
        log.info("Пользователь удален из списка друзей друга");
    }

    public Set<User> getFriends(Set<Long> friendList) {
        Set<User> friends = new HashSet<>();
        for (Long friendId : friendList) {
            User friend = userStorage.getUserById(friendId);
            friends.add(friend);
        }
        return friends;
    }

    public Set<User> checkFriends(Long userId) {
        Set<User> friendList = new HashSet<>();
        for (Long friendId : userStorage.getUserById(userId).getFriendList()) {
            User friend = userStorage.getUserById(friendId);
            if (friend != null) {
                friendList.add(friend);
            } else {
                log.error("Ошибка в получении списка друзей");
                throw new ValidationException("У вас нет добавленных друзей");
            }
        }
        log.info("Выводим список друзей");
        return friendList;
    }

    public Set<User> getCommonFriends(Long userId1, Long userId2) {
        if (Objects.equals(userId1, userId2)) return new HashSet<>();

        Set<User> friends1 = getFriends(userStorage.getUserById(userId1).getFriendList());
        Set<User> friends2 = getFriends(userStorage.getUserById(userId2).getFriendList());


        friends1.retainAll(friends2);
        return new HashSet<>(friends1);
    }
}

