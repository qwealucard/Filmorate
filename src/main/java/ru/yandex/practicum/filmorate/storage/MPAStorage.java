package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MPARating;

import java.util.List;

public interface MPAStorage {

    List<MPARating> findAll();

    MPARating addRating(MPARating rating);

    MPARating findRatingById(Integer id);

    Integer deleteRating(Integer id);
}
