package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage storage;
    private final UserService userService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) { // inMemoryFilmStorage
        this.storage = filmStorage;
        this.userService = userService;
    }

    public Film createFilm(Film film) {
        return storage.create(film);
    }

    public Optional<Film> updateFilm(Film film) {
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
            return storage.addLikeToFilm(id, userId);
        }
        return false;
    }

    public boolean removeLikeFromFilm(long id, long userId) {
        Optional<User> optUser = userService.findUserById(userId);
        Optional<Film> optFilm = storage.findById(id);

        if (optUser.isPresent() && optFilm.isPresent() && optFilm.get().getLikes_count() > 0) {
            return storage.removeLikeFromFilm(id, userId);
        }
        return false;
    }

    public List<Film> findTopLikableFilms(long count) {
        return storage.findAll().stream().sorted(Comparator.comparing(Film::getLikes_count).reversed()).limit(count).collect(Collectors.toList());
    }

    public Optional<List<Film>> findTopCommonFilms(long userId, long friendId) {
        if (userService.findUserById(userId).isPresent() && userService.findUserById(friendId).isPresent()) {
            return Optional.of(storage.getUserFilms(userId).stream().filter(storage.getUserFilms(friendId)::contains).collect(Collectors.toList()));
        }
        return Optional.empty();
    }
}