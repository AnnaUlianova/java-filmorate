package ru.yandex.practicum.filmorate.dbIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmDbTest {

    private final FilmDbStorage storage;
    private final UserDbStorage userStorage;

    @Test
    public void testFindFilmById() {
        // Given
        Set<Genre> genreSet = new HashSet<>();
        Genre genre = Genre.builder().id(1).name("Комедия").build();
        genreSet.add(genre);
        storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(143)
                .mpa(MPA.builder().id(2).name("PG").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .genres(genreSet)
                .build());
        // When
        Optional<Film> filmOptional = storage.findById(1);
        // Then
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                                .hasFieldOrPropertyWithValue("name",
                                        "Pirates of the Caribbean: The Curse of the Black Pearl")
                                .hasFieldOrPropertyWithValue("description",
                                        "American fantasy swashbuckler film")
                                .hasFieldOrPropertyWithValue("duration", 143)
                );
        assertEquals(2, filmOptional.get().getMpa().getId());
        assertEquals("PG", filmOptional.get().getMpa().getName());
        assertEquals(1, genreSet.size());
        assertTrue(genreSet.contains(genre));
    }

    @Test
    public void testFindFilmByWrongId() {
        // When
        Optional<Film> filmOptional = storage.findById(10);
        // Then
        assertThat(filmOptional).isEmpty();
    }

    @Test
    public void testFindAllFilms() {
        // Given
        storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(152)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        storage.create(Film.builder()
                .name("Harry Potter and the Philosopher's Stone")
                .description("Fantasy film based on J. K. Rowling's 1997 novel of the same name.")
                .duration(143)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2001, 11, 11))
                .build());
        // When
        List<Film> films = storage.findAll();
        // Then
        assertEquals(2, films.size());
        assertThat(films.get(0)).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name",
                        "Pirates of the Caribbean: The Curse of the Black Pearl");
        assertThat(films.get(1)).hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("name", "Harry Potter and the Philosopher's Stone");
    }

    @Test
    public void testDeleteById() {
        // Given
        storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(152)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        // When
        boolean isDeleted = storage.deleteById(1);
        // Then
        assertTrue(isDeleted);
        assertEquals(0, storage.findAll().size());
    }

    @Test
    public void testDeleteByWrongId() {
        // Given
        storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(152)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        // When
        boolean isDeleted = storage.deleteById(10);
        // Then
        assertFalse(isDeleted);
        assertEquals(1, storage.findAll().size());
    }

    @Test
    public void testCreate() {
        // When
        Film film = storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(152)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        // Then
        assertEquals(1, film.getId());
        assertEquals(1, storage.findAll().size());
    }

    @Test
    public void testUpdate() {
        // Given
        storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(152)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        // When
        storage.update(Film.builder()
                .id(1)
                .name("Pirates of the Caribbean: At World's End")
                .description("American fantasy swashbuckler film")
                .duration(169)
                .mpa(MPA.builder().id(3).name("PG-13").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        // Then
        assertThat(storage.findById(1).get())
                .hasFieldOrPropertyWithValue("name", "Pirates of the Caribbean: At World's End");
        assertEquals(1, storage.findAll().size());
    }

    @Test
    public void testAddLikeToFilm() {
        // Given
        userStorage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        storage.create(Film.builder()
                .name("Pirates of the Caribbean: The Curse of the Black Pearl")
                .description("American fantasy swashbuckler film")
                .duration(152)
                .mpa(MPA.builder().id(1).name("G").build())
                .releaseDate(LocalDate.of(2003, 7, 9))
                .build());
        // When
        boolean isLikeAdded = storage.addLikeToFilm(1, 1);
        // Then
        assertTrue(isLikeAdded);
        assertEquals(1, storage.findById(1).get().getLikes_count());
    }

    @Test
    public void testRemoveLikeFromFilm() {
        // Given
        testAddLikeToFilm();
        // When
        boolean isLikeRemoved = storage.removeLikeFromFilm(1, 1);
        // Then
        assertTrue(isLikeRemoved);
        assertEquals(0, storage.findById(1).get().getLikes_count());
    }
}
