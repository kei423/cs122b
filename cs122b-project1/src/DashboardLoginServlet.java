import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/api/_dashboard/login")
public class DashboardLoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.setContentType("application/json");
        JsonObject responseJsonObject = new JsonObject();

        // Verify reCAPTCHA
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            request.getServletContext().log("Login failed: Recaptcha verification failed");
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Recaptcha Verification Failed: Please complete the captcha");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        // Verify Username and Password
        try (Connection conn = dataSource.getConnection()) {
            request.getServletContext().log("DashboardLoginServlet Connected");
            int option = employeeExists(username, password, conn);
            if (option == 2) {
                request.getServletContext().log("Login successful");
                request.getSession().setAttribute("employee", username);
                request.getSession().setAttribute("user", username);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            } else if (option == 1) {
                request.getServletContext().log("Login failed: password did not match");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Incorrect password");
            } else if (option == 0) {
                request.getServletContext().log("Login failed: username does not exist");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Employee " + username + " doesn't exist");
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

    private int employeeExists(String username, String password, Connection conn) throws SQLException {
        int isValidEmployee = 0;
        String query = "SELECT email, password FROM employees WHERE email=?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isValidEmployee = 1;
                    String encryptedPassword = resultSet.getString("password");
                    if (new StrongPasswordEncryptor().checkPassword(password, encryptedPassword)) {
                        return 2;
                    }
                }
            }
        }
        return isValidEmployee;
    }
}