package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MPADbStorage;
import ru.yandex.practicum.filmorate.exceptions.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.MPARating;

import java.util.List;

@Service
public class MPARatingService {
    private final MPADbStorage mpaStorage;

    public MPARatingService(@Qualifier("MPADbStorage") MPADbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public MPARating addMPARating(MPARating mpaRating) {
        return mpaStorage.addRating(mpaRating);
    }

    public MPARating getMPARatingById(Integer id) {
        MPARating mpaRating = mpaStorage.findRatingById(id);
        if (mpaRating == null) {
            throw new RatingNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        return mpaRating;
    }

    public List<MPARating> getAllMPARatings() {
        return mpaStorage.findAll();
    }

    public void deleteMPARatingById(Integer id) {
        Integer rows = mpaStorage.deleteRating(id);
        if (rows == 0) {
            throw new RatingNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
    }
}
