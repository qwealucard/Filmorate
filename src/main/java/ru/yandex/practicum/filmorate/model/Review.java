package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @NotNull(message = "Content cannot be null")
    private String content;

    @NotNull(message = "isPositive cannot be null")
    private Boolean isPositive;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id")
    private Integer userId;

    @NotNull(message = "Film ID cannot be null")
    @Column(name = "film_id")
    private Integer filmId;

    private Integer useful;
}