import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.util.ArrayList;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

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
        JsonArray responseJsonArray = new JsonArray();

        HttpSession session = request.getSession();
        System.out.println("does it get here?");

        try (Connection conn = dataSource.getConnection()) {
            request.getServletContext().log("ConfirmationServlet Connected");
            conn.setAutoCommit(false);
            // insert sale entry into database
            String insertQuery;
            ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
            ArrayList<String> previousTitles = (ArrayList<String>) session.getAttribute("previousTitles");
            ArrayList<Integer> previousCounts = (ArrayList<Integer>) session.getAttribute("previousCounts");
            long millis = System.currentTimeMillis();

            if (previousItems == null) {
                // Should never reach here but just in case
                previousItems = new ArrayList<String>();
                previousTitles = new ArrayList<String>();
                previousCounts = new ArrayList<Integer>();
            }
            for (int i = 0; i < previousItems.size(); i++) {
                for (int j = 0; j < previousCounts.get(i); j++) {
                    insertQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        int customerId = (int) session.getAttribute("customerId");
                        String movieId = previousItems.get(i);
                        Date saleDate = new Date(millis);

                        preparedStatement.setInt(1, customerId);
                        preparedStatement.setString(2, movieId);
                        preparedStatement.setDate(3, saleDate);

                        int affectedRows = preparedStatement.executeUpdate();
                        conn.commit();
                        JsonObject responseJsonObject = new JsonObject();
                        if (affectedRows == 1) {
                            // The record was inserted successfully
                            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                int generatedId = generatedKeys.getInt(1);
                                responseJsonObject.addProperty("status", "success");
                                responseJsonObject.addProperty("message", "success");
                                responseJsonObject.addProperty("saleId", generatedId);
                                responseJsonObject.addProperty("movieId", movieId);
                                responseJsonObject.addProperty("movieName", previousTitles.get(i));
                            }
                        } else {
                            request.getServletContext().log("Inserting sale entry failed");
                            responseJsonObject.addProperty("status", "fail");
                            responseJsonObject.addProperty("message", "failed to insert sale entry");
                        }
                        responseJsonArray.add(responseJsonObject);
                    }
                }
            }
            // clear the cart
            session.setAttribute("previousItems", new ArrayList<String>());
            session.setAttribute("previousTitles", new ArrayList<String>());
            session.setAttribute("previousCounts", new ArrayList<Integer>());

            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.getMessage());
            responseJsonArray.add(responseJsonObject);
            request.getServletContext().log(e.getMessage());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        System.out.println(responseJsonArray);
        response.getWriter().write(responseJsonArray.toString());
    }
}