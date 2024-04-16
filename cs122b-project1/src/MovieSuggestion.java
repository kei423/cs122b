import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "MovieSuggestion", urlPatterns = "/api/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        JsonArray jsonArray = new JsonArray();
        String query = request.getParameter("query");

        if (query == null || query.trim().isEmpty()) {
            response.getWriter().write(jsonArray.toString());
            return;
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String sqlQuery = "SELECT m.id AS mID, m.title AS mTitle " +
                    "FROM movies m " +
                    "WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) " +
                    "LIMIT 10;";

            StringBuilder words = new StringBuilder("'");
            for (String word : query.split(" ")) {
                words.append("+").append(word).append("* ");
            }
            words.append("'");

            System.out.println("words: " + words);
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, words.toString());

            try(ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    jsonArray.add(generateJsonObject(resultSet.getString("mId"), resultSet.getString("mTitle")));
                }
            }

            statement.close();
            response.getWriter().write(jsonArray.toString());
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private static JsonObject generateJsonObject(String movieID, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieID", movieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
