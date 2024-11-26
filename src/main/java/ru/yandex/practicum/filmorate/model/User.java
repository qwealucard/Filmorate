package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Data
public class User {
    private Long id;
    @Email(message = "Некорректный формат email")
    private String email;
    @NotNull
    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;
    final Set<Long> friendList = new HashSet<>();

    public void addFriend(User user) {
        friendList.add(user.getId());
    }

    public void removeFriend(User user) {
        friendList.remove(user.getId());
    }
}
