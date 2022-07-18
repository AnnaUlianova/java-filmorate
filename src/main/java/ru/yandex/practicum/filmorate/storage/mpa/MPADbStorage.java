package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class MPADbStorage implements MPAStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String CREATE_MPA = "INSERT INTO ratings(name) VALUES (?)";
    private static final String UPDATE_MPA = "UPDATE ratings SET name = ? WHERE rating_id = ?";
    private static final String FIND_MPA = "SELECT * FROM ratings WHERE rating_id = ?";
    private static final String FIND_ALL_MPA = "SELECT * FROM ratings ORDER BY rating_id";
    private static final String DELETE_MPA = "DELETE FROM ratings WHERE rating_id = ?";

    public MPADbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MPA create(MPA mpa) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_MPA, new String[]{"rating_id"});
            stmt.setString(1, mpa.getName());
            return stmt;
        }, keyHolder);
        mpa.setId(keyHolder.getKey().intValue());
        return mpa;
    }

    @Override
    public Optional<MPA> update(MPA mpa) {
        boolean isUpdated = jdbcTemplate.update(UPDATE_MPA, mpa.getName(), mpa.getId()) > 0;
        return isUpdated ? Optional.of(mpa) : Optional.empty();
    }

    @Override
    public List<MPA> findAll() {
        return jdbcTemplate.query(FIND_ALL_MPA, this::mapRowToMPA);
    }

    @Override
    public Optional<MPA> findById(long id) {
        try {
            MPA mpa = jdbcTemplate.queryForObject(FIND_MPA, this::mapRowToMPA, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(long id) {
        return jdbcTemplate.update(DELETE_MPA, id) > 0;
    }

    private MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("rating_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
