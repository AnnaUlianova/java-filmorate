package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users;
    private static long userId;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    private long generateId() {
        return ++userId;
    }

    @Override
    public User create(User user) {
        user.setId(generateId());
        users.put(userId, user);
        return users.get(user.getId());
    }

    @Override
    public Optional<User> update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        }
        return Optional.ofNullable(users.get(user.getId()));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> deleteById(long id) {
        return Optional.ofNullable(users.remove(id));
    }
}
