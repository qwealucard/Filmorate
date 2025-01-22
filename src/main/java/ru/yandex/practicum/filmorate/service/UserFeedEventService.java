package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserFeedEventDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class UserFeedEventService {
    private final UserFeedEventDbStorage userFeedEventDbStorage;
    private final UserStorage userStorage;

    public UserFeedEventService(UserFeedEventDbStorage userFeedEventDbStorage, UserStorage userStorage) {
        this.userFeedEventDbStorage = userFeedEventDbStorage;
        this.userStorage = userStorage;
    }

    public List<UserFeedEvent> getUserFeed(Integer userId) {
        validateNotFound(userId);
        log.info("Получение ленты пользователя с ID: {}", userId);

        List<UserFeedEvent> userFeed = userFeedEventDbStorage.getUserFeed(userId);
        log.info("Получена лента пользователя с ID = : {}, кол-во событий: {}", userId, userFeed.size());
        return userFeed;
    }

    private void validateNotFound(int id) {
        userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    private void addEvent(UserFeedEvent event) {
        log.info("Добавление события типа \"{}\" для операции \"{}\" для пользователя id = {} в БД",
                event.getEventType(), event.getOperation(), event.getUserId());

        userFeedEventDbStorage.addUserEvent(event);
        log.info("Событие типа \"{}\" для операции \"{}\" для пользователя id = {} добавлено в БД",
                event.getEventType(), event.getOperation(), event.getUserId());
    }

    public void addUserEvent(Integer userId, String eventType, String operation, Integer entityId) {
        log.info("Создание события типа \"{}\" для операции \"{}\", для пользователя с id = {}", eventType, operation, userId);

        UserFeedEvent event = new UserFeedEvent(
                0, // eventId будет сгенерирован базой данных
                userId,
                eventType,
                operation,
                entityId,
                Instant.now().toEpochMilli()
        );
        addEvent(event);
        log.info("Событие типа \"{}\" для операции \"{}\" для пользователя с id = {} внесено в БД", eventType, operation, userId);
    }

}

