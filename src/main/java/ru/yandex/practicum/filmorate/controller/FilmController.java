package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(
        value = "/films",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class FilmController {

    private final FilmService service;

    @Autowired
    public FilmController(FilmService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Film>> findAllFilms() {
        return new ResponseEntity<>(service.findAllFilms(), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        return new ResponseEntity<>(service.createFilm(film), HttpStatus.CREATED);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        return service.updateFilm(film).map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> findFilmById(@PathVariable long id) {
        return service.findFilmById(id).map(film -> new ResponseEntity<>(film, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Film> deleteFilmById(@PathVariable long id) {
        return service.deleteFilmById(id).map(film -> new ResponseEntity<>(film, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> likeFilm(@PathVariable long id, @PathVariable long userId) {
        return service.likeFilm(id, userId).map(film -> new ResponseEntity<>(film, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> removeLikeOfFilm(@PathVariable long id, @PathVariable long userId) {
        return service.removeLikeOfFilm(id, userId).map(film -> new ResponseEntity<>(film, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> findTopLikableFilms(@RequestParam(defaultValue = "10") long count) {
        return new ResponseEntity<>(service.findTopLikableFilms(count), HttpStatus.OK);
    }

}
