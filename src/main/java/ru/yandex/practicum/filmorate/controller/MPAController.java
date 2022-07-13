package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MPAService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(
        value = "/mpa",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class MPAController {

    private final MPAService service;

    @GetMapping()
    public ResponseEntity<List<MPA>> findAllRatings() {
        return new ResponseEntity<>(service.findAllRatings(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MPA> findRatingById(@PathVariable int id) {
        return service.findRatingById(id).map(mpa -> new ResponseEntity<>(mpa, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

}
