package ru.yandex.practicum.filmorate.dbIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserDBTest {

    private final UserDbStorage storage;

    @Test
    public void testFindUserById() {
        // Given
        storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        // When
        Optional<User> UserOptional = storage.findById(1);
        // Then
        assertThat(UserOptional)
                .isPresent()
                .hasValueSatisfying(User ->
                        assertThat(User).hasFieldOrPropertyWithValue("id", 1L)
                                .hasFieldOrPropertyWithValue("email", "mike@mail.ru")
                                .hasFieldOrPropertyWithValue("login", "Mike123")
                                .hasFieldOrPropertyWithValue("name", "Mike")
                );
    }

    @Test
    public void testFindUserByWrongId() {
        // When
        Optional<User> userOptional = storage.findById(10);
        // Then
        assertThat(userOptional).isEmpty();
    }

    @Test
    public void testFindAllUsers() {
        // Given
        storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        storage.create(User.builder()
                .email("tom@mail.ru")
                .login("Tom")
                .name("Tom")
                .birthday(LocalDate.of(1994, 1, 7))
                .build());
        // When
        List<User> users = storage.findAll();
        // Then
        assertEquals(2, users.size());
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(users.get(1)).hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("name", "Tom");
    }

    @Test
    public void testDeleteById() {
        // Given
        storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
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
        storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
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
        User user = storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        // Then
        assertEquals(1, user.getId());
        assertEquals(1, storage.findAll().size());
    }

    @Test
    public void testUpdate() {
        // Given
        storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        // When
        storage.update(User.builder()
                .id(1L)
                .email("kate@mail.ru")
                .login("Katy")
                .name("Kate")
                .birthday(LocalDate.of(2005, 5, 1))
                .build());
        // Then
        assertThat(storage.findById(1).get())
                .hasFieldOrPropertyWithValue("name", "Kate");
        assertEquals(1, storage.findAll().size());
    }

    @Test
    public void testAddToFriends() {
        // Given
        storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        User user2 = storage.create(User.builder()
                .email("tom@mail.ru")
                .login("Tom")
                .name("Tom")
                .birthday(LocalDate.of(1994, 1, 7))
                .build());
        // When
        boolean isRequested = storage.addToFriends(1, 2);
        // Then
        assertTrue(isRequested);
        // Request for friendship
        assertEquals(1, storage.getListOfFriends(1).size());
        assertTrue(storage.getListOfFriends(1).contains(user2));
        assertEquals(0, storage.getListOfFriends(2).size());
    }

    @Test
    public void testAddToFriendsMutual() {
        // Given
        User user1 = storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        User user2 = storage.create(User.builder()
                .email("tom@mail.ru")
                .login("Tom")
                .name("Tom")
                .birthday(LocalDate.of(1994, 1, 7))
                .build());
        // When
        boolean isAdded = storage.addToFriends(1, 2) && storage.addToFriends(2, 1);
        // Then
        assertTrue(isAdded);
        // Mutual
        assertEquals(1, storage.getListOfFriends(1).size());
        assertTrue(storage.getListOfFriends(1).contains(user2));
        assertEquals(1, storage.getListOfFriends(2).size());
        assertTrue(storage.getListOfFriends(2).contains(user1));
    }

    @Test
    public void testDeleteFromMutualFriendship() {
        // Given
        User user1 = storage.create(User.builder()
                .email("mike@mail.ru")
                .login("Mike123")
                .name("Mike")
                .birthday(LocalDate.of(2000, 4, 5))
                .build());
        storage.create(User.builder()
                .email("tom@mail.ru")
                .login("Tom")
                .name("Tom")
                .birthday(LocalDate.of(1994, 1, 7))
                .build());
        storage.addToFriends(1, 2);
        storage.addToFriends(2, 1);
        // When
        boolean isDeleted = storage.deleteFromFriends(1, 2);
        // Then
        assertTrue(isDeleted);

        assertEquals(0, storage.getListOfFriends(1).size());
        assertEquals(1, storage.getListOfFriends(2).size());
        assertTrue(storage.getListOfFriends(2).contains(user1));
    }
}
