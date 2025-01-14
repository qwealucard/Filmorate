package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ReviewStorageTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewStorage reviewStorage;

    private Review review;
    private Faker faker;

    @BeforeEach
    void setUp() throws Exception {
        faker = new Faker();
        Gson gson = new Gson();

        // Случайные данные для фильма
        JsonObject filmJson = new JsonObject();
        filmJson.addProperty("name", faker.book().title());
        filmJson.addProperty("description", faker.lorem().sentence());
        filmJson.addProperty("releaseDate", LocalDate.of(
                faker.number().numberBetween(1950, 2023),
                faker.number().numberBetween(1, 12),
                faker.number().numberBetween(1, 28)
        ).toString());
        filmJson.addProperty("duration", faker.number().numberBetween(60, 200));
        JsonObject mpaJson = new JsonObject();
        mpaJson.addProperty("id", 1);
        filmJson.add("mpa", mpaJson);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(filmJson)))
                .andExpect(status().isOk());

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

        // Создание объекта Review для тестов
        review = new Review();
        review.setContent(faker.lorem().sentence());
        review.setIsPositive(faker.bool().bool());
        review.setUserId(1);  // ID добавленного пользователя
        review.setFilmId(1);  // ID добавленного фильма
    }

    @Test
    void testAddReview() {
        Review savedReview = reviewStorage.addReview(review);

        assertThat(savedReview).isNotNull();
        assertThat(savedReview.getReviewId()).isPositive();
        assertThat(savedReview.getContent()).isNotBlank();
        assertThat(savedReview.getUseful()).isEqualTo(0);
    }

    @Test
    void testUpdateReview() {
        Review savedReview = reviewStorage.addReview(review);

        savedReview.setContent("Updated review content");
        savedReview.setIsPositive(false);

        Review updatedReview = reviewStorage.updateReview(savedReview);

        assertThat(updatedReview.getContent()).isEqualTo("Updated review content");
        assertThat(updatedReview.getIsPositive()).isFalse();
    }

    @Test
    void testGetReviewById() {
        Review savedReview = reviewStorage.addReview(review);

        Review foundReview = reviewStorage.getReviewById(savedReview.getReviewId());

        assertThat(foundReview).isNotNull();
        assertThat(foundReview.getReviewId()).isEqualTo(savedReview.getReviewId());
        assertThat(foundReview.getContent()).isEqualTo(savedReview.getContent());
    }

    @Test
    void testDeleteReview() {
        Review savedReview = reviewStorage.addReview(review);

        reviewStorage.deleteReview(savedReview.getReviewId());

        assertThat(reviewStorage.existsById(savedReview.getReviewId())).isFalse();
    }

    @Test
    void testGetReviewsByFilmId() {
        Review savedReview = reviewStorage.addReview(review);

        List<Review> reviews = reviewStorage.getReviews(savedReview.getFilmId(), 10);

        assertThat(reviews).isNotEmpty();
        assertThat(reviews.get(0).getFilmId()).isEqualTo(savedReview.getFilmId());
    }

    @Test
    void testAddLike() {
        Review savedReview = reviewStorage.addReview(review);

        reviewStorage.addLike(savedReview.getReviewId(), 1);

        Review updatedReview = reviewStorage.getReviewById(savedReview.getReviewId());

        assertThat(updatedReview.getUseful()).isEqualTo(1);
    }

    @Test
    void testAddDislike() {
        Review savedReview = reviewStorage.addReview(review);

        reviewStorage.addDislike(savedReview.getReviewId(), 1);

        Review updatedReview = reviewStorage.getReviewById(savedReview.getReviewId());

        assertThat(updatedReview.getUseful()).isEqualTo(-1);
    }
}