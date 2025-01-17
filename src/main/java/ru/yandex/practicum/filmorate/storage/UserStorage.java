package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    User update(User user);

    Collection<User> findAll();

    void deleteUserById(Integer id);

    Optional<User> getUserById(Integer id);

//    List<UserFeedEvent> getUserFeed(int id);
//
//    void addUserEvent(UserFeedEvent event);
}
