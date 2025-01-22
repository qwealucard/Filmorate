package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exceptions.GenreException;
import ru.yandex.practicum.filmorate.exceptions.MPAException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ReleaseDateException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@AllArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbc;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, RELEASE_DATE, duration, mpa) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(minReleaseDate)) {
                throw new ReleaseDateException("Ошибка при создании фильма, связанная с датой");
            }
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() == null) {
                ps.setObject(5, null);
            } else {
                validateMpaRating(film.getMpa());
                ps.setInt(5, film.getMpa().getId());
            }
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }
        if (film.getMpa() != null) {
            film.setMpa(getMpaRatingById(film.getMpa().getId()));
        }
        addGenreToFilm(film);

        if (film.getDirectors() != null) {
            addDirectorToFilm(film);
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE id = ?";
        try {
            // Проверка рейтинга MPA
            if (film.getMpa() != null) {
                validateMpaRating(film.getMpa());
            }

            // Обновление основных данных фильма
            jdbc.update(sql,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa() != null ? film.getMpa().getId() : null,
                    film.getId());

            // Обновляем объект MPA в фильме
            if (film.getMpa() != null) {
                film.setMpa(getMpaRatingById(film.getMpa().getId()));
            }

            // Удаляем существующие жанры для фильма
            clearGenresFromFilm(film.getId());

            // Сортируем жанры по ID перед добавлением
            if (film.getGenres() != null) {
                log.info("Сортировка жанров для фильма ID {}", film.getId());
                Set<Genre> sortedGenres = film.getGenres()
                        .stream()
                        .sorted(Comparator.comparingInt(Genre::getId))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                film.setGenres(sortedGenres);
            }

            // Добавляем новые жанры
            addGenreToFilm(film);
            // Обновляем режиссеров
            if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
                addDirectorToFilm(film);
            } else {
                clearDirectorsByIdFilm(film.getId());
            }

            return film;
        } catch (RuntimeException e) {
            log.error("Ошибка при обновлении фильма " + film.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    // Метод для очистки режиссеров у фильма
    private void clearDirectorsByIdFilm(Integer filmId) {
        String sql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbc.update(sql, filmId);
        log.info("Режиссеры для фильма с id {} успешно удалены", filmId);
    }

    // Метод для очистки жанров у фильма
    private void clearGenresFromFilm(Integer filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(sql, filmId);
        log.info("Жанры для фильма с id {} успешно удалены", filmId);
    }

    public Collection<Film> findAll() {
        // SQL-запрос для выборки базовых данных о фильмах
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name FROM films f LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id";

        // Загружаем все фильмы
        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Integer mpaId = rs.getObject("MPARating_id", Integer.class);
            String mpaName = rs.getString("MPA_Rating_name");

            // Создаем объект MPARating только если данные не null
            MPARating mpaRating = mpaId != null ? new MPARating(mpaId, mpaName) : null;

            // Создаем объект Film
            return new Film(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(),  // Жанры будут заполнены позже
                    mpaRating,
                    new HashSet<>()
            );
        });

        // Получаем все жанры для фильмов
        Map<Integer, HashSet<Genre>> allFilmGenres = getAllFilmGenres();
        Map<Integer, HashSet<Director>> allFilmDirectors = getAllFilmDirectors();

        // Заполняем жанры и режиссеров для каждого фильма
        films.forEach(film -> {
            film.setGenres(allFilmGenres.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(allFilmDirectors.getOrDefault(film.getId(), new HashSet<>()));
        });
        return films;
    }

    // Метод для получения всех режиссеров
    private Map<Integer, HashSet<Director>> getAllFilmDirectors() {
        String sql = " SELECT fd.film_id, d.id, d.name FROM film_directors fd JOIN directors d ON fd.directors_id = d.id ";
        Map<Integer, HashSet<Director>> allFilmDirectors = new HashMap<>();

        jdbc.query(sql, (rs, rowNum) -> {
            Integer filmId = rs.getInt("film_id");
            Director director = new Director(
                    rs.getInt("id"),
                    rs.getString("name")
            );

            allFilmDirectors.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            return null;
        });

        return allFilmDirectors;
    }

    private void validateMpaRating(MPARating mpaRating) {
        String sql = "SELECT COUNT(*) FROM MPA_Ratings WHERE MPARating_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mpaRating.getId());
        if (count == null || count == 0) {
            throw new MPAException("Ошибка с заполнением рейтинга");
        }
    }

    private void genreIsNotNull(Genre genre) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, genre.getId());
        if (count == null || count == 0) {
            throw new GenreException("Ошибка с заполнением жанра");
        }
    }

    private MPARating getMpaRatingById(Integer id) {
        String sql = "SELECT MPARating_id, MPA_Rating_name FROM MPA_Ratings WHERE MPARating_id = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> new MPARating(
                    rs.getInt("MPARating_id"),
                    rs.getString("MPA_Rating_name")
            ), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA Rating с id {} не найден", id);
            return null; // Или выбросить свое исключение, если отсутствие значения критично
        }
    }

    private MPARating getMpaRatingByIdFilm(Integer id) {
        String sql = "SELECT MPARating_id, MPA_Rating_name FROM MPA_Ratings m join films f on f.mpa = m.MPARating_id WHERE f.id = ?";
        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> new MPARating(
                    rs.getInt("MPARating_id"),
                    rs.getString("MPA_Rating_name")
            ), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA Rating  для id фильма {} не найден", id);
            return null; // Или выбросить свое исключение, если отсутствие значения критично
        }
    }

    private void addGenreToFilm(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        // Если у фильма нет жанров, выходим из метода
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            log.warn("Фильм с ID {} не содержит жанров для добавления.", film.getId());
            return;
        }

        // Уникальный набор жанров фильма, отсортированный по ID от меньшего к большему
        List<Genre> sortedGenres = film.getGenres().stream()
                .filter(genre -> {
                    genreIsNotNull(genre); // Проверяем существование жанра
                    return !isGenreAlreadyAdded(film.getId(), genre.getId()); // Фильтруем уже добавленные жанры
                }).sorted(Comparator.comparingInt(Genre::getId))
                .sorted(Comparator.comparingInt(Genre::getId)) // Сортируем жанры по ID в порядке возрастания
                .toList();

        log.info("Список жанров для добавления (отсортирован): {}", sortedGenres);
        film.setGenres(new LinkedHashSet<>(sortedGenres));
        // Выполняем пакетную вставку
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = sortedGenres.get(i);
                ps.setInt(1, film.getId());
                ps.setInt(2, genre.getId());
                log.info("Добавление жанра с ID {} к фильму с ID {}", genre.getId(), film.getId());
            }

            @Override
            public int getBatchSize() {
                return sortedGenres.size();
            }
        });

        log.info("Добавление жанров завершено для фильма с ID {}", film.getId());
    }

    private void addDirectorToFilm(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            log.warn("Список режиссеров для фильма с ID {} пустой или равен null", film.getId());
            return;
        }

        String sql = "INSERT INTO film_directors (film_id, directors_id) VALUES (?, ?)";
        List<Director> validDirectors = film.getDirectors().stream()
                .filter(director -> director.getId() != null)
                .collect(Collectors.toList());

        if (validDirectors.isEmpty()) {
            log.warn("Фильм с ID {} содержит только недействительные или пустые режиссеры", film.getId());
            return;
        }

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = validDirectors.get(i);
                ps.setInt(1, film.getId());
                ps.setInt(2, director.getId());
                log.info("Добавление режиссера с ID {} к фильму с ID {}", director.getId(), film.getId());
            }

            @Override
            public int getBatchSize() {
                return validDirectors.size();
            }
        });
    }

    private boolean filmExists(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name " +
                "FROM films AS f " +
                "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id WHERE f.id = ?";
        try {
            Film film = jdbc.queryForObject(sql, (rs, rowNum) -> {
                return new Film(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("release_date").toLocalDate(),
                        rs.getInt("duration"),
                        new HashSet<>(),
                        new MPARating(
                                rs.getInt("MPARating_id"),
                                rs.getString("MPA_Rating_name")
                        ),
                        new HashSet<>()
                );
            }, id);
            HashSet<Genre> genres = getFilmGenresById(film.getId());
            if (genres != null) {
                film.setGenres(genres);
                log.info("жанр добавлен");
            } else {
                film.setGenres(new HashSet<>());
            }
            Set<Director> directors = getFilmDirectorsById(film.getId());
            if (!directors.isEmpty()) {
                film.setDirectors(directors);
            }
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            log.error("Ошибка при поиске фильма по id " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private HashSet<Genre> getFilmGenresById(Integer filmId) {
        String sql = "SELECT g.genre_id, g.genre_name FROM film_genres AS fg LEFT JOIN genres AS g ON fg.genre_id = g.genre_id WHERE film_id = ?";
        List<Genre> genresList = jdbc.query(sql, (rs, rowNum) -> new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name")
        ), filmId);
        HashSet<Genre> uniqueGenres = new HashSet<>(genresList);
        return uniqueGenres;
    }

    private Set<Director> getFilmDirectorsById(Integer filmId) {
        String sql = """
                SELECT d.id, d.name
                FROM directors d
                INNER JOIN film_directors fd ON d.id = fd.directors_id
                WHERE fd.film_id = ?
                """;

        // Выполняем запрос и преобразуем результат в Set для устранения дубликатов
        return new HashSet<>(jdbc.query(sql, new DirectorRowMapper(), filmId));
    }

    private boolean isGenreAlreadyAdded(Integer filmId, Integer genreId) {
        String sql = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId, genreId);
        return count != null && count > 0;
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name, COUNT(l.user_id) as likesCount " +
                "FROM films f LEFT JOIN film_likes l ON f.id = l.film_id " +
                "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id " +
                "GROUP BY f.id ORDER BY likesCount DESC, f.id DESC LIMIT ?";
        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Film film = new Film(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(),
                    rs.getObject("mpa") != null ? new MPARating(
                            rs.getInt("MPARating_id"),
                            rs.getString("MPA_Rating_name")
                    ) : null,
                    new HashSet<>()
            );



           // addGenreToFilm(film);
            return film;
        }, count);
        Map<Integer, HashSet<Genre>> allFilmGenres = getAllFilmGenres();
        // Map<Integer, HashSet<Director>> allFilmDirectors = getAllFilmDirectors();

        // Заполняем жанры и директоров для каждого фильма
        films.forEach(film -> {
            film.setGenres(allFilmGenres.getOrDefault(film.getId(), new HashSet<>()));
           // film.setDirectors(allFilmDirectors.getOrDefault(film.getId(), new HashSet<>()));
        });
        return films;
    }

    private Map<Integer, HashSet<Genre>> getAllFilmGenres() {
        String sql = "SELECT fg.film_id, g.genre_id, g.genre_name FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id";
        Map<Integer, HashSet<Genre>> allFilmGenres = new HashMap<>();

        jdbc.query(sql, (rs, rowNum) -> {
            Integer filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));

            // Используем HashSet для уникальности жанров
            allFilmGenres.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            return null;
        });

        return allFilmGenres;
    }

    @Override
    public List<Film> getDirectorSort(Integer directorId, String sortBy) {
        // Определяем метод сортировки
        List<Film> films;
        if ("year".equalsIgnoreCase(sortBy)) {
            films = getFilmSortYear(directorId);
        } else {
            films = getFilmSortLike(directorId);
        }
        if (films.isEmpty()) {
            throw new NotFoundException("There are no items to sort.");
        }

        // Получаем все жанры и режиссеров для фильмов
        Map<Integer, HashSet<Genre>> allFilmGenres = getAllFilmGenres();
        Map<Integer, HashSet<Director>> allFilmDirectors = getAllFilmDirectors();

        // Заполняем жанры, режиссеров и проверяем наличие MPA для каждого фильма
        films.forEach(film -> {
            film.setGenres(allFilmGenres.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(allFilmDirectors.getOrDefault(film.getId(), new HashSet<>()));

            // Если MPA отсутствует, загружаем его вручную
            if (film.getMpa() == null) {
                film.setMpa(getMpaRatingByIdFilm(film.getId()));
            }
        });

        return films;
    }

    @Override
    public List<Film> getSearch(String query, String by) {

        Set<Film> set = new HashSet<>();
        set.addAll(getSearchBy("%" + query + "%", by));
        return new ArrayList<>(set);
    }


    private List<Film> getSearchBy(String query, String by) {
        String[] byArr = by.split(",");
        // SQL-запрос для поиска фильмов и режиссеров
        String sql = "WITH res AS (SELECT COUNT(user_id) likes, film_id FROM film_likes GROUP BY film_id)\n" +
                "SELECT f.*, m.MPARating_id, m.MPA_Rating_name, r.likes FROM films f\n" +
                "LEFT JOIN film_directors fd ON f.id = fd.film_id \n" +
                "LEFT JOIN directors d ON d.id = fd.directors_id \n" +
                "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id \n" +
                "LEFT JOIN res AS r ON r.film_id = f.id\n" +
                "WHERE (CASE WHEN ? LIKE 'director' THEN UPPER(d.name) LIKE ? ELSE UPPER(f.name) LIKE ? END)\n" +
                "or (CASE WHEN ? LIKE 'title' THEN UPPER(f.name) LIKE ? ELSE UPPER(d.name) LIKE ? END) ORDER BY r.likes desc";

        // Выполнение запроса и обработка результатов
        return jdbc.query(sql, (rs, rowNum) -> {
            // Создаем объект Film
            Film film = new Film(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(), // Жанры будут заполнены позже
                    rs.getObject("MPARating_id") != null ? new MPARating(
                            rs.getInt("MPARating_id"),
                            rs.getString("MPA_Rating_name")
                    ) : null,
                    new HashSet<>(), // Режиссеры будут заполнены позже
                    rs.getInt("likes")
            );

            // Добавляем жанры и режиссеров
            film.setGenres(getGenresByIdFilm(film.getId()));
            film.setDirectors(getDirectorByIdFilm(film.getId()));

            return film;
        }, byArr[0], query.toUpperCase(), query.toUpperCase(), byArr.length == 2 ? byArr[1] : byArr[0], query.toUpperCase(), query.toUpperCase());

    }

    private Set<Director> getDirectorByIdFilm(Integer idFilm) {
        // SQL-запрос для получения списка режиссеров, связанных с фильмом
        String sql = " SELECT d.id, d.name FROM directors d INNER JOIN film_directors fd ON fd.directors_id = d.id WHERE fd.film_id = ?";

        // Логируем информацию о начале выполнения метода
        log.debug("Получение списка режиссеров для фильма с ID {}", idFilm);

        // Выполняем запрос и возвращаем результат
        try {
            // Преобразуем результат в Set для устранения дублирования
            Set<Director> directors = new HashSet<>(jdbc.query(sql, new DirectorRowMapper(), idFilm));

            // Логируем полученный результат
            log.debug("Найдено {} уникальных режиссеров для фильма с ID {}", directors.size(), idFilm);

            return directors;
        } catch (DataAccessException e) {
            // Логируем ошибку, если запрос завершился неудачно
            log.error("Ошибка при получении режиссеров для фильма с ID {}: {}", idFilm, e.getMessage());
            return Collections.emptySet(); // Возвращаем пустой Set в случае ошибки
        }
    }

    private HashSet<Genre> getGenresByIdFilm(Integer idFilm) {
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        List<Genre> genresList = jdbc.query(sql, new GenreRowMapper(), idFilm);
        return new HashSet<>(genresList);
    }

    private List<Film> getFilmSortYear(Integer directorId) {
        String sql = "SELECT f.* FROM films f JOIN film_directors fd ON f.id = fd.film_id WHERE fd.directors_id = ? ORDER BY release_date";
        return jdbc.query(sql, (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new HashSet<>(),
                null,
                new HashSet<>()
        ), directorId);
    }

    private List<Film> getFilmSortLike(Integer directorId) {
        String sql = "WITH res AS (SELECT COUNT(user_id) likes, film_id FROM film_likes GROUP BY film_id)\n" +
                "SELECT f.* FROM films f JOIN film_directors fd ON f.id = fd.film_id LEFT JOIN res AS r ON r.FILM_ID = f.ID " +
                "WHERE fd.directors_id = ? ORDER BY likes DESC";

        List<Film> films = jdbc.query(sql, (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new HashSet<>(),
                null,
                new HashSet<>()
        ), directorId);
        return films;
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        // Базовый SQL-запрос
        StringBuilder sql = new StringBuilder(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, m.MPARating_id, m.MPA_Rating_name, " +
                        "COUNT(fl.user_id) AS likes " +
                        "FROM films f " +
                        "LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                        "LEFT JOIN film_genres fg ON f.id = fg.film_id "
        );

        // Условия фильтрации
        List<Object> params = new ArrayList<>();
        if (genreId != null || year != null) {
            sql.append("WHERE ");
            if (genreId != null) {
                sql.append("fg.genre_id = ? ");
                params.add(genreId);
            }
            if (year != null) {
                if (genreId != null) {
                    sql.append("AND ");
                }
                sql.append("YEAR(f.release_date) = ? ");
                params.add(year);
            }
        }

        // Группировка и сортировка
        sql.append("GROUP BY f.id, f.name, f.description, f.release_date, f.duration, m.MPA_Rating_name, m.MPARating_id ")
                .append("ORDER BY likes DESC ")
                .append("LIMIT ?");

        params.add(count);

        // Выполнение основного запроса
        List<Film> films = jdbc.query(sql.toString(), (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new HashSet<>(), // Жанры будут заполнены позже
                rs.getObject("MPARating_id") != null ? new MPARating(
                        rs.getInt("MPARating_id"),
                        rs.getString("MPA_Rating_name")
                ) : null,
                new HashSet<>() // Директоры будут заполнены позже
        ), params.toArray());

        // Получаем все жанры и директоров
        Map<Integer, HashSet<Genre>> allFilmGenres = getAllFilmGenres();
        Map<Integer, HashSet<Director>> allFilmDirectors = getAllFilmDirectors();

        // Заполняем жанры и директоров для каждого фильма
        films.forEach(film -> {
            film.setGenres(allFilmGenres.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(allFilmDirectors.getOrDefault(film.getId(), new HashSet<>()));
        });

        return films;
    }

    public void deleteFilmById(Integer id) {
        try {
            String deleteLikesSql = "DELETE FROM film_likes WHERE film_id = ?";
            jdbc.update(deleteLikesSql, id);

            String deleteFilmGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
            jdbc.update(deleteFilmGenresSql, id);

            String deleteFilmSql = "DELETE FROM films WHERE id = ?";
            int rowsAffected = jdbc.update(deleteFilmSql, id);

            if (rowsAffected == 0) {
                throw new NotFoundException("Фильм с id " + id + " не найден");
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Ошибка при удалении фильма с id " + id + ": " + e.getMessage(), e);
        }
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.MPARating_id, m.MPA_Rating_name
                FROM films f
                LEFT JOIN MPA_Ratings m ON f.mpa = m.MPARating_id
                JOIN film_likes fl1 ON f.id = fl1.FILM_ID AND fl1.USER_ID = ?
                JOIN film_likes fl2 ON f.id = fl2.FILM_ID AND fl2.USER_ID = ?
                ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.FILM_ID = f.id) DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> {

            Film film = new Film(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(),
                    rs.getObject("mpa") != null ? new MPARating(
                            rs.getInt("MPARating_id"),
                            rs.getString("MPA_Rating_name")
                    ) : null,
                    new HashSet<>()
            );

            HashSet<Genre> genres = getFilmGenresById(film.getId());
            if (!genres.isEmpty()) {
                film.setGenres(genres);
            }
            return film;

        }, userId, friendId);
    }
}

