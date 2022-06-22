package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public User createUser(User user) {
        User newUser = validateName(user);
        return storage.create(newUser);
    }

    public Optional<User> updateUser(User user) {
        return storage.update(user);
    }

    public List<User> findAllUsers() {
        return storage.findAll();
    }

    public Optional<User> findUserById(long id) {
        return storage.findById(id);
    }

    public boolean deleteUserById(long id) {
        return storage.deleteById(id);
    }

    public List<User> getListOfFriends(long id) {
        List<User> users = new ArrayList<>();
        storage.findById(id).map(user -> user.getFriends().stream()
                .peek(friendId -> storage.findById(friendId).ifPresent(users::add))
                .collect(Collectors.toList()));
        return users;
    }

    public List<User> getListOfCommonFriends(long id, long otherId) {
        List<User> users = new ArrayList<>();
        storage.findById(id).map(user -> user.getFriends()
                .stream()
                .filter(userId -> storage.findById(otherId).get().getFriends().contains(userId))
                .peek(otherUserId -> storage.findById(otherUserId).ifPresent(users::add))
                .collect(Collectors.toList()));
       return users;
    }

    public Optional<User> addToFriends(long id, long friendId) {
        Optional<User> optUser = storage.findById(id);
        Optional<User> optFriend = storage.findById(friendId);

        if (optUser.isPresent() && optFriend.isPresent()) {
            optUser.get().getFriends().add(friendId);
            optFriend.get().getFriends().add(id);
            return optFriend;
        }
        return Optional.empty();
    }

    public Optional<User> deleteFromFriends(long id, long friendId) {
        Optional<User> optUser = storage.findById(id);
        Optional<User> optFriend = storage.findById(friendId);

        if (optUser.isPresent() && optFriend.isPresent()) {
            optUser.get().getFriends().remove(friendId);
            optFriend.get().getFriends().remove(id);
            return optFriend;
        }
        return Optional.empty();
    }

    private User validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
