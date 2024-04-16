import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLParserStars extends DefaultHandler {
    private final HashSet<Star> myStars;
    private Star tempStar;
    private final HashSet<StarsInMovies> myStarsInMovies;
    private StarsInMovies tempStarInMovie;
    private String tempVal;

    int newStarId;
    private boolean flag;
    private int numDuplicateStarsInMovies;
    private int moviesRowsAffected;
    String file;

    private final HashSet<String> invalidMovieIds;
    private final HashMap<String, String> mapExistingXmlMovieIdToDbMovieId;


    public XMLParserStars(HashMap<String, String> existingMovieIds) throws SQLException, ClassNotFoundException {
        myStars = new HashSet<>();
        myStarsInMovies = new HashSet<>();
        invalidMovieIds = new HashSet<>();
        numDuplicateStarsInMovies = 0;
        moviesRowsAffected = 0;
        mapExistingXmlMovieIdToDbMovieId = existingMovieIds;
        getNewStarId();
    }

    public void run(boolean insert) throws SQLException, ClassNotFoundException {
        parseActorDocument();
        parseCastDocument();


        if (insert) {
            insertIntoDB();
            removeInvalidEntriesInDB();
        }

        printData();
    }

    private void getNewStarId() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(DatabaseParameters.dburl, DatabaseParameters.dbusername, DatabaseParameters.dbpassword);
        String maxId;
        PreparedStatement statement = connection.prepareStatement("SELECT max(id) AS id from stars");
        try(ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            maxId = resultSet.getString("id");
        }
        statement.close();
        connection.close();

        newStarId = Integer.parseInt(maxId.substring(2)) + 1;
    }

    private void parseActorDocument() {
        file = "actor";
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("stanford-movies/actors63.xml", this);
        } catch (SAXException | ParserConfigurationException | IOException se) {
            se.printStackTrace();
        }
    }

    private void parseCastDocument() {
        file = "cast";
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("stanford-movies/casts124.xml", this);
        } catch (SAXException | ParserConfigurationException | IOException se) {
            se.printStackTrace();
        }
    }

    private void printData() {
        System.out.println("Inserted " + myStars.size() + " stars.");
        System.out.println("Inserted " + myStarsInMovies.size() + " stars_in_movies.");
        System.out.println(numDuplicateStarsInMovies + " stars_in_movies duplicate.");
        System.out.println("Found " + invalidMovieIds.size() + " inconsistent movies.");
        System.out.println("Removed " + moviesRowsAffected + " movies that have no stars or genres.");
    }


    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        flag = true;
        if (file.equals("actor") && qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        } else if (file.equals("cast") && qName.equalsIgnoreCase("m")) {
            tempStarInMovie = new StarsInMovies();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        if (file.equals("actor")) {
            if (flag && qName.equalsIgnoreCase("actor")) {
                if (tempStar.getName() != null) {
                    tempStar.setId("nm" + newStarId);
                    newStarId++;
                    myStars.add(tempStar);
                }
            } else if (qName.equalsIgnoreCase("stagename")) {
                if (tempVal != null && !tempVal.strip().equals("")) {
                    tempStar.setName(tempVal);
                }
            } else if (qName.equalsIgnoreCase("dob")) {
                try {
                    tempStar.setBirthYear(Integer.parseInt(tempVal));
                } catch (NumberFormatException | NullPointerException e) {
                    tempStar.setBirthYear(null);
                }
            }
        } else if (file.equals("cast")) {
            if (flag && qName.equalsIgnoreCase("m")) {
                if (!myStarsInMovies.contains(tempStarInMovie) && tempStarInMovie.getMovieId() != null && tempStarInMovie.getStarId() != null) {
                    myStarsInMovies.add(tempStarInMovie);
                } else if (myStarsInMovies.contains(tempStarInMovie)) {
                    numDuplicateStarsInMovies++;
                }
            } else if (qName.equalsIgnoreCase("a")) {
                boolean found = false;
                for (Star s : myStars) {
                    if (s.getName().equals(tempVal)) {
                        tempStarInMovie.setStarId(s.getId());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tempStarInMovie.setStarId(null);
                    flag = false;
                }
            } else if (qName.equalsIgnoreCase("f")) {
                if (mapExistingXmlMovieIdToDbMovieId.containsKey(tempVal)) {
                    tempStarInMovie.setMovieId(mapExistingXmlMovieIdToDbMovieId.get(tempVal));
                } else {
                    invalidMovieIds.add(tempVal);
                    tempStarInMovie.setMovieId(null);
                    flag = false;
                }
            }
        }
    }


    public void insertIntoDB() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection(DatabaseParameters.dburl, DatabaseParameters.dbusername, DatabaseParameters.dbpassword)) {
            connection.setAutoCommit(false);

            insertStars(connection);
//            System.out.println("Done with inserting into stars Table");
            connection.commit();

            insertStarsInMovies(connection);

//            System.out.println("Done with inserting into stars_in_movies Table");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertStarsInMovies(Connection connection) throws SQLException {
        try (PreparedStatement starsInMoviesPreparedStatement = connection.prepareStatement("INSERT INTO stars_in_movies VALUES(?,?);")) {
            int counter = 0;
            for (StarsInMovies sm : myStarsInMovies) {
                starsInMoviesPreparedStatement.setString(1, sm.getStarId());
                starsInMoviesPreparedStatement.setString(2, sm.getMovieId());
                starsInMoviesPreparedStatement.addBatch();
                counter++;

                if (counter >= 1) {
                    starsInMoviesPreparedStatement.executeBatch();
                    counter = 0;
                }
            }
            starsInMoviesPreparedStatement.executeBatch();
        }
    }

    private void insertStars(Connection connection) throws SQLException {
        try (PreparedStatement starPreparedStatement = connection.prepareStatement("INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);")) {
            int counter = 0;
            for (Star s : myStars) {
                starPreparedStatement.setString(1, s.getId());
                starPreparedStatement.setString(2, s.getName());
                if (s.getBirthYear() != null) {
                    starPreparedStatement.setInt(3, s.getBirthYear());
                } else {
                    starPreparedStatement.setNull(3, Types.INTEGER);
                }
                starPreparedStatement.addBatch();

                counter++;
                if (counter >= 1000) {
                    starPreparedStatement.executeBatch();
                    counter = 0;
                }
            }
            starPreparedStatement.executeBatch();
        }
    }

    private void removeInvalidEntriesInDB() {
        try (Connection connection = DriverManager.getConnection(DatabaseParameters.dburl, DatabaseParameters.dbusername, DatabaseParameters.dbpassword)) {
            Statement statement = connection.createStatement();

            String deleteMoviesQuery = "DELETE FROM movies WHERE id NOT IN (SELECT movieId FROM stars_in_movies) OR id NOT IN (SELECT movieId FROM genres_in_movies)";
            moviesRowsAffected = statement.executeUpdate(deleteMoviesQuery);
//            System.out.println(moviesRowsAffected + " rows deleted from movies.");

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        XMLParserMovies main32Parser = new XMLParserMovies();
        main32Parser.run(false);
        HashMap<String, String> existingMovieIds = main32Parser.getMapXmlMovieIdToDbMovieId();

        XMLParserStars parser = new XMLParserStars(existingMovieIds);
        parser.run(false);
    }
}

