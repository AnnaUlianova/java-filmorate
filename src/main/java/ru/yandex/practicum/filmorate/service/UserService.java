package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage storage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage storage) { // inMemoryUserStorage
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
        return storage.getListOfFriends(id);
    }

    public List<User> getListOfCommonFriends(long id, long otherId) {
        return getListOfFriends(id).stream().filter(getListOfFriends(otherId)::contains).collect(Collectors.toList());
    }

    public boolean addToFriends(long id, long friendId) {
        Optional<User> optUser = storage.findById(id);
        Optional<User> optFriend = storage.findById(friendId);

        if (optUser.isPresent() && optFriend.isPresent()) {
            return storage.addToFriends(id, friendId);
        }
        return false;
    }

    public boolean deleteFromFriends(long id, long friendId) {
        Optional<User> optUser = storage.findById(id);
        Optional<User> optFriend = storage.findById(friendId);

        if (optUser.isPresent() && optFriend.isPresent()) {
            return storage.deleteFromFriends(id, friendId);
        }
        return false;
    }

    private User validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
