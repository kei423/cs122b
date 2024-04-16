import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParserMovies extends DefaultHandler {
    private final HashSet<Movie> myMovies;
    private Movie tempMovie;
    private String tempVal;
    private String tempXmlMovieId;
    private boolean flag;
    private int newMovieId;
    private int newGenreId;

    private final HashMap<Integer, String> newGenres;
    private final HashSet<String> invalidGenreNames;
    private final HashMap<String, Integer> mapGenreNameToGenreId;
    private HashMap<String, String> mapXmlGenreCatToDbGenreName;
    private final HashMap<String, String> mapXmlMovieIdToDbMovieId;
    private int numDuplicateMovies;
    private int numGenresInMoviesAdded;

    public XMLParserMovies() {
        myMovies = new HashSet<>();
        newGenres = new HashMap<>();
        invalidGenreNames = new HashSet<>();
        mapGenreNameToGenreId = new HashMap<>();
        mapXmlMovieIdToDbMovieId = new HashMap<>();
        numDuplicateMovies = 0;
        numGenresInMoviesAdded = 0;
    }

    public void run(boolean insert) throws SQLException, ClassNotFoundException {
        initMapGenres();
        getExistingDataFromDb();
        parseDocument();

        if (insert) {
            insertIntoDatabase();
        }

        printData();
    }

    public HashMap<String, String> getMapXmlMovieIdToDbMovieId() {
        return mapXmlMovieIdToDbMovieId;
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("stanford-movies/mains243.xml", this);
        } catch (SAXException | ParserConfigurationException | IOException se) {
            se.printStackTrace();
        }
    }

    private void printData() {
        System.out.println("Inserted " + myMovies.size() + " movies.");
        System.out.println(numDuplicateMovies + " movies duplicate.");
        System.out.println("Inserted " + mapGenreNameToGenreId.size() + " genres.");
        System.out.println("Found " + invalidGenreNames.size() + " inconsistent genres.");
        System.out.println("Inserted " + numGenresInMoviesAdded + " genres_in_movies.");
//        System.out.println("Invalid Genres: " + invalidGenreNames);
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            flag = true;
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        if (flag && qName.equalsIgnoreCase("film")) {
            if (tempMovie.getId() != null && tempMovie.getTitle() != null && tempMovie.getYear() != 0 && tempMovie.getDirector() != null) {
                myMovies.add(tempMovie);
                mapXmlMovieIdToDbMovieId.put(tempXmlMovieId, tempMovie.getId());
            }
        } else if (qName.equalsIgnoreCase("fid")) {
            if (mapXmlMovieIdToDbMovieId.containsKey(tempVal)) {
                numDuplicateMovies++;
                flag = false;
            } else {
                String newId = "tt0" + newMovieId;
                if (newId == "tt0544749") {
                    System.out.println("tes");
                }
                tempXmlMovieId = tempVal;
//                mapXmlMovieIdToDbMovieId.put(tempVal, newId);
                tempMovie.setId(newId);
                newMovieId++;
            }
        } else if (qName.equalsIgnoreCase("t")) {
            if (tempVal.equals("NKT") || tempVal.strip().equals("")) {
                flag = false;
            } else {
                tempMovie.setTitle(tempVal.strip());
            }
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempVal));
            } catch (NumberFormatException e) {
                flag = false;
                tempMovie.setYear(0);
            }
        } else if (qName.equalsIgnoreCase("dir")) {
            tempMovie.setDirector(tempVal);
        } else if (qName.equalsIgnoreCase("cat")) {
            for (String a : tempVal.split("[ .]")) {
                a = a.strip().toLowerCase();
                if (tempMovie.getGenres().contains(mapXmlGenreCatToDbGenreName.get(a))) {
                    return;
                }

                if (mapXmlGenreCatToDbGenreName.containsKey(a) && mapGenreNameToGenreId.containsKey(mapXmlGenreCatToDbGenreName.get(a))) {
                    tempMovie.addGenre(mapXmlGenreCatToDbGenreName.get(a));
                } else if (mapXmlGenreCatToDbGenreName.containsKey(a)) {
                    mapGenreNameToGenreId.put(mapXmlGenreCatToDbGenreName.get(a), newGenreId);
                    newGenres.put(newGenreId, mapXmlGenreCatToDbGenreName.get(a));
                    newGenreId++;
                    tempMovie.addGenre(mapXmlGenreCatToDbGenreName.get(a));
                } else {
                    invalidGenreNames.add(a);
                }
            }
        }
    }


    private void insertIntoDatabase() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection(DatabaseParameters.dburl, DatabaseParameters.dbusername, DatabaseParameters.dbpassword)) {
            connection.setAutoCommit(false);

            insertGenres(connection);
            insertMovies(connection);
//            System.out.println("Done with inserting into genres and movies Table");
            connection.commit();

            insertGenresInMovies(connection);
//            System.out.println("Done with inserting into genres_in_movies Table");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertGenres(Connection connection) throws SQLException {
        try (PreparedStatement genrePreparedStatement = connection.prepareStatement("INSERT INTO genres VALUES(?, ?);")) {
            for (Map.Entry<Integer, String> entry : newGenres.entrySet()) {
                genrePreparedStatement.setInt(1, entry.getKey());
                genrePreparedStatement.setString(2, entry.getValue());
                genrePreparedStatement.addBatch();
            }
            genrePreparedStatement.executeBatch();
        }
    }

    private void insertMovies(Connection connection) throws SQLException {
        try (PreparedStatement moviePreparedStatement = connection.prepareStatement("INSERT INTO movies VALUES(?, ?, ?, ?);")) {
            int counter = 0;
            for (Movie movie : myMovies) {
                moviePreparedStatement.setString(1, movie.getId());
                moviePreparedStatement.setString(2, movie.getTitle());
                moviePreparedStatement.setInt(3, movie.getYear());
                moviePreparedStatement.setString(4, movie.getDirector());
                moviePreparedStatement.addBatch();

                counter++;

                if (counter >= 1000) {
                    moviePreparedStatement.executeBatch();
                    counter = 0;
                }
            }
            moviePreparedStatement.executeBatch();
        }
    }

    private void insertGenresInMovies(Connection connection) throws SQLException {
        try (PreparedStatement genresInMoviesPreparedStatement = connection.prepareStatement("INSERT INTO genres_in_movies VALUES(?, ?);")) {
            int counter = 0;
            for (Movie movie : myMovies) {
                for (String genre : movie.getGenres()) {
                    genresInMoviesPreparedStatement.setInt(1, mapGenreNameToGenreId.get(genre));
                    genresInMoviesPreparedStatement.setString(2, movie.getId());
                    genresInMoviesPreparedStatement.addBatch();
                    numGenresInMoviesAdded++;
                }

                counter++;
                if (counter >= 1000) {
                    genresInMoviesPreparedStatement.executeBatch();
                    counter = 0;
                }
            }
            genresInMoviesPreparedStatement.executeBatch();
        }
    }


    private void getExistingDataFromDb() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(DatabaseParameters.dburl, DatabaseParameters.dbusername, DatabaseParameters.dbpassword);

        int genreId = 0;
        PreparedStatement statement = connection.prepareStatement("SELECT id, name from genres");
        try(ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                genreId = Integer.parseInt(resultSet.getString("id"));
                String name = resultSet.getString("name");
                mapGenreNameToGenreId.put(name, genreId);
            }
            newGenreId = genreId + 1;
        }

        String maxMovieId;
        statement = connection.prepareStatement("SELECT max(id) AS id from movies");
        try(ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            maxMovieId = resultSet.getString("id");
        }

        newMovieId = Integer.parseInt(maxMovieId.substring(2)) + 1;

        statement.close();

    }

    private void initMapGenres() {
        mapXmlGenreCatToDbGenreName = new HashMap<>();

        // Source: http://www-db.stanford.edu/pub/movies/doc.html#CATS

        mapXmlGenreCatToDbGenreName.put("actn", "Action");
        mapXmlGenreCatToDbGenreName.put("advt", "Adventure");
        mapXmlGenreCatToDbGenreName.put("avga", "Avant Garde");
        mapXmlGenreCatToDbGenreName.put("camp", "Camp");
        mapXmlGenreCatToDbGenreName.put("cart", "Cartoon");
        mapXmlGenreCatToDbGenreName.put("comd", "Comedy");
        mapXmlGenreCatToDbGenreName.put("cnr", "Cops and Robbers");
        mapXmlGenreCatToDbGenreName.put("ctxx", "Uncategorized");
        mapXmlGenreCatToDbGenreName.put("disa", "Disaster");
        mapXmlGenreCatToDbGenreName.put("docu", "Documentary");
        mapXmlGenreCatToDbGenreName.put("dram", "Drama");
        mapXmlGenreCatToDbGenreName.put("epic", "Epic");
        mapXmlGenreCatToDbGenreName.put("faml", "Family");
        mapXmlGenreCatToDbGenreName.put("fant", "Fantasy");
        mapXmlGenreCatToDbGenreName.put("hist", "History");
        mapXmlGenreCatToDbGenreName.put("horr", "Horror");
        mapXmlGenreCatToDbGenreName.put("musc", "Musical");
        mapXmlGenreCatToDbGenreName.put("myst", "Mystery");
        mapXmlGenreCatToDbGenreName.put("noir", "Black");
        mapXmlGenreCatToDbGenreName.put("porn", "Pornography");
        mapXmlGenreCatToDbGenreName.put("romt", "Romance");
        mapXmlGenreCatToDbGenreName.put("s.f.", "Sci-Fi");
        mapXmlGenreCatToDbGenreName.put("scfi", "Sci-Fi");
        mapXmlGenreCatToDbGenreName.put("surl", "Sureal");
        mapXmlGenreCatToDbGenreName.put("susp", "Thriller");
        mapXmlGenreCatToDbGenreName.put("west", "Western");
        mapXmlGenreCatToDbGenreName.put("biop", "Biography");
        mapXmlGenreCatToDbGenreName.put("tv", "TV-show");
        mapXmlGenreCatToDbGenreName.put("tvs", "TV-series");
        mapXmlGenreCatToDbGenreName.put("tvm", "TV-miniseries");
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // main32: movie id, movie title, movie year, movie dir, movie genres
        XMLParserMovies main32Parser = new XMLParserMovies();
        main32Parser.run(false);
    }
}