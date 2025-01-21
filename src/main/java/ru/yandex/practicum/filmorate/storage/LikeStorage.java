package ru.yandex.practicum.filmorate.storage;

import java.util.List;

public interface LikeStorage {
    void addLike(Integer userId, Integer filmId);

    void removeLike(Integer userId, Integer filmId);

    List<Integer> getLikedFilmIds(Integer userId);

//    boolean isLikeAlreadyAdded(Integer filmId, Integer userId);
}
