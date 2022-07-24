package ru.yandex.practicum.filmorate.validationTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

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
        User user = User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build();

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertTrue(violations.isEmpty());
    }

    @Test
    public void test2_shouldDetectInvalidEmailForEmptyData() {
        //Given
        User user = User.builder()
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build();

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Email is required", violation.getMessage());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    public void test3_shouldDetectInvalidEmailForWrongData() {
        //Given
        User user = User.builder()
                .email("mike.ru@")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build();

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Email should be valid", violation.getMessage());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    public void test4_shouldDetectInvalidLoginForEmptyData() {
        //Given
        User user = User.builder()
                .email("mike@mail.ru")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build();

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Login is required", violation.getMessage());
        assertEquals("login", violation.getPropertyPath().toString());
    }

    @Test
    public void test5_shouldDetectInvalidLoginForWrongData() {
        //Given
        User user = User.builder()
                .email("mike@mail.ru")
                .login("Mike Smith  ")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build();

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Login without white spaces is required", violation.getMessage());
        assertEquals("login", violation.getPropertyPath().toString());
    }

    @Test
    public void test6_shouldUseLoginForEmptyName() {
        //Given
        User user = User.builder()
                .email("mike@mail.ru")
                .login("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build();

        //When
        UserService service = new UserService(new InMemoryUserStorage(), null);
        service.createUser(user);

        //Then
        assertEquals("Mike", user.getName());
    }

    @Test
    public void test7_shouldDetectInvalidBirthdayForDateInTheFuture() {
        //Given
        User user = User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2033, 1, 7))
                .build();

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertEquals(violations.size(), 1);

        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("Date must be in the past", violation.getMessage());
        assertEquals("birthday", violation.getPropertyPath().toString());
    }

    @AfterAll
    public static void close() {
        validatorFactory.close();
    }
}

