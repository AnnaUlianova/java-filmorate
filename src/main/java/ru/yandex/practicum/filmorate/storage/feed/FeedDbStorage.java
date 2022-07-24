package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;


@Component
public class FeedDbStorage implements FeedStorage {
    private static final String FIND_ALL_FEEDS = "SELECT * FROM feeds WHERE user_id = ?";
    private static final String ADD_FEED = "INSERT INTO feeds(timestamp, user_id, event_type," +
            "operation, entity_id) VALUES (?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFeed(Long userId, Feed.EventTypeList eventType,
                        Feed.OperationTypeList operationType, Long entityId) {

        jdbcTemplate.update(ADD_FEED,
                System.currentTimeMillis(),
                userId,
                getIntegerFromEvent(eventType),
                getIntegerFromOperation(operationType),
                entityId);
    }

    @Override
    public Collection<Feed> findAllFeeds(Long id) {
        return jdbcTemplate.query(FIND_ALL_FEEDS, this::mapRowToFeed, id);
    }

    private Feed mapRowToFeed(ResultSet resultSet, int rowNum) throws SQLException {
        return Feed.builder()
                .timestamp(resultSet.getLong("timestamp"))
                .userId(resultSet.getLong("user_id"))
                .eventType(getEventFromInteger(resultSet.getInt("event_type")))
                .operation(getOperationFromInteger(resultSet.getInt("operation")))
                .eventId(resultSet.getLong("feed_id"))
                .entityId(resultSet.getLong("entity_id"))
                .build();
    }

    private Feed.EventTypeList getEventFromInteger(int x) {
        switch (x) {
            case 1:
                return Feed.EventTypeList.LIKE;
            case 2:
                return Feed.EventTypeList.REVIEW;
            case 3:
                return Feed.EventTypeList.FRIEND;
        }
        return null;
    }

    private Integer getIntegerFromEvent(Feed.EventTypeList event) {
        switch (event) {
            case LIKE:
                return 1;
            case REVIEW:
                return 2;
            case FRIEND:
                return 3;
        }
        return null;
    }

    private Feed.OperationTypeList getOperationFromInteger(int x) {
        switch (x) {
            case 1:
                return Feed.OperationTypeList.REMOVE;
            case 2:
                return Feed.OperationTypeList.ADD;
            case 3:
                return Feed.OperationTypeList.UPDATE;
        }
        return null;
    }

    private Integer getIntegerFromOperation(Feed.OperationTypeList operation) {
        switch (operation) {
            case REMOVE:
                return 1;
            case ADD:
                return 2;
            case UPDATE:
                return 3;
        }
        return null;
    }
}
