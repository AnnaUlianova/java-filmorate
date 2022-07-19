package ru.yandex.practicum.filmorate.storage.genre;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Component
public class InMemoryGenreStorage implements GenreStorage {

    @Getter
    private final Map<Integer, Genre> genres;
    private static int genreId;

    public InMemoryGenreStorage() {
        genres = new HashMap<>();
        genres.put(generateId(), Genre.builder().id(genreId).name("Комедия").build());
        genres.put(generateId(), Genre.builder().id(genreId).name("Драма").build());
        genres.put(generateId(), Genre.builder().id(genreId).name("Мультфильм").build());
        genres.put(generateId(), Genre.builder().id(genreId).name("Триллер").build());
        genres.put(generateId(), Genre.builder().id(genreId).name("Документальный").build());
        genres.put(generateId(), Genre.builder().id(genreId).name("Боевик").build());
    }

    private int generateId() {
        return ++genreId;
    }

    @Override
    public Genre create(Genre genre) {
        genre.setId(generateId());
        genres.put(genreId, genre);
        return genre;
    }

    @Override
    public Optional<Genre> update(Genre genre) {
        if (genres.containsKey(genre.getId())) {
            genres.put(genre.getId(), genre);
            return Optional.of(genre);
        }
        return Optional.empty();
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findById(long id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public boolean deleteById(long id) {
        return genres.remove(id) != null;
    }
}
