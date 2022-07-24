package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class ReviewDbStorage implements ReviewStorage {

    private static final String QUERY_GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String QUERY_GET_TEN_REVIEW_SORTED = "SELECT * FROM reviews WHERE " +
            "film_id = ? ORDER BY useful DESC LIMIT 10";
    private static final String QUERY_GET_ALL_REVIEWS = "SELECT * FROM reviews ORDER BY USEFUL DESC";
    private static final String QUERY_ADD_REVIEW = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, 0);";
    private static final String QUERY_UPDATE_REVIEW_BY_ID = "UPDATE reviews SET " +
            " content = ?, is_positive = ? WHERE review_id = ?";
    private static final String QUERY_DELETE_REVIEW_BY_ID = "DELETE FROM reviews WHERE review_id = ?";
    private static final String QUERY_GET_FILM_REVIEW_SORTED = "SELECT * FROM reviews WHERE " +
            "film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String QUERY_DELETE_FROM_USERS_REVIEWS = "DELETE FROM users_reviews WHERE review_id = ?";
    private static final String QUERY_INCREASE_REVIEW_USEFUL = "UPDATE reviews SET " +
            "useful = (useful + 1) WHERE review_id = ?";
    private static final String QUERY_INSERT_INTO_USERS_REVIEWS = "UPDATE users_reviews " +
            "SET  is_helpful=? WHERE user_id = ? AND review_Id = ?";
    private static final String QUERY_DECREASE_REVIEW_USEFUL = "UPDATE reviews SET " +
            "useful = (useful - 1) WHERE review_id = ?";
    private static final String QUERY_DELETE_LIKE_FROM_USERS_REVIEWS = "DELETE FROM users_reviews WHERE user_id = ?" +
            " AND review_id = ? AND is_helpful IS TRUE";
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Review> findAll() {
        return jdbcTemplate.query(QUERY_GET_ALL_REVIEWS, this::mapRowToReview);
    }

    @Override
    public Review create(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(QUERY_ADD_REVIEW, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());
        return review;
    }

    @Override
    public Optional<Review> update(Review review) {
        boolean isUpdated = jdbcTemplate.update(QUERY_UPDATE_REVIEW_BY_ID,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()) > 0;
        return isUpdated ? Optional.of(review) : Optional.empty();
    }

    @Override
    public boolean deleteById(long reviewId) {
        jdbcTemplate.update(QUERY_DELETE_REVIEW_BY_ID, reviewId);
        jdbcTemplate.update(QUERY_DELETE_FROM_USERS_REVIEWS, reviewId);
        return true;
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(resultSet.getLong("review_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }

    @Override
    public Optional<Review> findById(long reviewId) {
        try {
            Review review = jdbcTemplate.queryForObject(QUERY_GET_REVIEW_BY_ID,
                    this::mapRowToReview, reviewId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean addLikeToReview(Long reviewId, Long userId) {
        jdbcTemplate.update(QUERY_INSERT_INTO_USERS_REVIEWS, true, userId, reviewId);
        jdbcTemplate.update(QUERY_INCREASE_REVIEW_USEFUL, reviewId);
        return false;
    }

    @Override
    public boolean addDislikeToReview(Long reviewId, Long userId) {
        jdbcTemplate.update(QUERY_INSERT_INTO_USERS_REVIEWS, false, userId, reviewId);
        jdbcTemplate.update(QUERY_DECREASE_REVIEW_USEFUL, reviewId);
        return false;
    }

    @Override
    public boolean deleteLikeFromReview(Long reviewId, Long userId) {
        jdbcTemplate.update(QUERY_DELETE_LIKE_FROM_USERS_REVIEWS, userId, reviewId);
        jdbcTemplate.queryForObject(QUERY_DECREASE_REVIEW_USEFUL, this::mapRowToReview, reviewId);
        return false;
    }

    @Override
    public boolean deleteDislikeFromReview(Long reviewId, Long userId) {
        jdbcTemplate.update(QUERY_DELETE_LIKE_FROM_USERS_REVIEWS, userId, reviewId);
        jdbcTemplate.queryForObject(QUERY_DECREASE_REVIEW_USEFUL, this::mapRowToReview, reviewId);
        return false;
    }

    @Override
    public List<Review> getReviewSorted(Long filmId, int count) {
        if (filmId != null) {
            if (count == 0) {
                return jdbcTemplate.query(QUERY_GET_TEN_REVIEW_SORTED, this::mapRowToReview, filmId);
            } else
                return jdbcTemplate.query(QUERY_GET_FILM_REVIEW_SORTED, this::mapRowToReview, filmId, count);
        }
        return findAll();
    }
}