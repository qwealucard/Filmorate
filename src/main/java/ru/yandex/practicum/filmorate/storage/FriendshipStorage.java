package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {
     void addFriend(Integer id, Integer friendId);

     void removeFriend(Integer id, Integer friendId);

     List<User> getAllFriends(Integer id);

     List<User> getCommonFriends(Integer userId, Integer friendId);

     public boolean isFriend(Integer userId, Integer friendId);
}
