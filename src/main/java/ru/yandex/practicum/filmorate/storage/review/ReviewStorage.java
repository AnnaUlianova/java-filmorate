package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface ReviewStorage extends Storage<Review> {

    void addLikeToReview(Long reviewId, Long userId);

    void addDislikeToReview(Long reviewId, Long userId);

    void deleteLikeFromReview(Long reviewId, Long userId);

    void deleteDislikeFromReview(Long reviewId, Long userId);

    List<Review> getReviewSorted(Long filmId, int count);

}
