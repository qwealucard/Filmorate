package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
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
    private List<Genre> genres;
    private MPARating mpa;
    private List<Director> directors;
}