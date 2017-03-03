import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;

public class Main {
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, userName VARCHAR, address VARCHAR, email VARCHAR);");

    }

    public static ArrayList<User> selectUsers(Connection conn) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Integer id = results.getInt("id");
            String userName = results.getString("userName");
            String address = results.getString("address");
            String email = results.getString("email");
            users.add(new User(id, userName, address, email));
        }
        return users;
    }

    public static void insertUser(Connection conn, String userName, String address, String email) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?, ?)");
        stmt.setString(1, userName);
        stmt.setString(2, address);
        stmt.setString(3, email);
        stmt.execute();

    }

    public static void updateUser(Connection conn, String userName, String address, String email, Integer id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET userName = ?, address = ?, email = ? WHERE id = ?");
        stmt.setString(1, userName);
        stmt.setString(2, address);
        stmt.setString(3, email);
        stmt.setInt(4, id);
        stmt.execute();

    }

    public static void deleteUser(Connection conn, Integer id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM users WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();

    }

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.externalStaticFileLocation("public");
        Spark.init();

        Spark.get("/user", (request, response) -> {
            ArrayList<User> users = selectUsers(conn);
            JsonSerializer s = new JsonSerializer();
            return s.serialize(users);
        });
        Spark.post("/user", (request, response) -> {
            String json = request.body();
            JsonParser p = new JsonParser();
            User user = p.parse(json, User.class);
            insertUser(conn, user.getUsername(), user.getAddress(), user.getEmail());
            return "";
        });

        Spark.put("/user", (request, response) -> {
            String json = request.body();
            JsonParser p = new JsonParser();
            User user = p.parse(json, User.class);
            updateUser(conn, user.getUsername(), user.getAddress(), user.getEmail(), user.getId());
            return "";
        });

        Spark.delete("/user/:id", (request, response) -> {
            String id = request.queryParams(":id");
            Integer idNum = Integer.parseInt(id);
            deleteUser(conn, idNum);

            return "";
        });


    }
}


