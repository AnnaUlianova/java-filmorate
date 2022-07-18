package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
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
    private static final String CREATE_USER = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String FIND_USER = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_ALL_USERS = "SELECT * FROM users";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";
    private static final String GET_FRIENDS = "SELECT * FROM friendship f LEFT JOIN users u " +
            "ON f.to_user_id = u.user_id WHERE from_user_id = ? UNION SELECT * FROM friendship f " +
            "LEFT JOIN users u ON f.from_user_id = u.user_id WHERE to_user_id = ? AND accepted = ?";
    private static final String UPDATE_FRIENDS = "UPDATE friendship SET accepted = ? WHERE (to_user_id = ? " +
            "AND from_user_id = ?) OR (to_user_id = ? AND from_user_id = ?)";
    private static final String DELETE_FRIEND = "DELETE FROM friendship WHERE (from_user_id = ? AND to_user_id = ?) " +
            "OR (from_user_id = ? AND to_user_id = ?)";
    private static final String DELETE_ALL_FRIENDS = "DELETE FROM friendship WHERE to_user_id = ? OR from_user_id = ?";
    private static final String ADD_FRIEND = "INSERT INTO friendship(to_user_id, from_user_id, accepted) " +
            "VALUES (?, ?, ?)";
    private static final String HAS_CONNECTION = "SELECT * FROM friendship WHERE (from_user_id = ? " +
            "AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?)";
    private static final String HAS_MUTUAL_CONNECTION = "SELECT * FROM friendship WHERE (from_user_id = ? " +
            "AND to_user_id = ? AND accepted = ?) OR (from_user_id = ? AND to_user_id = ? AND accepted = ?)";


    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_USER, new String[]{"user_id"});
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
        boolean isUpdated = jdbcTemplate.update(UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()) > 0;

        // Delete all previous user's connections
//        if (isUpdated) {
//            jdbcTemplate.update("DELETE FROM friendship WHERE to_user_id = ? OR from_user_id = ?",
//                    user.getId(), user.getId());
//        }
        return isUpdated ? Optional.of(user) : Optional.empty();
    }

    @Override
    public boolean deleteById(long id) {
        boolean isDeleted = jdbcTemplate.update(DELETE_USER, id) > 0;
        jdbcTemplate.update(DELETE_ALL_FRIENDS, id, id);
        return isDeleted;
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_USERS, this::mapRowToUser);
    }

    @Override
    public Optional<User> findById(long id) {
        try {
            User user = jdbcTemplate.queryForObject(FIND_USER, this::mapRowToUser, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getListOfFriends(long id) {
        return jdbcTemplate.query(GET_FRIENDS, this::mapRowToUser, id, id, true);
    }

    private boolean hasMutualConnection(long id, long friendId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(HAS_MUTUAL_CONNECTION, id, friendId, true,
                friendId, id, true);
        return sqlRowSet.next();
    }

    private boolean hasConnection(long id, long friendId) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(HAS_CONNECTION, id, friendId, friendId, id);
        return sqlRowSet.next();
    }

    private boolean addConnection(long id, long friendId) {
        return jdbcTemplate.update(ADD_FRIEND, friendId, id, false) > 0;
    }

    private boolean deleteConnection(long id, long friendId) {
        return jdbcTemplate.update(DELETE_FRIEND, id, friendId, friendId, id) > 0;
    }

    @Override
    public boolean addToFriends(long id, long friendId) {
        if (hasConnection(id, friendId)) {
            return jdbcTemplate.update(UPDATE_FRIENDS, true, id, friendId, friendId, id) > 0;
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