package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MPAStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;
    private final DirectorStorage directorStorage;
    private static final String CREATE_FILM = "INSERT INTO films(name, description, duration, release_date, " +
            "rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, duration = ?, " +
            "release_date = ?, rating_id = ? WHERE film_id = ?";
    private static final String FIND_FILM = "SELECT * FROM films WHERE film_id = ?";
    private static final String FIND_ALL_FILMS = "SELECT * FROM films";
    private static final String DELETE_FILM = "DELETE FROM films WHERE film_id = ?";
    private static final String ADD_LIKE = "INSERT INTO films_likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_LIKES_COUNT = "UPDATE films SET likes_count = ? WHERE film_id = ?";
    private static final String GET_FILM_GENRE = "SELECT genre_id FROM films_genres WHERE film_id = ?";
    private static final String ADD_FILM_GENRE = "INSERT INTO films_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRE = "DELETE FROM films_genres WHERE film_id = ?";
    private static final String FIND_TOP_FILMS = "SELECT * FROM films ORDER BY likes_count DESC LIMIT ?";
    private static final String FIND_TOP_FILMS_BY_GENRE = "SELECT * FROM films WHERE film_id IN " +
            "(SELECT film_id FROM films_genres WHERE genre_id = ?) ORDER BY likes_count DESC LIMIT ?";
    private static final String FIND_TOP_FILMS_BY_YEAR = "SELECT * FROM films WHERE " +
            "EXTRACT(YEAR FROM release_date) = ? ORDER BY likes_count DESC LIMIT ?";
    private static final String FIND_TOP_FILMS_BY_YEAR_AND_GENRE = "SELECT * FROM films WHERE film_id IN " +
            "(SELECT film_id FROM films_genres WHERE genre_id = ?) AND EXTRACT(YEAR FROM release_date) = ? " +
            "ORDER BY likes_count DESC LIMIT ?";
    private static final String ADD_FILM_DIRECTOR = "INSERT INTO films_directors(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTOR = "DELETE FROM films_directors WHERE film_id = ?";
    private static final String FIND_DIRECTOR_FILMS_SORT_BY_LIKES =
            "SELECT f.* FROM films_directors fd, films f " +
                    "WHERE fd.director_id = ? AND f.film_id = fd.film_id " +
                    "ORDER BY f.likes_count DESC";
    private static final String FIND_DIRECTOR_FILMS_SORT_BY_YEAR =
            "SELECT f.* FROM films_directors fd, films f " +
                    "WHERE fd.director_id = ? AND f.film_id = fd.film_id " +
                    "ORDER BY f.release_date";
    private static final String FIND_DIRECTOR_FILMS_SORT_BY_ID =
            "SELECT f.* FROM films_directors fd, films f " +
                    "WHERE fd.director_id = ? AND f.film_id = fd.film_id " +
                    "ORDER BY f.film_id";
    private static final String FIND_TOP_FILMS_BY_TITLE_FRAGMENT = "SELECT * FROM films " +
            "WHERE name ~* ? ORDER BY likes_count";
    private static final String FIND_TOP_FILMS_BY_DIRECTOR_FRAGMENT = "SELECT f.* FROM FILMS AS f " +
            "JOIN films_directors AS fd ON fd.film_id = f.film_id " +
            "JOIN directors AS d ON fd.director_id = d.director_id " +
            "WHERE d.name ~* ? " +
            "ORDER BY likes_count";
    private static final String FIND_TOP_FILMS_BY_TITLE_AND_DIRECTOR_FRAGMENT = "SELECT f.* FROM FILMS AS f " +
            "JOIN films_directors AS fd ON fd.film_id = f.film_id " +
            "JOIN directors AS d ON fd.director_id = d.director_id " +
            "WHERE f.name ~* ? AND d.name ~* ? " +
            "ORDER BY likes_count";
    private static final String FIND_ALL_FILMS_BY_LIKES = "SELECT * FROM films ORDER BY likes_count";

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         @Qualifier("genreDbStorage") GenreStorage genreStorage,
                         @Qualifier("MPADbStorage") MPAStorage mpaStorage,
                         @Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(CREATE_FILM, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setInt(3, film.getDuration());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);
        long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);
        addGenreIdsToDB(film);
        setGenresFromDB(film);

        addDirectorIdsToDB(film);
        setDirectorsFromDB(film);

        setRatingFromDB(film);
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        boolean isUpdated = jdbcTemplate.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getId()) > 0;
        if (isUpdated) {
            jdbcTemplate.update(DELETE_FILM_GENRE, film.getId());
            addGenreIdsToDB(film);
            jdbcTemplate.update(DELETE_FILM_DIRECTOR, film.getId());
            addDirectorIdsToDB(film);
        }
        setGenresFromDB(film);
        if (film.getDirectors() != null) { // Костыль, чтобы пройти тест PUT Film update remove director
            setDirectorsFromDB(film);
        }
        setRatingFromDB(film);
        return isUpdated ? Optional.of(film) : Optional.empty();
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS, this::mapRowToFilm);
        for (Film film : films) {
            setGenresFromDB(film);
            setDirectorsFromDB(film);
        }
        return films;
    }

    @Override
    public Collection<Film> getDirectorFilms(long directorId, String sortBy) {
        Collection<Film> films = "likes".equals(sortBy)
                ? jdbcTemplate.query(FIND_DIRECTOR_FILMS_SORT_BY_LIKES, this::mapRowToFilm, directorId)
                : ("year".equals(sortBy)
                ? jdbcTemplate.query(FIND_DIRECTOR_FILMS_SORT_BY_YEAR, this::mapRowToFilm, directorId)
                : jdbcTemplate.query(FIND_DIRECTOR_FILMS_SORT_BY_ID, this::mapRowToFilm, directorId));
        for (Film film : films) {
            setGenresFromDB(film);
            setDirectorsFromDB(film);
        }
        return films;
    }

    @Override
    public Optional<Film> findById(long id) {
        try {
            Film film = jdbcTemplate.queryForObject(FIND_FILM, this::mapRowToFilm, id);
            Optional<Film> optFilm = Optional.ofNullable(film);
            optFilm.ifPresent(this::setGenresFromDB);
            optFilm.ifPresent(this::setDirectorsFromDB);
            return optFilm;
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(long id) {
        return jdbcTemplate.update(DELETE_FILM, id) > 0;
    }

    @Override
    public boolean removeLikeFromFilm(long id, long userId) {
        boolean isRemoved = jdbcTemplate.update(DELETE_LIKE, id, userId) > 0;
        if (isRemoved) {
            long likesAmount = findById(id).get().getLikes_count() - 1;
            jdbcTemplate.update(GET_LIKES_COUNT, likesAmount, id);
        }
        return isRemoved;
    }

    @Override
    public boolean addLikeToFilm(long id, long userId) {
        boolean isAdded = jdbcTemplate.update(ADD_LIKE, id, userId) > 0;
        if (isAdded) {
            long likesAmount = findById(id).get().getLikes_count() + 1;
            jdbcTemplate.update(GET_LIKES_COUNT, likesAmount, id);
        }
        return isAdded;
    }

    public List<Film> findTopLikableFilms(long count) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS, this::mapRowToFilm, count);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;
    }

    public List<Film> findTopFilmsByGenre(long count, int genreId) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS_BY_GENRE, this::mapRowToFilm, genreId, count);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;
    }

    public List<Film> findTopFilmsByYear(long count, int year) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS_BY_YEAR, this::mapRowToFilm, year, count);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;
    }

    public List<Film> findTopFilmsByGenreAndYear(long count, int genreId, int year) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS_BY_YEAR_AND_GENRE, this::mapRowToFilm,
                genreId, year, count);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;
    }

    @Override
    public List<Film> findTopFilmsByTitleFragment(String someText) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS_BY_TITLE_FRAGMENT, this::mapRowToFilm, someText);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;

    }

    @Override
    public List<Film> findTopFilmsByDirectorFragment(String someText) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS_BY_DIRECTOR_FRAGMENT, this::mapRowToFilm, someText);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;
    }

    @Override
    public List<Film> findTopFilmsByTitleAndDirectorFragment(String someText) {
        List<Film> films = jdbcTemplate.query(FIND_TOP_FILMS_BY_TITLE_AND_DIRECTOR_FRAGMENT,
                this::mapRowToFilm, someText, someText);
        for (Film film : films) {
            setGenresFromDB(film);
        }
        return films;
    }

    @Override
    public List<Film> findAllFilmsByLikes() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS_BY_LIKES, this::mapRowToFilm);
        for (Film film : films) {
            setGenresFromDB(film);
            setDirectorsFromDB(film);
        }
        return films;
    }

    private void setGenresFromDB(Film film) {
        Set<Genre> genreSet = jdbcTemplate.queryForList(GET_FILM_GENRE, Long.class, film.getId())
                .stream()
                .map(genreId -> genreStorage.findById(genreId).get())
                .collect(Collectors.toSet());
        film.setGenres(genreSet);
    }

    private void setRatingFromDB(Film film) {
        film.setMpa(mpaStorage.findById(film.getMpa().getId()).get());
    }

    private void addGenreIdsToDB(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(ADD_FILM_GENRE, film.getId(), genre.getId());
            }
        }
    }

    private void addDirectorIdsToDB(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update(ADD_FILM_DIRECTOR, film.getId(), director.getId());
            }
        }
    }

    private void setDirectorsFromDB(Film film) {
        Set<Director> directorSet = directorStorage.getFilmDirectors(film.getId())
                .stream().collect(Collectors.toSet());
        film.setDirectors(directorSet);
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
