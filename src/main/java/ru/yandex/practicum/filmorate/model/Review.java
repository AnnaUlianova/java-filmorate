package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Review {

    private Long reviewId;
    @NonNull
    private String content;
    @JsonProperty(value = "isPositive")
    @NonNull
    private Boolean isPositive;
    @NonNull
    private Long userId;
    @NonNull
    private Long filmId;
    private Integer useful;
}
