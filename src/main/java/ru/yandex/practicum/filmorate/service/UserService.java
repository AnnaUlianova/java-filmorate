package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService {

    private final Map<Long, User> users;

    private static long userId;

    public UserService() {
        users = new HashMap<>();
    }

    public User createUser(User user) {
        User newUser = validateName(user);
        newUser.setId(++userId);
        users.put(userId, newUser);
        log.info("Created user: {}", users.get(user.getId()));
        return users.get(user.getId());
    }

    public ResponseEntity<User> updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Updated user: {}", users.get(user.getId()));
            return new ResponseEntity<>(users.get(user.getId()), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public List<User> findAllUsers() {
        log.debug("Current amount of users: {}", users.size());
        return new ArrayList<>(users.values());
    }

    private User validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
