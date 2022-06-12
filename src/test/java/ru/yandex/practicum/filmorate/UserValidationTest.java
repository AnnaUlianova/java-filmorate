package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        User user = new User();
        user.setEmail("mike@mail.ru");
        user.setLogin("Mike123");
        user.setName("Mike");
        user.setBirthday(LocalDate.of(2000, 4, 5));

        //When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        //Then
        assertTrue(violations.isEmpty());
    }

    @Test
    public void test2_shouldDetectInvalidEmailForEmptyData() {
        //Given
        User user = new User();
        user.setLogin("Mike123");
        user.setName("Mike");
        user.setBirthday(LocalDate.of(2000, 4, 5));

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
        User user = new User();
        user.setEmail("mike.ru@");
        user.setLogin("Mike123");
        user.setName("Mike");
        user.setBirthday(LocalDate.of(2000, 4, 5));

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
        User user = new User();
        user.setEmail("mike@mail.ru");
        user.setName("Mike");
        user.setBirthday(LocalDate.of(2000, 4, 5));

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
        User user = new User();
        user.setEmail("mike@mail.ru");
        user.setLogin("Mike Smith  ");
        user.setName("Mike");
        user.setBirthday(LocalDate.of(2000, 4, 5));

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
        User user = new User();
        user.setEmail("mike@mail.ru");
        user.setLogin("Mike");
        user.setBirthday(LocalDate.of(2000, 4, 5));

        //When
        UserStorage storage = new InMemoryUserStorage();
        storage.create(user);

        //Then
        assertEquals("Mike", user.getName());
    }

    @Test
    public void test7_shouldDetectInvalidBirthdayForDateInTheFuture() {
        //Given
        User user = new User();
        user.setEmail("mike@mail.ru");
        user.setLogin("Mike123");
        user.setName("Mike");
        user.setBirthday(LocalDate.of(2023, 1, 7));

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

