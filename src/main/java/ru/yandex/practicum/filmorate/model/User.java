package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private long id;

    @NotBlank(message = "Email is required")
    @Email(regexp = "\\w+@\\w+\\.(ru|com)",
            message = "Email should be valid")
    private String email;


    @NotBlank(message = "Login is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Login without white spaces is required")
    private String login;

    private String name;

    @Past(message = "Date must be in the past")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();
}
