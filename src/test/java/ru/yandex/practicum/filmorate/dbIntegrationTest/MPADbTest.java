package ru.yandex.practicum.filmorate.dbIntegrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MPADbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MPADbTest {

    private final MPADbStorage storage;

    @Test
    public void testFindMPAById() {
        // When
        Optional<MPA> mpaOptional = storage.findById(1);
        // Then
        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "G")
                );
    }

    @Test
    public void testFindMPAByWrongId() {
        // When
        Optional<MPA> mpaOptional = storage.findById(10);
        // Then
        assertThat(mpaOptional).isEmpty();
    }

    @Test
    public void testFindAllMPAs() {
        // When
        List<MPA> mpas = storage.findAll();
        // Then
        assertEquals(5, mpas.size());
        assertThat(mpas.get(0)).hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
        assertThat(mpas.get(1)).hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "PG");
    }

    @Test
    public void testDeleteById() {
        // When
        boolean isDeleted = storage.deleteById(1);
        // Then
        assertTrue(isDeleted);
        assertEquals(4, storage.findAll().size());
    }

    @Test
    public void testDeleteByWrongId() {
        // When
        boolean isDeleted = storage.deleteById(10);
        // Then
        assertFalse(isDeleted);
        assertEquals(5, storage.findAll().size());
    }

    @Test
    public void testCreate() {
        // When
        MPA mpa = storage.create(MPA.builder().id(10).name("ABC").build());
        // Then
        assertEquals(6, mpa.getId());
        assertEquals(6, storage.findAll().size());
    }

    @Test
    public void testUpdate() {
        // When
        storage.update(MPA.builder().id(1).name("ABC").build());
        // Then
        assertThat(storage.findById(1).get())
                .hasFieldOrPropertyWithValue("name", "ABC");
        assertEquals(5, storage.findAll().size());
    }
}
