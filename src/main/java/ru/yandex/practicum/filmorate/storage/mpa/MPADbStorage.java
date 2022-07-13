package ru.yandex.practicum.filmorate.storage.mpa;

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

    public MPADbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MPA create(MPA mpa) {
        String sqlQuery = "INSERT INTO ratings(name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"rating_id"});
            stmt.setString(1, mpa.getName());
            return stmt;
        }, keyHolder);
        mpa.setId(keyHolder.getKey().intValue());
        return mpa;
    }

    @Override
    public Optional<MPA> update(MPA mpa) {
        String sqlQuery = "UPDATE ratings SET name = ? WHERE rating_id = ?";
        boolean isUpdated = jdbcTemplate.update(sqlQuery, mpa.getName(), mpa.getId()) > 0;
        return isUpdated ? Optional.of(mpa) : Optional.empty();
    }

    @Override
    public List<MPA> findAll() {
        String sqlQuery = "SELECT * FROM ratings ORDER BY rating_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMPA);
    }

    @Override
    public Optional<MPA> findById(long id) {
        String sqlQuery = "SELECT * FROM ratings WHERE rating_id = ?";
        List<MPA> result = jdbcTemplate.query(sqlQuery, this::mapRowToMPA, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public boolean deleteById(long id) {
        String sqlQuery = "DELETE FROM ratings where rating_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    private MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("rating_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
