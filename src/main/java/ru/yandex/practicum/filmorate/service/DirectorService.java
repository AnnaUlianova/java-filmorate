package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class DirectorService {

    private final DirectorStorage storage;

    public DirectorService(@Qualifier("directorDbStorage") DirectorStorage storage) {
        this.storage = storage;
    }

    public Director createDirector(Director director) {
        return storage.create(director);
    }

    public Optional<Director> updateDirector(Director director) {
        return storage.update(director);
    }

    public List<Director> findAllDirectors() {
        return storage.findAll();
    }

    public Optional<Director> findDirectorById(long id) {
        return storage.findById(id);
    }

    public boolean deleteDirectorById(long id) {
        return storage.deleteById(id);
    }

    public Collection<Director> getFilmDirectors(long filmId) {
        return storage.getFilmDirectors(filmId);
    }
}
