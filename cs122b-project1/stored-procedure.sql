drop procedure if exists add_movie;
use moviedb;
delimiter //
create procedure add_movie (
	in m_title VARCHAR(100),
	in m_year int,
	in m_director VARCHAR(100),
	in m_star VARCHAR(100),
	in m_genre VARCHAR(32)
)
BEGIN
	declare movie_id VARCHAR(10);
	declare star_id VARCHAR(10);
	declare genre_id int;
    declare status_message VARCHAR(40);
    
    start transaction;
    
	select id into movie_id from movies 
    where title = m_title and year = m_year and director = m_director;
    
    if movie_id is null then
        select id into star_id from stars where name = m_star limit 1;
        if star_id is null then
			set star_id = CONCAT('nm', LPAD((SELECT MAX(SUBSTRING(id, 3)) + 1 FROM stars), 7, '0'));
			insert into stars (id, name) values (star_id, m_star);
        end if;
		
        select id into genre_id from genres where name = m_genre limit 1;
        if genre_id is null then
			insert into genres (name) values (m_genre);
            SET genre_id = LAST_INSERT_ID();
        end if;
		
        set movie_id = CONCAT('tt', LPAD((SELECT MAX(SUBSTRING(id, 3)) + 1 FROM movies), 7, '0'));
		insert into movies (id, title, year, director) values (movie_id, m_title, m_year, m_director);
        
        insert into stars_in_movies (starId, movieId) values (star_id, movie_id);
        insert into genres_in_movies (genreId, movieId) values (genre_id, movie_id);
        
        set status_message = 'Success';
	else
		set status_message = 'Failure';
	end if;
    select status_message as status, movie_id as newMovieId, star_id as newStarId, genre_id as newGenreId;
    commit;
	
end //
delimiter ;
