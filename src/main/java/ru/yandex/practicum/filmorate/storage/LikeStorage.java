package ru.yandex.practicum.filmorate.storage;

public interface LikeStorage {
    void addLike(Integer userId, Integer filmId);

    void removeLike(Integer userId, Integer filmId);
}
