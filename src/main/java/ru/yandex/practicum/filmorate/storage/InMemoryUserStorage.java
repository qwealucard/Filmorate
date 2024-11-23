package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        log.info("Создание нового пользователя");
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        user.setId(getNextId());
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь добавлен");
        return user;
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.info("Обновление пользователя");
        User existingUser = users.get(user.getId());
        if (existingUser != null) {
            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getLogin() != null) {
                existingUser.setLogin(user.getLogin());
            }
            if (user.getBirthday() != null) {
                existingUser.setBirthday(user.getBirthday());
            }
            users.put(user.getId(), existingUser);
            log.info("Пользователь обновлен");
            return existingUser;
        }
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    public User getUserById(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователя с таким id нет");
        }
        return users.entrySet().stream()
                    .filter(entry -> entry.getKey() == id)
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
    }

    private Long getNextId() {
        Long currentMaxId = users.keySet()
                                 .stream()
                                 .mapToLong(id -> id)
                                 .peek(id -> log.info("ID сгенерирован: {}", id))
                                 .max()
                                 .orElse(0L) + 1;
        return currentMaxId;
    }
}
