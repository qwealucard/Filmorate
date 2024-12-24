package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String name;
    private String login;
    private String email;
    private LocalDate birthday;
}
