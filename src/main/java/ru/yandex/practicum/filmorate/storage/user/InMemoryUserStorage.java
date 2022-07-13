package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users;
    private final Map<Long, Set<Long>> friends;
    private static long userId;

    public InMemoryUserStorage() {
        users = new HashMap<>();
        friends = new HashMap<>();
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


    @Override
    public boolean addToFriends(long id, long friendId) {
        if (friends.containsKey(id)) {
            return friends.get(id).add(friendId);
        }
        Set<Long> userFriends = new HashSet<>();
        userFriends.add(friendId);
        friends.put(id, userFriends);
        return true;
    }

    @Override
    public boolean deleteFromFriends(long id, long friendId) {
        if (friends.containsKey(id)) {
            return friends.get(id).remove(friendId);
        }
        return false;
    }

    @Override
    public List<User> getListOfFriends(long id) {
        if (friends.containsKey(id)) {
            return friends.get(id).stream().map(users::get).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
