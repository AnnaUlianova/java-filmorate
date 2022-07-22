package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;
    private final ReviewStorage storage;
    private final UserService userService;

    @Autowired
    public ReviewService(ReviewDbStorage reviewDbStorage, ReviewStorage storage, UserService userService) {
        this.reviewDbStorage = reviewDbStorage;
        this.storage = storage;
        this.userService = userService;
    }

    public Review addReview(Review review) throws ValidationException {
        if (isReviewValid(review)) {
            return reviewDbStorage.create(review);
        }
        throw new ValidationException("Validation fail");
    }

    public Optional<Review> updateReview(Review review) throws ValidationException {
        if (isReviewValid(review)) {
            return reviewDbStorage.update(review);
        }
        throw new ValidationException("Validation fail");
    }

    public boolean deleteById(long reviewId) {
        return reviewDbStorage.deleteById(reviewId);

    }

    public Optional<Review> getReviewById(long reviewId) {
        return storage.findById(reviewId);
    }

    public List<Review> getReviewSorted(Long filmId, int count) {
        if (filmId != null) {
            if (filmId < 0) {
                throw new FilmNotFoundException("Фильм не найден");
            } else if (count < 0) {
                throw new IncorrectParameterException("Некорректно указан параметр count");
            }
        }
        return reviewDbStorage.getReviewSorted(filmId, count);
    }

    public void addLikeToReview(Long reviewId, Long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Review> optReview = storage.findById(userId);

        if (optUser.isPresent() && optReview.isPresent()) {
            storage.addLikeToReview(reviewId, userId);
        }
    }

    public void addDislikeToReview(Long reviewId, Long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Review> optReview = storage.findById(userId);

        if (optUser.isPresent() && optReview.isPresent()) {
            reviewDbStorage.addDislikeToReview(reviewId, userId);
        }
    }

    public boolean deleteLikeFromReview(Long reviewId, Long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Review> optReview = storage.findById(userId);

        if (optUser.isPresent() && optReview.isPresent()) {
            reviewDbStorage.deleteLikeFromReview(reviewId, userId);
        }
        return false;
    }

    public boolean deleteDislikeFromReview(Long reviewId, Long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Review> optReview = storage.findById(userId);

        if (optUser.isPresent() && optReview.isPresent()) {
            reviewDbStorage.deleteDislikeFromReview(reviewId, userId);
        }
        return false;
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
