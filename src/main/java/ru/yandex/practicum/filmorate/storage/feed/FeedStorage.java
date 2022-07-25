package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

public interface FeedStorage {
    Collection<Feed> findAllFeeds(Long id);

    void addFeed(Long userId, Feed.EventTypeList eventType, Feed.OperationTypeList operationType, Long entityId);
}
