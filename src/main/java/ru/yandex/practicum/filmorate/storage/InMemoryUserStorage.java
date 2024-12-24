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
import java.util.Optional;

@Getter
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();


    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        log.info("Создание нового пользователя");
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка при заполнении логина");
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

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            log.error("Ошибка нахождения пользователя при обновлении");
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
        log.error("Ошибка нахождения пользователя при обновлении");
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        User user = users.get(id);
        return Optional.ofNullable(user);
    }


    private Integer getNextId() {
        Integer currentMaxId = users.keySet()
                                    .stream()
                                    .peek(id -> log.info("ID сгенерирован: {}", id)) // оставляем peek
                                    .max(Integer::compare)
                                    .orElse(0) + 1;

        return currentMaxId;
    }

    @Override
    public boolean exists(Integer userId) {
        return users.containsKey(userId);
    }
}
