package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class Film {

    private long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max=200, message="Description must be no longer than 200 characters")
    private String description;

    @Positive(message="Duration must be positive")
    private int duration;

    @NotNull
    private LocalDate releaseDate;
}
