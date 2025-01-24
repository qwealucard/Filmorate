package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "directors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Наименование автора не может быть null")
    @Size(min = 1, max = 255, message = "Наименование автора должно быть не длиннее 255 символов")
    @Pattern(regexp = ".*\\S.*", message = "Наименование автора не может быть пустым или состоять только из пробелов")
    @Column(nullable = false, unique = true, length = 255)
    private String name;
}