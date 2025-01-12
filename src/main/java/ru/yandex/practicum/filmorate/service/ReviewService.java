package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review addReview(Review review) {
        validateReview(review);
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("Review ID cannot be null for update.");
        }
        validateReview(review);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Integer reviewId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(Integer reviewId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getReviews(Integer filmId, Integer count) {
        if (count == null || count <= 0) {
            count = 10; // Значение по умолчанию
        }
        return reviewStorage.getReviews(filmId, count);
    }

    public void addLike(Integer reviewId, Integer userId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        if (!reviewStorage.existsById(reviewId)) {
            throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
        }
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Review content cannot be null or blank.");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("Review must have a valid user ID.");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Review must have a valid film ID.");
        }
    }
}