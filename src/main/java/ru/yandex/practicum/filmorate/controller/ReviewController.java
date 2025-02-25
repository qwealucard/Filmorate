package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserFeedEventService;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserFeedEventService userFeedEventService;

    @PostMapping
    public Review addReview(@RequestBody Review review) {
        log.info("POST /reviews - Adding review: {}", review);
        Review savedReview = reviewService.addReview(review);
        UserFeedEvent event = new UserFeedEvent(
                0,
                savedReview.getUserId(),
                "REVIEW",
                "ADD",
                review.getReviewId(),
                Instant.now().toEpochMilli()
        );
        userFeedEventService.addEvent(event);
        return savedReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        log.info("PUT /reviews - Updating review: {}", review);
        Review savedReview = reviewService.updateReview(review);
        UserFeedEvent event = new UserFeedEvent(
                0,
                savedReview.getUserId(),
                "REVIEW",
                "UPDATE",
                review.getReviewId(),
                Instant.now().toEpochMilli()
        );
        userFeedEventService.addEvent(event);
        return savedReview;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReview(@PathVariable Integer id) {
        log.info("DELETE /reviews/{} - Deleting review", id);
        int userId = reviewService.getReviewById(id).getUserId();
        reviewService.deleteReview(id);
        UserFeedEvent event = new UserFeedEvent(
                0,
                userId,
                "REVIEW",
                "REMOVE",
                id,
                Instant.now().toEpochMilli()
        );
        userFeedEventService.addEvent(event);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Integer id) {
        log.info("GET /reviews/{} - Fetching review by ID", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(defaultValue = "10") int count) {
        log.info("GET /reviews - Fetching reviews with filmId: {}, count: {}", filmId, count);
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("PUT /reviews/{}/like/{} - Adding like", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("PUT /reviews/{}/dislike/{} - Adding dislike", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE /reviews/{}/like/{} - Removing like", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE /reviews/{}/dislike/{} - Removing dislike", id, userId);
        reviewService.removeDislike(id, userId);
    }
}