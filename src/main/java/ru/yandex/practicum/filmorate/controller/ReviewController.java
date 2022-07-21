package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@RequestBody Review review) throws ValidationException {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) throws ValidationException {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable("id") long reviewId) {
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") long reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getMostUsefulReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10", required = false) int count) {
        if (filmId != null) {
            if (filmId < 0) {
                throw new FilmNotFoundException("Фильм не найден");
            } else if (count < 0) {
                throw new IncorrectParameterException("Некорректно указан параметр count");
            }
        }
        return reviewService.getReviewSorted(filmId, count);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewService.addLikeToReview(reviewId, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislikeToReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLikeFromReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewService.deleteLikeFromReview(reviewId, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeDislikeFromReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewService.deleteDislikeFromReview(reviewId, userId);
    }
}

