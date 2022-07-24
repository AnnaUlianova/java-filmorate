package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage storage;
    private final FeedStorage feedStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage storage,
                       @Qualifier("feedDbStorage") FeedStorage feedStorage) {
        this.storage = storage;
        this.feedStorage = feedStorage;
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

    public Optional<List<User>> getListOfFriends(long id) {
        if (storage.findById(id).isPresent()) {
            return Optional.of(storage.getListOfFriends(id));
        }
        return Optional.empty();
    }

    public Optional<List<User>> getListOfCommonFriends(long id, long otherId) {
        Optional<List<User>> userFriends = getListOfFriends(id);
        Optional<List<User>> otherUserFriends = getListOfFriends(otherId);

        if (userFriends.isPresent() && otherUserFriends.isPresent())
            return Optional.of(userFriends.get().stream()
                    .filter(user -> otherUserFriends.get().contains(user))
                    .collect(Collectors.toList()));
        return Optional.empty();
    }

    public boolean addToFriends(long id, long friendId) {
        Optional<User> optUser = storage.findById(id);
        Optional<User> optFriend = storage.findById(friendId);

        if (optUser.isPresent() && optFriend.isPresent()) {
            feedStorage.addFeed(id, Feed.EventTypeList.FRIEND, Feed.OperationTypeList.ADD,friendId);
            return storage.addToFriends(id, friendId);

        }
        return false;
    }

    public boolean deleteFromFriends(long id, long friendId) {
        Optional<User> optUser = storage.findById(id);
        Optional<User> optFriend = storage.findById(friendId);

        if (optUser.isPresent() && optFriend.isPresent()) {
            feedStorage.addFeed(id, Feed.EventTypeList.FRIEND, Feed.OperationTypeList.REMOVE,friendId);
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

    public Collection<Feed> findAllFeeds(Long id) {
        return feedStorage.findAllFeeds(id);
    }
}
