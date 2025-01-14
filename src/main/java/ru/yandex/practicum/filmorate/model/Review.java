package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;

    @NotNull(message = "Content cannot be null")
    private String content;

    @NotNull(message = "isPositive cannot be null")
    private Boolean isPositive;


    @NotNull(message = "Film ID cannot be null")
    private Integer userId;

    @NotNull(message = "Film ID cannot be null")

    @NotNull(message = "Film ID cannot be null")
    private Integer filmId;
    private Integer useful;

}