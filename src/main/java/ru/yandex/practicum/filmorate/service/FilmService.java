package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage storage;
    private final GenreStorage genreStorage;
    private final UserService userService;
    private final FeedStorage feedStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage,
                       @Qualifier("feedDbStorage") FeedStorage feedStorage) { // inMemoryFilmStorage
        this.storage = filmStorage;
        this.userService = userService;
        this.genreStorage = genreStorage;
        this.feedStorage = feedStorage;
    }

    public Film createFilm(Film film) {
        validateReleaseDate(film);
        return storage.create(film);
    }

    public Optional<Film> updateFilm(Film film) {
        validateReleaseDate(film);
        return storage.update(film);
    }

    public List<Film> findAllFilms() {
        return storage.findAll();
    }

    public Optional<Film> findFilmById(long id) {
        return storage.findById(id);
    }

    public boolean deleteFilmById(long id) {
        return storage.deleteById(id);
    }

    public boolean likeFilm(long id, long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Film> optFilm = storage.findById(id);

        if (optUser.isPresent() && optFilm.isPresent()) {
            feedStorage.addFeed(userId, Feed.EventTypeList.LIKE, Feed.OperationTypeList.ADD,id);
            return storage.addLikeToFilm(id, userId);
        }
        return false;
    }

    public boolean removeLikeFromFilm(long id, long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Film> optFilm = storage.findById(id);

        if (optUser.isPresent() && optFilm.isPresent() && optFilm.get().getLikes_count() > 0) {
            feedStorage.addFeed(userId, Feed.EventTypeList.LIKE, Feed.OperationTypeList.REMOVE,id);
            return storage.removeLikeFromFilm(id, userId);
        }
        return false;
    }

    public List<Film> findTopLikableFilms(long count) {
        return storage.findTopLikableFilms(count);
    }

    public Optional<List<Film>> findTopFilmsByYear(long count, int year) {
        if (year > CINEMA_BIRTHDAY.getYear()) {
            return Optional.of(storage.findTopFilmsByYear(count, year));
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<Film>> findTopFilmsByGenre(long count, int genreId) {
        if (genreStorage.findById(genreId).isPresent()) {
            return Optional.of(storage.findTopFilmsByGenre(count, genreId));
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<Film>> findTopFilmsByGenreAndYear(long count, int genreId, int year) {
        if (genreStorage.findById(genreId).isPresent() && year > CINEMA_BIRTHDAY.getYear()) {
            return Optional.of(storage.findTopFilmsByGenreAndYear(count, genreId, year));
        } else {
            return Optional.empty();
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Release date should be later than 28.12.1895");
        }
    }

    public Collection<Film> getDirectorFilms(long directorId, String sortBy) {
        return storage.getDirectorFilms(directorId, sortBy);
    }
}