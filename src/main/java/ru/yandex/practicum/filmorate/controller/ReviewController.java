package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) throws ValidationException {
        return service.addReview(review);
    }

    @PutMapping
    public ResponseEntity<Review> updateReview(@Valid @RequestBody Review review) throws ValidationException {
        return service.updateReview(review).map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Review> removeReview(@PathVariable("id") long reviewId) {
        return service.deleteById(reviewId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable("id") long reviewId) {
        return service.getReviewById(reviewId).map(film -> new ResponseEntity<>(film, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Review>> getMostUsefulReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10", required = false) int count) {
        if (filmId != null) {
            if (filmId < 0) {
                throw new FilmNotFoundException("Фильм не найден");
            } else if (count < 0) {
                throw new IncorrectParameterException("Некорректно указан параметр count");
            }
        }
        return new ResponseEntity<>(service.getReviewSorted(filmId, count), HttpStatus.OK);
    }

    @PutMapping("{id}/like/{userId}")
    public ResponseEntity<Review> addLikeToReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        return service.addLikeToReview(reviewId, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PutMapping("{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislikeToReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        return service.addDislikeToReview(reviewId, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

    }

    @DeleteMapping("{id}/like/{userId}")
    public ResponseEntity<Review> removeLikeFromReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        return service.deleteLikeFromReview(reviewId, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

    }

    @DeleteMapping("{id}/dislike/{userId}")
    public ResponseEntity<Review> removeDislikeFromReview(
            @PathVariable("id") Long reviewId,
            @PathVariable Long userId) {
        return service.deleteDislikeFromReview(reviewId, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

    }
}

