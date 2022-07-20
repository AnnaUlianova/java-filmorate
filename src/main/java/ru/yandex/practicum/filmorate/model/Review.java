package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class Review {

    private Long id;

    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Content length should be less than 1000 chars")
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
