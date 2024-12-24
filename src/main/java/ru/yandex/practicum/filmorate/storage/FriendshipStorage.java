package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {
     void addFriend(Integer id, Integer friend_id);

     void removeFriend(Integer id, Integer friend_id);

     List<User> getAllFriends(Integer id);
}
