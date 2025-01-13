package ru.yandex.practicum.filmorate;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.adapters.GsonProvider;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerPopularTest {

    @Autowired
    private MockMvc mockMvc;

    private Gson gson = GsonProvider.getGson();

    @BeforeEach
    void setUp() throws Exception {
        Faker faker = new Faker();

        // Генерация случайного фильма
        for (int i = 1; i <= 20; i++) {
            Film film = new Film();
            film.setName(faker.book().title());
            film.setDescription(faker.lorem().sentence());
            film.setReleaseDate(LocalDate.of(
                    faker.number().numberBetween(1980, 2023),
                    faker.number().numberBetween(1, 12),
                    faker.number().numberBetween(1, 28)
            ));
            film.setDuration(faker.number().numberBetween(60, 200));
            film.setMpa(new MPARating(1, "G")); // Убедитесь, что MPA существует в БД
            film.setGenres(Collections.singletonList(new Genre(1, "Comedy"))); // Генерация жанра

            mockMvc.perform(post("/films")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(film)))
                    .andExpect(status().isOk());
        }

        // Генерация случайных пользователей
        for (int i = 1; i <= 20; i++) {
            // Случайные данные для пользователя
            JsonObject userJson = new JsonObject();
            userJson.addProperty("login", faker.name().username());
            userJson.addProperty("name", faker.name().fullName());
            userJson.addProperty("email", faker.internet().emailAddress());
            userJson.addProperty("birthday", LocalDate.of(
                    faker.number().numberBetween(1950, 2005),
                    faker.number().numberBetween(1, 12),
                    faker.number().numberBetween(1, 28)
            ).toString());

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(userJson)))
                    .andExpect(status().isOk());
        }

        // Генерация случайных лайков
        for (int filmId = 1; filmId <= 20; filmId++) {
            mockMvc.perform(put(String.format("/films/%d/like/%d", filmId, filmId)))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void testGetPopularFilmsWithGenreFilter() throws Exception {
        mockMvc.perform(get("/films/popular?count=10&genreId=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(10)); // Проверка, что возвращён только 1 фильм с жанром "1"
    }

}