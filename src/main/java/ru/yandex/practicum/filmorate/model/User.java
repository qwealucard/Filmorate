package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    @NotNull
    @Email(message = "Некорректный формат email")
    private String email;
    @NotNull
    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;
}
