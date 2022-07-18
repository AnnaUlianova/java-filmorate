package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO users(email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        long userId = keyHolder.getKey().longValue();
        user.setId(userId);
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        String sqlQuery = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?";
        boolean isUpdated = jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()) > 0;

        // Delete all previous user's connections
        if (isUpdated) {
            jdbcTemplate.update("DELETE FROM friendship WHERE to_user_id = ? OR from_user_id = ?",
                    user.getId(), user.getId());
        }
        return isUpdated ? Optional.of(user) : Optional.empty();
    }

    @Override
    public boolean deleteById(long id) {
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";
        boolean isDeleted = jdbcTemplate.update(sqlQuery, id) > 0;
        jdbcTemplate.update("DELETE FROM friendship WHERE to_user_id = ? OR from_user_id = ?", id, id);
        return isDeleted;
    }

    @Override
    public List<User> findAll() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public Optional<User> findById(long id) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> getListOfFriends(long id) {
        String sqlQuery = "SELECT * FROM friendship f " +
                "LEFT JOIN users u ON f.to_user_id = u.user_id " +
                "WHERE from_user_id = ?";

        String sqlQueryMutual = "SELECT * FROM friendship f " +
                "LEFT JOIN users u ON f.from_user_id = u.user_id " +
                "WHERE to_user_id = ? AND accepted = ?";

        List<User> users = jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
        users.addAll(jdbcTemplate.query(sqlQueryMutual, this::mapRowToUser, id, true));
        return users;

    }

    private boolean hasMutualConnection(long id, long friendId) {
        String sqlQuery = "SELECT * FROM friendship WHERE (from_user_id = ? AND to_user_id = ? AND accepted = ?) " +
                "OR (from_user_id = ? AND to_user_id = ? AND accepted = ?) ";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlQuery, id, friendId, true, friendId, id, true);
        return sqlRowSet.next();
    }

    private boolean hasConnection(long id, long friendId) {
        String sqlQuery = "SELECT * FROM friendship WHERE (from_user_id = ? AND to_user_id = ?) " +
                "OR (from_user_id = ? AND to_user_id = ?) ";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlQuery, id, friendId, friendId, id);
        return sqlRowSet.next();
    }

    private boolean addConnection(long id, long friendId) {
        String sqlQuery = "INSERT INTO friendship(to_user_id, from_user_id, accepted) " +
                "values (?, ?, ?)";
        return jdbcTemplate.update(sqlQuery, friendId, id, false) > 0;
    }

    private boolean deleteConnection(long id, long friendId) {
        String sqlQuery = "DELETE FROM friendship WHERE (from_user_id = ? AND to_user_id = ?) OR " +
                "(from_user_id = ? AND to_user_id = ?)";
        return jdbcTemplate.update(sqlQuery, id, friendId, friendId, id) > 0;
    }

    @Override
    public boolean addToFriends(long id, long friendId) {
        if (hasConnection(id, friendId)) {
            String sql = "UPDATE friendship SET accepted = ? WHERE (to_user_id = ? AND from_user_id = ?) " +
                    "OR (to_user_id = ? AND from_user_id = ?)";
            return jdbcTemplate.update(sql, true, id, friendId, friendId, id) > 0;
        } else {
            return addConnection(id, friendId);
        }
    }

    @Override
    public boolean deleteFromFriends(long id, long friendId) {
        boolean isMutual = hasMutualConnection(id, friendId);
        boolean isDeleted = deleteConnection(id, friendId);
        if (isMutual) {
            addConnection(friendId, id);
        }
        return isDeleted;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {

        return User.builder()
                .id(resultSet.getLong("user_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }
}