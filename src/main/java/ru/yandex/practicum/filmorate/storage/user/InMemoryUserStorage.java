package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

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
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return Optional.of(user);
        }
        return Optional.empty();
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
    public boolean deleteById(long id) {
        return users.remove(id) != null;
    }
}
