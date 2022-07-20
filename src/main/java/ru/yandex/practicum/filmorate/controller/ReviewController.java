package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewDaoService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewDaoService reviewDaoService;

    @Autowired
    public ReviewController(ReviewDaoService reviewDaoService) {
        this.reviewDaoService = reviewDaoService;
    }

    @PostMapping
    public Review addReview(@RequestBody Review review) throws ValidationException {
        return reviewDaoService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) throws ValidationException {
        return reviewDaoService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable("id") long reviewId) {
        reviewDaoService.deleteReview(reviewId);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") long reviewId) {
        return reviewDaoService.getReviewById(reviewId);
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
        return reviewDaoService.getReviewSorted(filmId, count);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewDaoService.addLikeToReview(reviewId, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislikeToReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewDaoService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLikeFromReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewDaoService.deleteLikeFromReview(reviewId, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeDislikeFromReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        reviewDaoService.deleteDislikeFromReview(reviewId, userId);
    }
}

