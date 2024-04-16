import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String isMobile = request.getParameter("mobile");
        if (isMobile != null && isMobile.equals("mobile")) {
            System.out.println(username);
            System.out.println(password);
            System.out.println(isMobile);
        }

        response.setContentType("application/json");
        JsonObject responseJsonObject = new JsonObject();

        // Verify reCAPTCHA
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            if (isMobile == null) RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            request.getServletContext().log("Login failed: Recaptcha verification failed");
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Recaptcha Verification Failed: Please complete the captcha");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        // Verify Username and Password
        try (Connection conn = dataSource.getConnection()) {
            request.getServletContext().log("LoginServlet Connected");
            int option = userExists(username, password, conn);
            if (option == 2) {
                request.getServletContext().log("Login successful");
                request.getSession().setAttribute("user", username);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                session.setAttribute("customerId", getCustomerId(username, conn));
            } else if (option == 1) {
                request.getServletContext().log("Login failed: password did not match");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Incorrect password");
            } else if (option == 0) {
                request.getServletContext().log("Login failed: username does not exist");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            }
            response.setStatus(200);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.getMessage());
            request.getServletContext().log(e.getMessage());
            response.setStatus(500);
        }

        response.getWriter().write(responseJsonObject.toString());
    }

    private int userExists(String username, String password, Connection conn) throws SQLException {
        int isValidUsername = 0;
        String query = "SELECT email, password FROM customers WHERE email=?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isValidUsername = 1;
                    String encryptedPassword = resultSet.getString("password");
                    if (new StrongPasswordEncryptor().checkPassword(password, encryptedPassword)) {
                        return 2;
                    }
                }
            }
        }
        return isValidUsername;
    }

    private int getCustomerId(String username, Connection conn) throws SQLException {
        String query = "SELECT id FROM customers WHERE email=?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return Integer.parseInt(resultSet.getString("id"));
            }
        }
    }
}
