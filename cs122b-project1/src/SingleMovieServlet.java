import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

        String id = request.getParameter("id");
        request.getServletContext().log("@SingleMovieServlet: retrieving info for movie id #" + id);

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT " +
                    "    m.id AS mID, " +
                    "    m.title AS mTitle, " +
                    "    m.year AS mYear, " +
                    "    m.director AS mDirector, " +
                    "    r.rating AS mRating, " +
                    "    GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) AS mGenres, " +
                    "    GROUP_CONCAT(DISTINCT s.name ORDER BY smc.stars_in_movies_count DESC, s.name ASC) AS mStars, " +
                    "    GROUP_CONCAT(DISTINCT s.id ORDER BY smc.stars_in_movies_count DESC, s.name ASC) AS mStarsId " +
                    "FROM movies m " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN ( " +
                    "    SELECT s.id AS starId, COUNT(sim.movieId) AS stars_in_movies_count " +
                    "    FROM stars s " +
                    "    LEFT JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "    GROUP BY s.id " +
                    ") smc ON s.id = smc.starId " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating;";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);

            try(ResultSet resultSet = statement.executeQuery()) {
                JsonObject jsonObject = new JsonObject();
                while (resultSet.next()) {
                    jsonObject.addProperty("movieId", resultSet.getString("mId"));
                    jsonObject.addProperty("movieTitle", resultSet.getString("mTitle"));
                    jsonObject.addProperty("movieYear", resultSet.getString("mYear"));
                    jsonObject.addProperty("movieDirector", resultSet.getString("mDirector"));
                    jsonObject.addProperty("movieRating", resultSet.getString("mRating"));
                    jsonObject.addProperty("movieGenres", resultSet.getString("mGenres"));
                    jsonObject.addProperty("movieStars", resultSet.getString("mStars"));
                    jsonObject.addProperty("movieStarsId", resultSet.getString("mStarsId"));
                }
                out.write(jsonObject.toString());
            }

            statement.close();
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
}
