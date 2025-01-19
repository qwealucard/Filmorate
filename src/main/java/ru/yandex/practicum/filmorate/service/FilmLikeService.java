package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class FilmLikeService {

    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final UserFeedEventService userFeedEventService;

    public FilmLikeService(@Qualifier("filmDbStorage") FilmStorage filmStorage, LikeStorage likeStorage,
                           UserFeedEventService userFeedEventService) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;
        this.userFeedEventService = userFeedEventService;
    }

    public void addLike(Integer userId, Integer filmId) {
            likeStorage.addLike(userId, filmId);

            addUserEvent(userId, "LIKE", "ADD", filmId);
    }

    public void removeLike(Integer userId, Integer filmId) {
            likeStorage.removeLike(userId, filmId);

            addUserEvent(userId, "LIKE", "REMOVE", filmId);
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
        userFeedEventService.addUserEvent(event);
        log.info("Событие типа \"{}\" для операции \"{}\" для пользователя с id = {} внесено в БД", eventType, operation, userId);
    }

    public List<Film> getTopFilms(int count) {
        try {
            return filmStorage.getPopularFilms(count);
        } catch (FilmNotFoundException e) {
            throw new FilmNotFoundException("Фильм не найден");
        }
    }
}
