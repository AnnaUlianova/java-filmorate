package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.List;

public interface UserStorage extends Storage<User> {

    boolean addToFriends(long id, long friendId);

    boolean deleteFromFriends(long id, long friendId);

    List<User> getListOfFriends(long id);

}
