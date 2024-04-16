import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword implements DatabaseParameters {

    public static void encryptCustomerPassword(Connection connection, Statement statement) throws SQLException {

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT id, password from customers";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<PreparedStatement> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the ID and plain text password from current table
            String id = rs.getString("id");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = "UPDATE customers SET password = ? WHERE id = ?;";

            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, encryptedPassword);
            updateStatement.setInt(2, Integer.parseInt(id));
            updateQueryList.add(updateStatement);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (PreparedStatement updateQuery : updateQueryList) {
            int updateResult = updateQuery.executeUpdate();
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");
    }
    public static void encryptEmployeePassword(Connection connection, Statement statement) throws SQLException {

        // change the employee table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering employees table schema completed, " + alterResult + " rows affected");

        // get the email and password for each employee
        String query = "SELECT email, password from employees";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<PreparedStatement> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the ID and plain text password from current table
            String email = rs.getString("email");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = "UPDATE employees SET password = ? WHERE email = ?;";

            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, encryptedPassword);
            updateStatement.setString(2, email);
            updateQueryList.add(updateStatement);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (PreparedStatement updateQuery : updateQueryList) {
            int updateResult = updateQuery.executeUpdate();
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");
    }


    /*
     *
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(DatabaseParameters.dburl, DatabaseParameters.dbusername, DatabaseParameters.dbpassword);
        Statement statement = connection.createStatement();

        encryptCustomerPassword(connection, statement);
        encryptEmployeePassword(connection, statement);

        statement.close();
        connection.close();

        System.out.println("finished");

    }
}
