    package ru.yandex.practicum.filmorate.storage;

    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.jdbc.core.RowMapper;
    import org.springframework.stereotype.Component;
    import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
    import ru.yandex.practicum.filmorate.exceptions.ValidationException;
    import ru.yandex.practicum.filmorate.model.Review;

    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.util.List;

    @Component
    public class ReviewStorage {

        private final JdbcTemplate jdbcTemplate;

        public ReviewStorage(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        public Review addReview(Review review) {
            validateReview(review);
            String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
            jdbcTemplate.update(sql,
                    review.getContent(),
                    review.isPositive(),
                    review.getUserId(),
                    review.getFilmId());

            String idSql = "SELECT review_id FROM reviews WHERE content = ? AND user_id = ? ORDER BY review_id DESC LIMIT 1";
            Integer reviewId = jdbcTemplate.queryForObject(idSql, Integer.class, review.getContent(), review.getUserId());
            review.setReviewId(reviewId);
            review.setUseful(0);

            return review;
        }

        public Review updateReview(Review review) {
            if (review.getReviewId() == null) {
                throw new ValidationException("Review ID cannot be null for update.");
            }
            validateReview(review);
            String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
            int rowsUpdated = jdbcTemplate.update(sql, review.getContent(), review.isPositive(), review.getReviewId());

            if (rowsUpdated == 0) {
                throw new EntityNotFoundException("Review with ID " + review.getReviewId() + " not found.");
            }

            return getReviewById(review.getReviewId());
        }

        public void deleteReview(Integer reviewId) {
            if (!existsById(reviewId)) {
                throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
            }
            String sql = "DELETE FROM reviews WHERE review_id = ?";
            jdbcTemplate.update(sql, reviewId);
        }

        public Review getReviewById(Integer reviewId) {
            String sql = "SELECT * FROM reviews WHERE review_id = ?";
            return jdbcTemplate.queryForObject(sql, new ReviewRowMapper(), reviewId);
        }

        public List<Review> getReviews(Integer filmId, Integer count) {
            String sql;
            Object[] params;

            if (filmId != null) {
                sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
                params = new Object[]{filmId, count};
            } else {
                sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
                params = new Object[]{count};
            }

            return jdbcTemplate.query(sql, new ReviewRowMapper(), params);
        }

        public void addLike(Integer reviewId, Integer userId) {
            if (!existsById(reviewId)) {
                throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
            }
            String sql = "MERGE INTO review_likes (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?, ?, TRUE)";
            jdbcTemplate.update(sql, reviewId, userId);

            updateUseful(reviewId);
        }

        public void addDislike(Integer reviewId, Integer userId) {
            if (!existsById(reviewId)) {
                throw new EntityNotFoundException("Review with ID " + reviewId + " not found.");
            }
            String sql = "MERGE INTO review_likes (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?, ?, FALSE)";
            jdbcTemplate.update(sql, reviewId, userId);

            updateUseful(reviewId);
        }

        public void removeLike(Integer reviewId, Integer userId) {
            String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE";
            jdbcTemplate.update(sql, reviewId, userId);

            updateUseful(reviewId);
        }

        public void removeDislike(Integer reviewId, Integer userId) {
            String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE";
            jdbcTemplate.update(sql, reviewId, userId);

            updateUseful(reviewId);
        }

        private void updateUseful(Integer reviewId) {
            String sql = "UPDATE reviews r SET useful = (SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0) FROM review_likes rl WHERE rl.review_id = r.review_id) WHERE r.review_id = ?";
            jdbcTemplate.update(sql, reviewId);
        }

        public boolean existsById(Integer reviewId) {
            String sql = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
            return count != null && count > 0;
        }

        private static class ReviewRowMapper implements RowMapper<Review> {
            @Override
            public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setContent(rs.getString("content"));
                review.setPositive(rs.getBoolean("is_positive"));
                review.setUserId(rs.getInt("user_id"));
                review.setFilmId(rs.getInt("film_id"));
                review.setUseful(rs.getInt("useful"));
                return review;
            }
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