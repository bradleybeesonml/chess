package dataaccess;

import model.GameData;
import model.UserData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class MySQLGameDAO implements GameDAO {

    public MySQLGameDAO() {
        configureDatabase();
    }

    private void configureDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                var createUserTable = """
                    CREATE TABLE IF NOT EXISTS games (
                        IMPLEMENT COLUMNS
                    )""";
                try (var statement = conn.prepareStatement(createUserTable)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new RuntimeException("Unable to configure database: " + ex.getMessage());
        }
    }

    @Override
    public GameData insertGame(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}