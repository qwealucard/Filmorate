-- Вставка начальных данных для жанров, если они не существуют
INSERT INTO genres (genre_name)
SELECT 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_name = 'Комедия');
INSERT INTO genres (genre_name)
SELECT 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_name = 'Драма');
INSERT INTO genres (genre_name)
SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_name = 'Мультфильм');
INSERT INTO genres (genre_name)
SELECT 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_name = 'Триллер');
INSERT INTO genres (genre_name)
SELECT 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_name = 'Документальный');
INSERT INTO genres (genre_name)
SELECT 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_name = 'Боевик');

-- Вставка начальных данных для возрастных рейтингов, если они не существуют
INSERT INTO MPA_Ratings (MPA_Rating_name)
SELECT 'G' WHERE NOT EXISTS (SELECT 1 FROM MPA_Ratings WHERE MPA_Rating_name = 'G');
INSERT INTO MPA_Ratings (MPA_Rating_name)
SELECT 'PG' WHERE NOT EXISTS (SELECT 1 FROM MPA_Ratings WHERE MPA_Rating_name = 'PG');
INSERT INTO MPA_Ratings (MPA_Rating_name)
SELECT 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM MPA_Ratings WHERE MPA_Rating_name = 'PG-13');
INSERT INTO MPA_Ratings (MPA_Rating_name)
SELECT 'R' WHERE NOT EXISTS (SELECT 1 FROM MPA_Ratings WHERE MPA_Rating_name = 'R');
INSERT INTO MPA_Ratings  (MPA_Rating_name)
SELECT 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM MPA_Ratings WHERE MPA_Rating_name = 'NC-17');