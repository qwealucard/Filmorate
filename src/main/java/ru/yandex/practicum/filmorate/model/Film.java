package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

//@AllArgsConstructor
@Data
public class Film {
    private Integer id;
    @NotNull
    @NotBlank(message = "Название фильма должно быть указано")
    private String name;
    @NotNull
    @Size(max = 200, message = "Описание фильма не должно превышать 200 символов")
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
    private MPARating mpaRating;
}