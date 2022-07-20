package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class ReviewDbStorage implements ReviewStorage {

    private static final String QUERY_GET_ALL_REVIEWS = "SELECT * FROM reviews ORDER BY USEFUL DESC";
    private static final String QUERY_ADD_REVIEW = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, 0);";
    private static final String QUERY_UPDATE_REVIEW_BY_ID = "UPDATE reviews SET " +
            " content = ?, is_positive = ? WHERE review_id = ?";
    private static final String QUERY_DELETE_REVIEW_BY_ID = "DELETE FROM reviews WHERE review_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Review> getAllReviews() {
        return jdbcTemplate.query(QUERY_GET_ALL_REVIEWS, this::mapRowToReview);
    }

    @Override
    public Review addReview(Review review) throws ValidationException {
        if (review.getUserId() < 0) {
            throw new UserNotFoundException("id < 0");
        }
        if (isReviewValid(review)) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(QUERY_ADD_REVIEW, new String[]{"review_id"});
                stmt.setString(1, review.getContent());
                stmt.setBoolean(2, review.getIsPositive());
                stmt.setLong(3, review.getUserId());
                stmt.setLong(4, review.getFilmId());
                return stmt;
            }, keyHolder);

            review.setId(keyHolder.getKey().longValue());
            return review;
        }
        log.info("ValidationException: Ревью c id = \"{}\" не прошло валидацию", review.getId());
        throw new ValidationException("Ревью не прошло валидацию");
    }

    @Override
    public Review updateReview(Review review) throws ValidationException {
        if (review.getId() < 0) {
            log.info("ReviewNotFoundException: Ревью c id = \"{}\" не найдено", review.getId());
            throw new ReviewNotFoundException("Ревью не найдено");
        }
        if (isReviewValid(review)) {
            jdbcTemplate.update(QUERY_UPDATE_REVIEW_BY_ID
                    , review.getContent()
                    , review.getIsPositive()
                    , review.getId());
        }
        return review;
    }


    @Override
    public void deleteReview(Long reviewId) {
        jdbcTemplate.update(QUERY_DELETE_REVIEW_BY_ID, reviewId);
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) throws SQLException {

        return Review.builder()
                .id(resultSet.getLong("review_id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }

    private boolean isReviewValid(Review review) throws ValidationException, ReviewNotFoundException {
        if (review.getContent().length() > 1000) {
            log.info("ValidationException: отзыв содержит более 1000 символов");
            throw new ValidationException("отзыв содержит более 1000 символов");
        } else if (review.getUserId() < 0) {
            log.info("UserNotFoundException: Юзер c id = \"{}\" не найден", review.getUserId());
            throw new UserNotFoundException("Юзер не найден");
        } else if (review.getFilmId() < 0) {
            log.info("FilmNotFoundException: Фильм c id = \"{}\" не найден", review.getFilmId());
            throw new FilmNotFoundException("Фильм не найден");
        } else if (review.getUserId() == 0) {
            log.info("ValidationException: поле user_id не может быть пустым");
            throw new ValidationException("поле user_id не может быть пустым");
        } else if (review.getContent().length() == 0) {
            log.info("ValidationException: контент отзыва не может быть пустым");
            throw new ValidationException("контент отзыва не может быть пустым");
        }
        log.info("Успешная валидация...");
        return true;
    }
}
