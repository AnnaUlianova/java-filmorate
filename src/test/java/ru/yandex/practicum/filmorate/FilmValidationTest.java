package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void test1_shouldHaveNoViolations() {
        //Given
        Film film = new Film();
        film.setName("Pirates of the Caribbean: The Curse of the Black Pearl");
        film.setDescription("American fantasy swashbuckler film");
        film.setDuration(143);
        film.setReleaseDate(LocalDate.of(2003, 7, 9));

        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        //Then
        assertTrue(violations.isEmpty());
    }

    @Test
    public void test2_shouldDetectInvalidName() {
        //Given
        Film film = new Film();
        film.setName("");
        film.setDescription("American fantasy swashbuckler film");
        film.setDuration(143);
        film.setReleaseDate(LocalDate.of(2003, 7, 9));

        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Name is required", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());

    }

    @Test
    public void test3_shouldDetectInvalidDescriptionForEmptyData() {
        //Given
        Film film = new Film();
        film.setName("Pirates of the Caribbean: The Curse of the Black Pearl");
        film.setDescription("");
        film.setDuration(143);
        film.setReleaseDate(LocalDate.of(2003, 7, 9));

        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Description is required", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    public void test4_shouldDetectInvalidDescriptionForTooManyCharacters() {
        //Given
        Film film = new Film();
        film.setName("Pirates of the Caribbean: The Curse of the Black Pearl");
        film.setDescription("In 1720, while sailing to Port Royal, Jamaica aboard HMS Dauntless, " +
                        "Governor Weatherby Swann, his daughter Elizabeth and crew encounter a shipwreck " +
                        "and recover a boy, Will Turner. Elizabeth discovers a golden pirate medallion " +
                        "around his neck, and takes it. Eight years later, Captain James Norrington " +
                        "is promoted to commodore and proposes to Elizabeth. Her corset makes her faint " +
                        "and fall into the sea, causing the medallion to emit a pulse. Captain Jack Sparrow, " +
                        "having just arrived in Port Royal to commandeer a ship, rescues Elizabeth. " +
                        "Norrington identifies Jack as a pirate, and a chase ensues.");
        film.setDuration(143);
        film.setReleaseDate(LocalDate.of(2003, 7, 9));

        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Description must be no longer than 200 characters", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    public void test5_shouldDetectInvalidDurationForNegativeValue() {
        //Given
        Film film = new Film();
        film.setName("Pirates of the Caribbean: The Curse of the Black Pearl");
        film.setDescription("American fantasy swashbuckler film");
        film.setDuration(-143);
        film.setReleaseDate(LocalDate.of(2003, 7, 9));

        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Duration must be positive", violation.getMessage());
        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals(-143, violation.getInvalidValue());
    }

    @Test
    public void test6_shouldDetectInvalidDurationForZeroValue() {
        //Given
        Film film = new Film();
        film.setName("Pirates of the Caribbean: The Curse of the Black Pearl");
        film.setDescription("American fantasy swashbuckler film");
        film.setDuration(0);
        film.setReleaseDate(LocalDate.of(2003, 7, 9));

        //When
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Duration must be positive", violation.getMessage());
        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals(0, violation.getInvalidValue());
    }

    @Test
    public void test7_shouldDetectInvalidReleaseDate() {
        //Given
        Film film = new Film();
        film.setName("Pirates of the Caribbean: The Curse of the Black Pearl");
        film.setDescription("American fantasy swashbuckler film");
        film.setDuration(143);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        //When
        FilmService service = new FilmService();

        //Then
        assertThrows(ValidationException.class,
                () -> service.createFilm(film),
                "Release date should be later than 28.12.1895");
    }

    @AfterAll
    public static void close() {
        validatorFactory.close();
    }
}
