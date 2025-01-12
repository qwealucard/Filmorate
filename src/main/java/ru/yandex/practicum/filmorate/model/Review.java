package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Review {
    private Integer reviewId;
    private String content;
    private boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful;
}