package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class MySQLAuthDAO implements AuthDAO{

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
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {


    }
}
