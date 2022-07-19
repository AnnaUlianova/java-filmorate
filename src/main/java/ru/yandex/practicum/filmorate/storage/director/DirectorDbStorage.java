package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class DirectorDbStorage implements DirectorStorage {

    private static final String CREATE_DIRECTOR = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_DIRECTOR = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String FIND_DIRECTOR = "SELECT * FROM directors where director_id = ?";
    private static final String FIND_ALL_DIRECTORS = "SELECT * FROM directors ORDER BY director_id";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors where director_id = ?";
    private static final String FIND_FILM_DIRECTORS =
            "SELECT d.* FROM directors d, films_directors fd WHERE fd.film_id = ? AND d.director_id = fd.director_id";

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_DIRECTOR, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Optional<Director> update(Director director) {
        boolean isUpdated = jdbcTemplate.update(UPDATE_DIRECTOR, director.getName(), director.getId()) > 0;
        return isUpdated ? Optional.of(director) : Optional.empty();
    }

    @Override
    public List<Director> findAll() {
        return jdbcTemplate.query(FIND_ALL_DIRECTORS, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> findById(long id) {
        try {
            Director director = jdbcTemplate.queryForObject(FIND_DIRECTOR, this::mapRowToDirector, id);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(long id) {
        return jdbcTemplate.update(DELETE_DIRECTOR, id) > 0;
    }

    @Override
    public Collection<Director> getFilmDirectors(long filmId) {
        return jdbcTemplate.query(FIND_FILM_DIRECTORS, this::mapRowToDirector, filmId);
    }

    public Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
