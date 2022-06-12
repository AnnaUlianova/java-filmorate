package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films;

    private static long filmId;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
    }

    private long generateId() {
        return ++filmId;
    }

    @Override
    public Film create(Film film) {
        film.setId(generateId());
        films.put(filmId, film);
        return films.get(film.getId());
    }

    @Override
    public Optional<Film> update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        }
        return Optional.ofNullable(films.get(film.getId()));
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Optional<Film> deleteById(long id) {
        return Optional.ofNullable(films.remove(id));
    }
}
