package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {

    public MySQLUserDAO() {
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    INSERT INTO users (username, password, email)
                    VALUES (?, ?, ?)
                    """;
            try (var insertUser = conn.prepareStatement(statement)) {
                insertUser.setString(1, user.username());
                insertUser.setString(2, hashedPassword);
                insertUser.setString(3, user.email());
                insertUser.executeUpdate();
            }
            catch (SQLException e) {
                throw new DataAccessException("Couldn't insert user (probably already taken)");
            }
        }
        catch (SQLException e) {
            throw new DataAccessException("Couldn't create user" + e.getMessage(), e);
            }
        }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                SELECT username, password, email
                FROM users
                WHERE username = ?;
                """;

            try (var getUserByUsername = conn.prepareStatement(statement)) {
                getUserByUsername.setString(1, username);
                var result = getUserByUsername.executeQuery();
                if(result.next()) { //checking if the result was null kept giving a sql error, changed to result.next(
                    return new UserData(
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("email")
                    );
                }
                else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM users";
            try (var clearUsers = conn.prepareStatement(statement)) {
                clearUsers.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                var createUserTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(255) PRIMARY KEY,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL
                    )""";
                try (var statement = conn.prepareStatement(createUserTable)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new RuntimeException("Unable to configure database: " + ex.getMessage());
        }
    }
}