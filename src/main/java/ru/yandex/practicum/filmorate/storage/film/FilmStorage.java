package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface FilmStorage extends Storage<Film> {

    boolean removeLikeFromFilm(long id, long userId);

    boolean addLikeToFilm(long id, long userId);

    List<Film> findTopFilmsByGenreAndYear(long count, int genreId, int year);
    List<Film> findTopFilmsByGenre(long count, int genreId);
    List<Film> findTopFilmsByYear(long count, int year);
    List<Film> findTopLikableFilms(long count);
}
