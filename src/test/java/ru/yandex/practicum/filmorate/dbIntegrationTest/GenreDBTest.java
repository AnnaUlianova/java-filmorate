package ru.yandex.practicum.filmorate.dbIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GenreDBTest {

    private final GenreDbStorage storage;

    @Test
    public void testFindGenreById() {
        // When
        Optional<Genre> genreOptional = storage.findById(1);
        // Then
        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Комедия")
                );
    }

    @Test
    public void testFindGenreByWrongId() {
        // When
        Optional<Genre> genreOptional = storage.findById(10);
        // Then
        assertThat(genreOptional).isEmpty();
    }

    @Test
    public void testFindAllGenres() {
        // When
        List<Genre> genres = storage.findAll();
        // Then
        assertEquals(6, genres.size());
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
        assertThat(genres.get(1)).hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Драма");
    }

    @Test
    public void testDeleteById() {
        // When
        boolean isDeleted = storage.deleteById(1);
        // Then
        assertTrue(isDeleted);
        assertEquals(5, storage.findAll().size());
    }

    @Test
    public void testDeleteByWrongId() {
        // When
        boolean isDeleted = storage.deleteById(10);
        // Then
        assertFalse(isDeleted);
        assertEquals(6, storage.findAll().size());
    }

    @Test
    public void testCreate() {
        // When
        Genre genre = storage.create(Genre.builder().id(10).name("Приключения").build());
        // Then
        assertEquals(7, genre.getId());
        assertEquals(7, storage.findAll().size());
    }

    @Test
    public void testUpdate() {
        // When
        storage.update(Genre.builder().id(1).name("Приключения").build());
        // Then
        assertThat(storage.findById(1).get())
                .hasFieldOrPropertyWithValue("name", "Приключения");
        assertEquals(6, storage.findAll().size());
    }
}
