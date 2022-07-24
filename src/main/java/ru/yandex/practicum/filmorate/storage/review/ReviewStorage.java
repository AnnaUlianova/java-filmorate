package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface ReviewStorage extends Storage<Review> {

    boolean addLikeToReview(Long reviewId, Long userId);

    boolean addDislikeToReview(Long reviewId, Long userId);

    boolean deleteLikeFromReview(Long reviewId, Long userId);

    boolean deleteDislikeFromReview(Long reviewId, Long userId);

    List<Review> getReviewSorted(Long filmId, int count);

}
