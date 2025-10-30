package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.GameDAO;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
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
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    INSERT INTO games (game_name, game_status)
                    VALUES (?, ?)
                    """;
            try (var insertGameData = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                var gson = new Gson();
                var newGame = new ChessGame();
                var gameJson = gson.toJson(newGame);
                
                insertGameData.setString(1, gameName);
                insertGameData.setString(2, gameJson);
                insertGameData.executeUpdate();
                
                var generatedKeys = insertGameData.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int gameId = generatedKeys.getInt(1);
                    return new GameData(gameId, null, null, gameName, newGame);
                } else {
                    throw new DataAccessException("Failed to retrieve generated game ID");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't insert game: " + e.getMessage());
        }
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
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM games";
            try (var clearAuth = conn.prepareStatement(statement)) {
                clearAuth.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}