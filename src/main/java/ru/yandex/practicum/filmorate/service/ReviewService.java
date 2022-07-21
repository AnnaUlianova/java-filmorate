package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;

    @Autowired
    public ReviewService(ReviewDbStorage reviewDbStorage) {
        this.reviewDbStorage = reviewDbStorage;
    }

    public Review create(Review review) throws ValidationException {
        if (isReviewValid(review)) {
            return reviewDbStorage.create(review);
        }
        throw new ValidationException("Validation fail");
    }

    public Optional<Review> update(Review review) throws ValidationException {
        return reviewDbStorage.update(review);
    }

    public void deleteById(long reviewId) {
        reviewDbStorage.deleteById(reviewId);
    }

    public Optional<Review> getReviewById(long reviewId) {
        return reviewDbStorage.findById(reviewId);
    }

    public List<Review> getReviewSorted(Long filmId, int count) {
        return reviewDbStorage.getReviewSorted(filmId, count);
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        reviewDbStorage.addLikeToReview(reviewId, userId);
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        reviewDbStorage.addDislikeToReview(reviewId, userId);
    }

    public void deleteLikeFromReview(Long reviewId, Long userId) {
        reviewDbStorage.deleteLikeFromReview(reviewId, userId);
    }

    public void deleteDislikeFromReview(Long reviewId, Long userId) {
        reviewDbStorage.deleteDislikeFromReview(reviewId, userId);
    }

    private boolean isReviewValid(Review review) throws ValidationException, ReviewNotFoundException {
        if (review.getContent().length() > 1000) {
            throw new ValidationException("отзыв содержит более 1000 символов");
        } else if (review.getUserId() < 0) {
            throw new UserNotFoundException("Юзер не найден");
        } else if (review.getFilmId() < 0) {
            throw new FilmNotFoundException("Фильм не найден");
        } else if (review.getUserId() == 0) {
            throw new ValidationException("поле user_id не может быть пустым");
        } else if (review.getContent().length() == 0) {
            throw new ValidationException("контент отзыва не может быть пустым");
        }
        return true;
    }

}
