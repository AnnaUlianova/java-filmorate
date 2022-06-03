package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilmService {

    private final Map<Long, Film> films;

    private static long filmId;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService() {
        films = new HashMap<>();
    }

    public Film createFilm(Film film) {
        validate(film);
        film.setId(++filmId);
        films.put(filmId, film);
        log.info("Created film: {}", films.get(film.getId()));
        return films.get(film.getId());
    }

    public ResponseEntity<Film> updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Updated film: {}", films.get(film.getId()));
            return new ResponseEntity<>(films.get(film.getId()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public List<Film> findAllFilms() {
        log.debug("Current amount of films: {}", films.size());
        return new ArrayList<>(films.values());
    }

    private void validate(Film data) {
        if (CINEMA_BIRTHDAY.isAfter(data.getReleaseDate())) {
            throw new ValidationException("Release date should be later than 28.12.1895");
        }
    }
}
