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
                var createGamesTable = """
                    CREATE TABLE IF NOT EXISTS games (
                    game_id INT AUTO_INCREMENT PRIMARY KEY,
                    white_username VARCHAR(255),
                    black_username VARCHAR(255),
                    game_name VARCHAR(255) NOT NULL,
                    game_status JSON NOT NULL
                    )""";
                try (var statement = conn.prepareStatement(createGamesTable)) {
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