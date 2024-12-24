DROP TABLE if EXISTS users CASCADE;
DROP TABLE if EXISTS films CASCADE;
DROP TABLE if EXISTS MPA_Ratings CASCADE;
DROP TABLE if EXISTS genres CASCADE;
DROP TABLE if EXISTS user_likes CASCADE;
DROP TABLE if EXISTS friendship CASCADE;
DROP TABLE if EXISTS film_genres CASCADE;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  name varchar(255) NOT NULL UNIQUE,
  email varchar(255) NOT NULL,
  login varchar(255) NOT NULL,
  birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
  id SERIAL PRIMARY KEY,
  name varchar(255) NOT NULL UNIQUE,
  description varchar(255) NOT NULL,
  RELEASE_DATE DATE NOT NULL,
  duration INTEGER NOT NULL,
  mpa_id INTEGER REFERENCES MPA_Rating(MPARating_id)
);

CREATE TABLE IF NOT EXISTS genres (
  genre_id SERIAL PRIMARY KEY,
  genre_name varchar(255)
);

CREATE TABLE IF NOT EXISTS MPA_Rating (
   MPARating_id SERIAL PRIMARY KEY,
   MPA_Rating_name varchar(255)
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

