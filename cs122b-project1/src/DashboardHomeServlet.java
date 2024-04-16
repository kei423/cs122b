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

@WebServlet(name = "DaskboardHomeServlet", urlPatterns = "/_dashboard/index")
public class DashboardHomeServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SHOW TABLES";

            PreparedStatement statement = conn.prepareStatement(query);

            try (ResultSet resultSet = statement.executeQuery()) {
                JsonArray jsonArray = new JsonArray();

                while (resultSet.next()) {
                    String tableName = resultSet.getString(1);
                    if (!(tableName.equals("customers_backup") || tableName.equals("employees_backup"))) {
                        JsonObject tableMetadata = fetchTableMetadata(conn, tableName);
                        jsonArray.add(tableMetadata);
                    }
                }

                out.write(jsonArray.toString());
            }

            statement.close();
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private JsonObject fetchTableMetadata(Connection conn, String tableName) throws Exception {
        String query = "DESCRIBE " + tableName;

        try (PreparedStatement statement = conn.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            JsonArray jsonArray = new JsonArray();

            while (resultSet.next()) {
                JsonObject jsonObject = new JsonObject();

                String fieldName = resultSet.getString("Field");
                String fieldType = resultSet.getString("Type");

                fieldType = removeLengthSpecifier(fieldType);

                jsonObject.addProperty("Field", fieldName);
                jsonObject.addProperty("Type", fieldType);

                jsonArray.add(jsonObject);
            }

            JsonObject tableMetadata = new JsonObject();
            tableMetadata.addProperty("tableName", tableName);
            tableMetadata.add("columns", jsonArray);
            return tableMetadata;
        }
    }

    private String removeLengthSpecifier(String type) {
        return type.replaceAll("\\(\\d+\\)", "");
    }


}
