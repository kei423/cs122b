import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "DashboardInsertServlet", urlPatterns = "/_dashboard/insert")
public class DashboardInsertServlet extends HttpServlet {

    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        JsonObject responseJsonObject = new JsonObject();

        String action = request.getParameter("action");
        System.out.println("query type: " + action);

        switch (action) {
            case "insertStar": {
                String starName = request.getParameter("name");
                String birthYear = request.getParameter("year");
                if (starName.isEmpty()) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: Star name cannot be empty.");
                } else responseJsonObject = insertStarIntoDatabase(starName, birthYear, response);
                break;
            }
            case "insertGenre": {
                String genreName = request.getParameter("name");
                if (genreName.isEmpty()) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: Genre name cannot be empty.");
                } else responseJsonObject = insertGenreIntoDatabase(genreName, response);
                break;
            }
            case "insertMovie": {
                if (!validateParametersForMovie(request)) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: All parameters cannot be empty.");
                }
                else {
                    Movie toBeInserted = new Movie("bogusId", request.getParameter("title"),
                            Integer.parseInt(request.getParameter("year")), request.getParameter("director"));
                    toBeInserted.addStar(request.getParameter("star"));
                    toBeInserted.addGenre(request.getParameter("genre"));
                    responseJsonObject = insertMovieIntoDatabase(toBeInserted, response);
                }
                break;
            }
            default:
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Error: Caught unknown signal");
                break;
        }

        System.out.println(responseJsonObject);
        response.getWriter().write(responseJsonObject.toString());
    }

    private boolean validateParametersForMovie(HttpServletRequest request) {
        if (request.getParameter("title").isEmpty()) return false;
        if (request.getParameter("year").isEmpty()) return false;
        if (request.getParameter("director").isEmpty()) return false;
        if (request.getParameter("star").isEmpty()) return false;
        return !request.getParameter("genre").isEmpty();
    }

    private String getNewId(String table, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        String getIdQuery = "select max(id) as max_id from " + table;
        String maxId;
        try(ResultSet resultSet = statement.executeQuery(getIdQuery)) {
            resultSet.next();
            maxId = resultSet.getString("max_id");
        }
        String nonNumericPart = maxId.replaceAll("[0-9]", "");
        String numericPart = maxId.replaceAll("[^0-9]", "");
        int maxNumericValue = Integer.parseInt(numericPart);
        int newNumericValue = maxNumericValue + 1;
        return nonNumericPart + newNumericValue;
    }

    private JsonObject insertStarIntoDatabase(String name, String birthYear, HttpServletResponse response) {
        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {
                String newId = getNewId("stars", conn);
                preparedStatement.setString(1, newId);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, birthYear);

                int affectedRows = preparedStatement.executeUpdate();
                conn.commit();
                if (affectedRows == 1) {
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Successfully added new star. Star ID: " + newId);
                    responseJsonObject.addProperty("newId", newId);
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: failed to insert star entry");
                }
            }

            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.getMessage());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        return responseJsonObject;
    }

    private JsonObject insertGenreIntoDatabase(String name, HttpServletResponse response) {
        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String insertQuery = "INSERT INTO genres (name) VALUES (?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);

                int affectedRows = preparedStatement.executeUpdate();
                conn.commit();
                if (affectedRows == 1) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int genreId = generatedKeys.getInt(1);
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Successfully added genre. Genre ID: " + genreId);
                    }
                    else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Error: failed to retrieve genre ID");
                    }
                }
                else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: failed to insert genre entry");
                }
            }

            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.getMessage());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        return responseJsonObject;
    }

    private JsonObject insertMovieIntoDatabase(Movie toBeInserted, HttpServletResponse response) {
        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String insertQuery = "call add_movie(?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, toBeInserted.getTitle());
                preparedStatement.setInt(2, toBeInserted.getYear());
                preparedStatement.setString(3, toBeInserted.getDirector());
                preparedStatement.setString(4, toBeInserted.getStars().get(0));
                preparedStatement.setString(5, toBeInserted.getGenres().get(0));

                ResultSet resultSet = preparedStatement.executeQuery();
                conn.commit();
                if (resultSet.next()) {
                    if (resultSet.getString("status").equals("Success")) {
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Successfully added new movie. Movie ID: " +
                                resultSet.getString("newMovieId") + ". Star ID: " +
                                resultSet.getString("newStarId") + ". Genre ID: " +
                                resultSet.getString("newGenreId") + ".");
                    }
                    else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Error: Duplicate movie");
                    }
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: failed to insert movie entry");
                }
            }

            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.getMessage());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        return responseJsonObject;
    }
}