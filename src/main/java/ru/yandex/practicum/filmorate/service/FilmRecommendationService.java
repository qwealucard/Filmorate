package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j
public class FilmRecommendationService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;

    private Map<User, Set<Film>> loadData() {
        Map<User, Set<Film>> data = new HashMap<>();
        Collection<User> users = userStorage.findAll();
        for (User user : users) {
            List<Integer> userLikes = getLikedFilmIdsByUser(user.getId());
            Set<Film> likedFilms = userLikes.stream()
                                            .map(filmId -> filmStorage.getFilmById(filmId).orElse(null))
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toSet());
            data.put(user, likedFilms);
            log.info("Пользователь {} лайкнул {} фильмов", user.getId(), likedFilms.size());
        }
        log.info("Данные загружены: {}", data);
        return data;
    }

    private List<Integer> getLikedFilmIdsByUser(Integer userId) {
        List<Integer> likedFilmIds = likeStorage.getLikedFilmIds(userId);
        return likedFilmIds;
    }

    public List<Film> getRecommendations(Integer userId) {
        Optional<User> userOptional = userStorage.getUserById(userId);
        if (!userOptional.isPresent()) {
            return Collections.emptyList();
        }
        User user = userOptional.get();
        Map<User, Set<Film>> data = loadData();
        return getRecommendationsForUser(user, data);
    }

    private List<Film> getRecommendationsForUser(User user, Map<User, Set<Film>> data) {
        log.info("Получение рекомендаций для пользователя {}", user.getId());
        Set<Film> recommendedFilms = new HashSet<>();
        Set<Film> userLikedFilms = data.getOrDefault(user, Collections.emptySet());

        if (userLikedFilms.isEmpty()) {
            log.info("Пользователь {} не лайкал фильмы", user.getId());
            return new ArrayList<>(recommendedFilms);
        }

        for (Map.Entry<User, Set<Film>> entry : data.entrySet()) {
            User otherUser = entry.getKey();
            if (otherUser.equals(user)) {
                continue;
            }

            Set<Film> otherUserLikedFilms = entry.getValue();
            Set<Film> commonLikedFilms = new HashSet<>(userLikedFilms);
            commonLikedFilms.retainAll(otherUserLikedFilms);

            if (!commonLikedFilms.isEmpty()) {
                for (Film film : otherUserLikedFilms) {
                    if (!userLikedFilms.contains(film)) {
                        recommendedFilms.add(film);
                    }
                }
                log.info("  Пользователь {} лайкнул фильмы: {}. Общие с пользователем {} {}", otherUser.getId(), otherUserLikedFilms.size(), user.getId(), commonLikedFilms);
            }
        }
        log.info("Рекомендованные фильмы для пользователя {}: {}", user.getId(), recommendedFilms);
        return new ArrayList<>(recommendedFilms);
    }
}





