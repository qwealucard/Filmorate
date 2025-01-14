DROP TABLE if EXISTS users CASCADE;
DROP TABLE if EXISTS films CASCADE;
DROP TABLE if EXISTS MPA_Ratings CASCADE;
DROP TABLE if EXISTS genres CASCADE;
DROP TABLE if EXISTS film_likes CASCADE;
DROP TABLE if EXISTS friendship CASCADE;
DROP TABLE if EXISTS film_genres CASCADE;
DROP TABLE if EXISTS film_directors CASCADE;
DROP TABLE if EXISTS directors CASCADE;
DROP TABLE if EXISTS recommendation CASCADE;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  name varchar(255) NOT NULL UNIQUE,
  email varchar(255) NOT NULL,
  login varchar(255) NOT NULL,
  birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS MPA_Ratings (
   MPARating_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   MPA_Rating_name varchar(255)
);

CREATE TABLE IF NOT EXISTS films (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(255) NOT NULL,
  description varchar(255) NOT NULL,
  RELEASE_DATE DATE NOT NULL,
  duration INTEGER NOT NULL,
  mpa INTEGER REFERENCES MPA_Ratings(MPARating_id)
);

CREATE TABLE IF NOT EXISTS genres (
  genre_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  genre_name varchar(255)
);

CREATE TABLE IF NOT EXISTS film_genres (
  film_id INTEGER REFERENCES films(id),
  genre_id INTEGER REFERENCES genres(genre_id)
);

CREATE TABLE IF NOT EXISTS friendship (
  user_id INTEGER REFERENCES users(id),
  friend_id INTEGER REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS film_likes (
  user_id INTEGER REFERENCES users(id),
  film_id INTEGER REFERENCES films(id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id INT NOT NULL,
    film_id INT NOT NULL,
    useful INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_likes (
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    is_like BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES reviews(review_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS directors (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(255)
);

CREATE TABLE IF NOT EXISTS film_directors (
  film_id INTEGER REFERENCES films(id),
  directors_id INTEGER REFERENCES directors(id),
    PRIMARY KEY (film_id, directors_id)
);

