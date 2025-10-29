package dataaccess;

import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import model.AuthData;

import java.sql.SQLException;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() {
        configureDatabase();
    }

    private void configureDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                var createAuthTable = """
                CREATE TABLE IF NOT EXISTS auth (
                auth_token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
                )""";
                try (var statement = conn.prepareStatement(createAuthTable)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new RuntimeException("Unable to configure database: " + ex.getMessage());
        }
    }

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {

        if (auth == null) {
            throw new DataAccessException("AuthData cannot be null!");
        }
        if (auth.authToken() == null || auth.authToken().trim().isEmpty()) {
            throw new DataAccessException("Auth token must not be null or an empty string");
        }
        if (auth.username() == null || auth.username().trim().isEmpty()) {
            throw new DataAccessException("Username must not be an empty string or null object ");
        }

        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    INSERT INTO auth (auth_token, username)
                    VALUES (?, ?)
                    """;
            try (var insertAuth = conn.prepareStatement(statement)) {
                insertAuth.setString(1, auth.authToken());
                insertAuth.setString(2, auth.username());
                insertAuth.executeUpdate();
            }
        }
        catch(SQLException e){
            throw new DataAccessException("Couldn't insert auth");
        }

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT auth_token, username
                    FROM auth
                    WHERE auth_token = ?;
                    """;

            try (var getAuthByToken = conn.prepareStatement(statement)) {
                getAuthByToken.setString(1, authToken);
                var result = getAuthByToken.executeQuery();
                if (result.next()) {
                    return new AuthData(
                            result.getString("auth_token"),
                            result.getString("username")
                    );
                } else {
                    return null;
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if(authToken == null || authToken.isEmpty()){
            throw new DataAccessException("Invalid authToken, cannot delete.");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                DELETE FROM auth
                WHERE auth_token = ?
                """;

            try (var deleteAuthByToken = conn.prepareStatement(statement)) {
                deleteAuthByToken.setString(1, authToken);
                deleteAuthByToken.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM auth";
            try (var clearAuth = conn.prepareStatement(statement)) {
                clearAuth.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
