package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Builder
public class Feed {
    private Long timestamp;
    @NotBlank(message = "User ID is required")
    private Long userId;
    @NotBlank(message = "EventType is required")
    private EventTypeList eventType; // одно из значениий LIKE, REVIEW или FRIEND
    @NotBlank(message = "OperationType is required")
    private OperationTypeList operation; // одно из значениий REMOVE, ADD, UPDATE
    @NotBlank(message = "EventId is required")
    private Long eventId; //primary key
    @NotBlank(message = "EntityId is required")
    private Long entityId; // идентификатор сущности, с которой произошло событие

    public enum EventTypeList {LIKE, REVIEW, FRIEND}

    public enum OperationTypeList {REMOVE, ADD, UPDATE}
}
