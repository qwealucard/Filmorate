package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Integer id) {
        if (id == null) {
            throw new ValidationException("Идентификатор режиссера должен быть указан");
        }
        return directorStorage.findById(id);
    }

    public Director create(Director director) {
        if (director == null || director.getName().isEmpty()) {
            throw new ValidationException("При создании режиссера его ид не может отсутствовать");
        }
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director == null || director.getId() == null || director.getName().trim().isEmpty()) {
            throw new ValidationException("При изменении режиссера его ид не может отсутствовать, имя не может быть пробелом");
        }
        directorStorage.findById(director.getId());
        return directorStorage.update(director);
    }

    public void delete(Integer id) {
        directorStorage.findById(id);
        directorStorage.delete(id);
    }
}
