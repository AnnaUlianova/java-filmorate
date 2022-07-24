package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(
        value = "/films",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class FilmController {

    private final FilmService service;

    @GetMapping
    public ResponseEntity<List<Film>> findAllFilms() {
        return new ResponseEntity<>(service.findAllFilms(), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) throws ValidationException {
        return new ResponseEntity<>(service.createFilm(film), HttpStatus.CREATED);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) throws ValidationException {
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
        return service.deleteFilmById(id) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> likeFilm(@PathVariable long id, @PathVariable long userId) {
        return service.likeFilm(id, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> removeLikeOfFilm(@PathVariable long id, @PathVariable long userId) {
        return service.removeLikeFromFilm(id, userId) ? new ResponseEntity<>(null, HttpStatus.OK)
                : new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/popular")
    public ResponseEntity<List<Film>> findTopLikableFilms(@RequestParam(defaultValue = "10") long count,
                                                          @RequestParam Optional<Integer> genreId,
                                                          @RequestParam Optional<Integer> year) {
        if (genreId.isPresent() && year.isPresent()) {
            return service.findTopFilmsByGenreAndYear(count, genreId.get(), year.get())
                    .map(films -> new ResponseEntity<>(films, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        } else if (genreId.isPresent()) {
            return service.findTopFilmsByGenre(count, genreId.get())
                    .map(films -> new ResponseEntity<>(films, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        } else if (year.isPresent()) {
            return service.findTopFilmsByYear(count, year.get())
                    .map(films -> new ResponseEntity<>(films, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        } else {
            return new ResponseEntity<>(service.findTopLikableFilms(count), HttpStatus.OK);
        }
    }

    // GET /films/director/{directorId}?sortBy=[year,likes]
    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<Film>> findDirectorFilms(@PathVariable long directorId,
                                                              @RequestParam(defaultValue = "id") String sortBy) {
        Collection<Film> films = service.getDirectorFilms(directorId, sortBy);
        if (films.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(films, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<Film>> findFilmsByConditions(
            @RequestParam Optional<String> query,
            @RequestParam Optional<String> by
    ) {
        if (query.isPresent() && by.isPresent()) {
            if (by.get().equals("title")) {
                Collection<Film> films = service.findTopFilmsByTitleFragment(query.get());
                return new ResponseEntity<>(films, HttpStatus.OK);

            } else if (by.get().equals("director")) {
                Collection<Film> films = service.findTopFilmsByDirectorFragment(query.get());
                return new ResponseEntity<>(films, HttpStatus.OK);

            } else if (by.get().equals("title,director") || by.get().equals("director,title")) {
                Collection<Film> films = service.findTopFilmsByTitleAndDirectorFragment(query.get());
                return new ResponseEntity<>(films, HttpStatus.OK);
            }
        } else {
            Collection<Film> films = service.findAllFilmsByLikes();
            return new ResponseEntity<>(films, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
