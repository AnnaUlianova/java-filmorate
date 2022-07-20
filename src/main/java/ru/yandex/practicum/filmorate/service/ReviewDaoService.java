package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
public class ReviewDaoService {

    private static final String QUERY_GET_REVIEW_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String QUERY_GET_TEN_REVIEW_SORTED = "SELECT * FROM reviews WHERE " +
            "film_id = ? ORDER BY useful DESC LIMIT 10";
    private static final String QUERY_GET_FILM_REVIEW_SORTED = "SELECT * FROM reviews WHERE " +
            "film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String QUERY_GET_REVIEW_SORTED = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String QUERY_INCREASE_REVIEW_USEFUL = "UPDATE reviews SET " +
            "useful = (useful + 1) WHERE review_id = ?";
    private static final String QUERY_DECREASE_REVIEW_USEFUL = "UPDATE reviews SET " +
            "useful = (useful - 1) WHERE review_id = ?";
    private static final String QUERY_INSERT_INTO_USERS_REVIEWS = "UPDATE users_reviews " +
            "SET  is_helpful=? WHERE user_id = ? AND review_Id = ?";
    private static final String QUERY_DELETE_LIKE_FROM_USERS_REVIEWS = "DELETE FROM users_reviews WHERE user_id = ?" +
            " AND review_id = ? AND is_helpful IS TRUE";
    private static final String QUERY_DELETE_FROM_USERS_REVIEWS = "DELETE FROM users_reviews WHERE review_id = ?";
    private final JdbcTemplate jdbcTemplate;
    private final ReviewDbStorage reviewDbStorage;

    @Autowired
    public ReviewDaoService(JdbcTemplate jdbcTemplate, ReviewDbStorage reviewDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewDbStorage = reviewDbStorage;
    }

    public Review getReviewById(Long reviewId) {
        if (reviewId < 0) {
            log.info("ReviewNotFoundException: Ревью c id = \"{}\" не найдено", reviewId);
            throw new ReviewNotFoundException("Ревью не найдено");
        }
        try {
            return jdbcTemplate.queryForObject(QUERY_GET_REVIEW_BY_ID, this::mapRowToReview, reviewId);

        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException("Ревью не найдено");
        }
    }

    public Review addReview(Review review) throws ValidationException {
        return reviewDbStorage.addReview(review);
    }

    public Review updateReview(Review review) throws ValidationException {
        return reviewDbStorage.updateReview(review);
    }

    public void deleteReview(Long reviewId) {
        jdbcTemplate.update(QUERY_DELETE_FROM_USERS_REVIEWS, reviewId);
        reviewDbStorage.deleteReview(reviewId);
    }

    public List<Review> getReviewSorted(Long filmId, int count) {
        if (filmId != null) {
            if (filmId < 0) {
                log.info("FilmNotFoundException: Фильм c id = \"{}\" не найден", filmId);
                throw new FilmNotFoundException("Фильм не найден");
            } else if (count < 0) {
                log.info("IncorrectParameterException: Параметр count = \"{}\" задан не верно", count);
                throw new IncorrectParameterException("Параметр count задан не верно");
            } else if (count == 0) {
                return jdbcTemplate.query(QUERY_GET_TEN_REVIEW_SORTED, this::mapRowToReview, filmId);
            } else
                return jdbcTemplate.query(QUERY_GET_FILM_REVIEW_SORTED, this::mapRowToReview, filmId, count);
        }
        return reviewDbStorage.getAllReviews();
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        if (reviewId < 0) {
            log.info("ReviewNotFoundException: Ревью c id = \"{}\" не найдено", reviewId);
            throw new ReviewNotFoundException("Ревью не найдено");
        } else if (userId < 0) {
            log.info("UserNotFoundException: Пользователь c id = \"{}\" не найден", userId);
            throw new UserNotFoundException("Пользователь не найден");
        }
        jdbcTemplate.update(QUERY_INSERT_INTO_USERS_REVIEWS, true, userId, reviewId);
        jdbcTemplate.update(QUERY_INCREASE_REVIEW_USEFUL, reviewId);
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        if (reviewId < 0) {
            log.info("ReviewNotFoundException: Ревью c id = \"{}\" не найдено", reviewId);
            throw new ReviewNotFoundException("Ревью не найдено");
        } else if (userId < 0) {
            log.info("UserNotFoundException: Пользователь c id = \"{}\" не найден", userId);
            throw new UserNotFoundException("Пользователь не найден");
        }
        jdbcTemplate.update(QUERY_INSERT_INTO_USERS_REVIEWS, false, userId, reviewId);
        jdbcTemplate.update(QUERY_DECREASE_REVIEW_USEFUL, reviewId);
    }

    public void deleteLikeFromReview(Long reviewId, Long userId) {
        if (reviewId < 0) {
            log.info("ReviewNotFoundException: Ревью c id = \"{}\" не найдено", reviewId);
            throw new ReviewNotFoundException("Ревью не найдено");
        } else if (userId < 0) {
            log.info("UserNotFoundException: Пользователь c id = \"{}\" не найден", userId);
            throw new UserNotFoundException("Пользователь не найден");
        }
        jdbcTemplate.update(QUERY_DELETE_LIKE_FROM_USERS_REVIEWS, userId, reviewId);
        jdbcTemplate.queryForObject(QUERY_DECREASE_REVIEW_USEFUL, this::mapRowToReview, reviewId);
    }

    public void deleteDislikeFromReview(Long reviewId, Long userId) {
        if (reviewId < 0) {
            log.info("ReviewNotFoundException: Ревью c id = \"{}\" не найдено", reviewId);
            throw new ReviewNotFoundException("Ревью не найдено");
        } else if (userId < 0) {
            log.info("UserNotFoundException: Пользователь c id = \"{}\" не найден", userId);
            throw new UserNotFoundException("Пользователь не найден");
        }
        jdbcTemplate.update(QUERY_DELETE_LIKE_FROM_USERS_REVIEWS, userId, reviewId);
        jdbcTemplate.queryForObject(QUERY_DECREASE_REVIEW_USEFUL, this::mapRowToReview, reviewId);
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
}
