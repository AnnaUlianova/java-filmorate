package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    List<Review> getAllReviews();

    Review addReview(Review review) throws ValidationException;

    Review updateReview(Review review) throws ValidationException;

    void deleteReview(Long reviewId);
}
