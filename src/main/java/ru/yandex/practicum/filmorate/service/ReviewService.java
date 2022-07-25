package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
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
    private final FeedStorage feedStorage;

    @Autowired
    public ReviewService(ReviewDbStorage reviewDbStorage, ReviewStorage storage,
                         UserService userService, @Qualifier("feedDbStorage") FeedStorage feedStorage) {
        this.reviewDbStorage = reviewDbStorage;
        this.storage = storage;
        this.userService = userService;
        this.feedStorage = feedStorage;
    }

    public List<Review> findAll() {
        return reviewDbStorage.findAll();
    }

    public Review addReview(Review review) throws ValidationException {
        if (isReviewValid(review)) {
            review = reviewDbStorage.create(review);
            feedStorage.addFeed(review.getUserId(), Feed.EventTypeList.REVIEW,
                    Feed.OperationTypeList.ADD, review.getReviewId());
            return review;
        }
        throw new ValidationException("Validation fail");
    }

    public Optional<Review> updateReview(Review review) throws ValidationException {
        if (isReviewValid(review)) {
            Optional<Review> updateReview = reviewDbStorage.update(review);
            if (updateReview.isPresent()) {
                feedStorage.addFeed(getReviewById(review.getReviewId()).get().getUserId(), Feed.EventTypeList.REVIEW,
                        Feed.OperationTypeList.UPDATE, review.getReviewId());
            }
            return updateReview;
        }
        throw new ValidationException("Validation fail");
    }

    public boolean deleteById(long reviewId) {
        feedStorage.addFeed(getReviewById(reviewId).get().getUserId(), Feed.EventTypeList.REVIEW,
                Feed.OperationTypeList.REMOVE, getReviewById(reviewId).get().getReviewId());
        return reviewDbStorage.deleteById(reviewId);
    }

    public Optional<Review> getReviewById(long reviewId) {
        return storage.findById(reviewId);
    }

    public Optional<List<Review>> getReviewSorted(Long filmId, int count) {
        if (filmId != null) {
            return Optional.of(reviewDbStorage.getReviewSorted(filmId, count));
        }
        return Optional.empty();
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

    private boolean isReviewValid(Review review) throws InvalidParameterException {
        if (review.getContent().length() > 1000) {
            throw new InvalidParameterException("отзыв содержит более 1000 символов");
        } else if (review.getUserId() < 0) {
            throw new InvalidParameterException("поле user_id не может быть пустым");
        } else if (review.getFilmId() < 0) {
            throw new InvalidParameterException("поле user_id не может быть пустым");
        } else if (review.getUserId() == 0) {
            throw new InvalidParameterException("поле user_id не может быть пустым");
        } else if (review.getContent().length() == 0) {
            throw new InvalidParameterException("контент отзыва не может быть пустым");
        }
        return true;
    }

}
