package ru.yandex.practicum.filmorate.storage.genre;

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

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre create(Genre genre) {
        String sqlQuery = "INSERT INTO genres(name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"genre_id"});
            stmt.setString(1, genre.getName());
            return stmt;
        }, keyHolder);
        genre.setId(keyHolder.getKey().intValue());
        return genre;
    }

    @Override
    public Optional<Genre> update(Genre genre) {
        String sqlQuery = "UPDATE genres SET name = ? WHERE genre_id = ?";
        boolean isUpdated = jdbcTemplate.update(sqlQuery, genre.getName(), genre.getId()) > 0;
        return isUpdated ? Optional.of(genre) : Optional.empty();
    }

    @Override
    public List<Genre> findAll() {
        String sqlQuery = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(long id) {
        String sqlQuery = "SELECT * FROM genres where genre_id = ?";
        Genre genre = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
        return Optional.ofNullable(genre);
    }

    @Override
    public boolean deleteById(long id) {
        String sqlQuery = "DELETE FROM genres where genre_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    public Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
