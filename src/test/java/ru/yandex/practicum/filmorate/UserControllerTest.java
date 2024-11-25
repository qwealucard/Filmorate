package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private final InMemoryUserStorage userStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(userStorage);
    private final UserController userController = new UserController(userService);

    @Test
    void create_validUser_shouldReturnCreatedUser() {
        User user = new User(0L, "Artem@yandex.ru", "qwealucard", "Артём", LocalDate.of(2004, 3, 11));
        user.setId(1L);
        user.setName("John");
        user.setEmail("john.doe@example.com");
        user.setLogin("John_Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(user);
        assertEquals(user, createdUser);
        assertEquals(1, userStorage.getUsers().size());
        assertEquals(user, userStorage.getUsers().get(1L));
    }

    @Test
    void create_invalidLogin_shouldThrowValidationException() {
        User user = new User(0L, "Artem@yandex.ru", "qwealucard", "Артём", LocalDate.of(2004, 3, 11));
        user.setId(1L);
        user.setName("John");
        user.setEmail("john.doe@example.com");
        user.setLogin("John Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void update_validUser_shouldReturnUpdatedUser() {
        User user = new User(0L, "Artem@yandex.ru", "qwealucard", "Артём", LocalDate.of(2004, 3, 11));
        user.setId(1L);
        user.setName("John");
        user.setEmail("john.doe@example.com");
        user.setLogin("John_Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.getUsers().put(user.getId(), user);
        User updatedUser = new User(0L, "Artem@yandex.ru", "qwealucard", "Артём", LocalDate.of(2004, 3, 11));
        updatedUser.setId(1L);
        updatedUser.setName("Jane");
        updatedUser.setEmail("jane.doe@example.com");
        updatedUser.setLogin("Jane_Doe");
        updatedUser.setBirthday(LocalDate.of(1991, 2, 2));
        User resultUser = userController.update(updatedUser);
        assertEquals(updatedUser, resultUser);
        assertEquals(updatedUser, userStorage.getUsers().get(1L));
    }

    @Test
    void update_notFound_shouldThrowNotFoundException() {
        User user = new User(0L, "Artem@yandex.ru", "qwealucard", "Артём", LocalDate.of(2004, 3, 11));
        user.setId(100L);
        user.setName("John");
        user.setEmail("john.doe@example.com");
        user.setLogin("John_Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }
}
