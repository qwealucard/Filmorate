package ru.yandex.practicum.filmorate.direction;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorDbStorage.class})
public class DirectorTest {

    private final DirectorDbStorage directorDbStorage;

    @Test
    public void testCreate() {

        Director director1 = new Director();
        director1.setName("direct1");
        Director director2 = new Director();
        director2.setName("direct2");

        directorDbStorage.create(director1);
        directorDbStorage.create(director2);

        List<Director> directorList = directorDbStorage.findAll();

        directorList.forEach(d -> {
            assertTrue(d.getName().equals(director1.getName()) || d.getName().equals(director2.getName()));
        });
    }

    @Test
    public void testUpdate() {

        Director director = new Director();
        director.setName("direct1");

        Director directorNew = directorDbStorage.create(director);
        directorNew.setName("direct2");
        directorDbStorage.update(directorNew);

        Director directorResult = directorDbStorage.findById(directorNew.getId());

        assertEquals(directorResult.getName(), directorNew.getName());
    }

    @Test
    public void testDelete() {

        Director director = new Director();
        director.setName("direct1");

        Director directorNew = directorDbStorage.create(director);
        directorDbStorage.delete(directorNew.getId());

        assertThrows(NotFoundException.class, () -> directorDbStorage.findById(directorNew.getId()));
    }
}
