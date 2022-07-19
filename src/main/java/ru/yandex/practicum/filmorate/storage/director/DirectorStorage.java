package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;

public interface DirectorStorage extends Storage<Director> {
    Collection<Director> getFilmDirectors(long filmId);
}
