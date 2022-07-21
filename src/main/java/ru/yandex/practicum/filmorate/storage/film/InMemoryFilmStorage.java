package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.InMemoryMPAStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final InMemoryGenreStorage genreStorage;
    private final InMemoryMPAStorage mpaStorage;

    private final Map<Long, Film> films;
    private final Map<Long, Set<Long>> likes;
    private static long filmId;

    public InMemoryFilmStorage(InMemoryGenreStorage genreStorage,
                               InMemoryMPAStorage mpaStorage) {
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        films = new HashMap<>();
        likes = new HashMap<>();
    }

    private long generateId() {
        return ++filmId;
    }

    private void validateRating(Film film) {
        film.setMpa(mpaStorage.getRatings().getOrDefault(film.getMpa().getId(), null));
        if (film.getMpa() == null) {
            throw new RuntimeException("Rating should be valid");
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() != null) {
            Set<Genre> treeSet = new TreeSet<>(Comparator.comparing(Genre::getId));
            treeSet.addAll(film.getGenres().stream()
                    .filter(genre -> genreStorage.getGenres().containsKey(genre.getId()))
                    .collect(Collectors.toSet()));
            for (Genre genre : treeSet) {
                genre.setName(genreStorage.getGenres().get(genre.getId()).getName());
            }
            film.setGenres(treeSet);
        }
    }

    @Override
    public Film create(Film film) {
        validateRating(film);
        validateGenres(film);

        film.setId(generateId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        if (films.containsKey(film.getId())) {
            validateRating(film);
            validateGenres(film);

            films.put(film.getId(), film);
            return Optional.of(film);
        }
        return Optional.empty();
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
    public boolean deleteById(long id) {
        return films.remove(id) != null;
    }

    @Override
    public boolean removeLikeFromFilm(long id, long userId) {
        if (likes.containsKey(id)) {
            likes.get(id).remove(userId);
            films.get(id).setLikes_count(likes.get(id).size());
            return true;
        }
        return false;
    }

    @Override
    public boolean addLikeToFilm(long id, long userId) {
        if (likes.containsKey(id)) {
            likes.get(id).add(userId);
        } else {
            Set<Long> filmLikes = new HashSet<>();
            filmLikes.add(userId);
            likes.put(id, filmLikes);
        }
        films.get(id).setLikes_count(likes.get(id).size());
        return true;
    }

    @Override
    public List<Film> getUserFilms(long userId) {
        return null;
    }

}
