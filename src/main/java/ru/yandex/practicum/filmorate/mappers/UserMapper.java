package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User mapToUser(NewUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBirthday(request.getBirthday());
        user.setLogin(request.getLogin());
        return user;
    }

    public static UserDTO mapToUserDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());
        dto.setLogin(user.getLogin());
        return dto;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getLogin() != null) {
            user.setLogin(request.getLogin());
        }
        return user;
    }
}
