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
import java.util.ArrayList;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ArrayList<Integer> previousCounts = (ArrayList<Integer>) session.getAttribute("previousCounts");
        int price = 0;
        for (Integer itemCount : previousCounts) {
            price += 42 * itemCount;
        }
        response.setContentType("application/json"); // Response mime type
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("price", price);
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        String firstname = request.getParameter("first-name");
        String lastname = request.getParameter("last-name");
        Date expirationDate = Date.valueOf(request.getParameter("expiration-date"));

        response.setContentType("application/json"); // Response mime type
        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            request.getServletContext().log("PaymentServlet Connected");

            if (correctCreditCardInfo(id, firstname, lastname, expirationDate, conn)) {
                request.getServletContext().log("Payment successful");
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            } else {
                request.getServletContext().log("Payment failed: incorrect credit card information");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect credit card information");
            }

            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            responseJsonObject.addProperty("errorMessage", e.getMessage());
            request.getServletContext().log(e.getMessage());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        System.out.println(responseJsonObject);
        response.getWriter().write(responseJsonObject.toString());
    }

    private boolean correctCreditCardInfo(String id, String firstname, String lastname, Date expirationDate, Connection conn) throws SQLException {
        String query = "SELECT * FROM creditcards WHERE id=? AND firstName=? AND lastName=? AND expiration=?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, firstname);
            preparedStatement.setString(3, lastname);
            preparedStatement.setDate(4, expirationDate);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }
        return false;
    }
}