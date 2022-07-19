package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String CREATE_GENRE = "INSERT INTO genres(name) VALUES (?)";
    private static final String UPDATE_GENRE = "UPDATE genres SET name = ? WHERE genre_id = ?";
    private static final String FIND_GENRE = "SELECT * FROM genres where genre_id = ?";
    private static final String FIND_ALL_GENRES = "SELECT * FROM genres ORDER BY genre_id";
    private static final String DELETE_GENRE = "DELETE FROM genres where genre_id = ?";

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre create(Genre genre) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_GENRE, new String[]{"genre_id"});
            stmt.setString(1, genre.getName());
            return stmt;
        }, keyHolder);
        genre.setId(keyHolder.getKey().intValue());
        return genre;
    }

    @Override
    public Optional<Genre> update(Genre genre) {
        boolean isUpdated = jdbcTemplate.update(UPDATE_GENRE, genre.getName(), genre.getId()) > 0;
        return isUpdated ? Optional.of(genre) : Optional.empty();
    }

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(long id) {
        try {
            Genre genre = jdbcTemplate.queryForObject(FIND_GENRE, this::mapRowToGenre, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(long id) {
        return jdbcTemplate.update(DELETE_GENRE, id) > 0;
    }

    public Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
