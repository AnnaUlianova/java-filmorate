package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MPAStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         @Qualifier("genreDbStorage") GenreStorage genreStorage,
                         @Qualifier("MPADbStorage") MPAStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films(name, description, duration, release_date, rating_id) " +
                "values (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setInt(3, film.getDuration());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        long filmId = keyHolder.getKey().longValue();

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO films_genres(film_id, genre_id) " +
                        "values (?, ?)", filmId, genre.getId());
            }
        }
        return findById(filmId).get();
    }

    @Override
    public Optional<Film> update(Film film) {
        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, duration = ?, release_date = ?, rating_id = ? " +
                "WHERE film_id = ?";
        boolean isUpdated = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getId()) > 0;

        if (isUpdated) {
            String sqlGenresQuery = "DELETE FROM films_genres WHERE film_id = ?";
            jdbcTemplate.update(sqlGenresQuery, film.getId());
        }

        if (isUpdated && film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO films_genres(film_id, genre_id) " +
                        "values (?, ?)", film.getId(), genre.getId());
            }
        }
        return isUpdated ? Optional.of(findById(film.getId()).get()) : Optional.empty();
    }

    @Override
    public List<Film> findAll() {
        String sqlQuery = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        for (Film film : films) {
            long id = film.getId();
            String sqlGenreQuery = "SELECT genre_id FROM films_genres WHERE film_id = ?";
            Set<Genre> genreSet = jdbcTemplate.queryForList(sqlGenreQuery, Long.class, id)
                    .stream()
                    .map(genreId -> genreStorage.findById(genreId).get())
                    .collect(Collectors.toSet());
            film.setGenres(genreSet);
        }
        return films;
    }

    @Override
    public Optional<Film> findById(long id) {
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";
        List<Film> result = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, id);
        Optional<Film> optFilm = result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        if (optFilm.isPresent()) {
            String sqlGenreQuery = "SELECT genre_id FROM films_genres WHERE film_id = ?";
            Set<Genre> genreSet = jdbcTemplate.queryForList(sqlGenreQuery, Long.class, id)
                    .stream()
                    .map(genreId -> genreStorage.findById(genreId).get())
                    .collect(Collectors.toSet());
            optFilm.get().setGenres(genreSet);
        }
        return optFilm;
    }

    @Override
    public boolean deleteById(long id) {
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public boolean removeLikeFromFilm(long id, long userId) {
        String sqlQuery = "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?";
        boolean isRemoved = jdbcTemplate.update(sqlQuery, id, userId) > 0;
        if (isRemoved) {
            String sqlLikesQuery = "UPDATE films SET " +
                    "likes_count = likes_count - 1 " +
                    "WHERE film_id = ?";
            jdbcTemplate.update(sqlLikesQuery, id);
        }
        return isRemoved;
    }

    @Override
    public boolean addLikeToFilm(long id, long userId) {
        String sqlQuery = "INSERT INTO films_likes(film_id, user_id) " +
                "values (?, ?)";
        boolean isAdded = jdbcTemplate.update(sqlQuery, id, userId) > 0;
        if (isAdded) {
            long likesAmount = findById(id).get().getLikes_count() + 1;
            String sqlLikesQuery = "UPDATE films SET likes_count = ? WHERE film_id = ?";
            jdbcTemplate.update(sqlLikesQuery, likesAmount, id);
        }
        return isAdded;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {

        return Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .duration(resultSet.getInt("duration"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .mpa(mpaStorage.findById(resultSet.getInt("rating_id")).get())
                .likes_count(resultSet.getLong("likes_count"))
                .build();
    }
}
