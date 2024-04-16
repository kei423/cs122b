-- DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE if not exists moviedb;
USE moviedb;

CREATE TABLE if not exists movies (
	id VARCHAR(10) NOT NULL,
	title VARCHAR(100) NOT NULL,
	year INTEGER NOT NULL,
	director VARCHAR(100) NOT NULL,
        FULLTEXT (title),
	PRIMARY KEY (id)
);

CREATE TABLE if not exists stars (
	id VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthYear INTEGER,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists stars_in_movies (
	starId VARCHAR(10) NOT NULL,
	movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY (starId, movieId),
	FOREIGN KEY (starId) REFERENCES stars(id) ON DELETE CASCADE,
	FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE if not exists genres (
	id INTEGER NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists genres_in_movies (
	genreId INTEGER NOT NULL,
	movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY (genreId, movieId),
	FOREIGN KEY (genreId) REFERENCES genres(id) ON DELETE CASCADE,
	FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE if not exists creditcards (
	id VARCHAR(20) NOT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration date NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists customers (
	id INTEGER NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccId VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

CREATE TABLE if not exists sales (
	id INTEGER NOT NULL AUTO_INCREMENT,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate date NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE if not exists ratings (
	movieId VARCHAR(10) NOT NULL,
    rating float NOT NULL,
    numVotes INTEGER NOT NULL,
    PRIMARY KEY (movieId),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE if not exists employees (
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);