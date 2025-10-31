package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.GameDAO;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

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
        if(gameName == null || gameName.isEmpty()){
            throw new DataAccessException("gameName cannot be null or an empty string.");
        }
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
        if(gameID < 0){
            throw new DataAccessException("Invalid gameID");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT * FROM games WHERE game_id = ?
                    """;
            try (var getGameByID = conn.prepareStatement(statement)) {
                getGameByID.setInt(1, gameID);
                var result = getGameByID.executeQuery();
                if (result.next()) {
                    var gson = new Gson();
                    var chessGameString = gson.fromJson(result.getString("game_status"), ChessGame.class);
                    return new GameData(
                            result.getInt("game_id"),
                            result.getString("white_username"),
                            result.getString("black_username"),
                            result.getString("game_name"),
                            chessGameString
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
    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                SELECT game_id, white_username, black_username, game_name, game_status
                FROM games
                """;
            try (var listGamesStmt = conn.prepareStatement(statement)) {
                var rs = listGamesStmt.executeQuery();
                var gson = new Gson();

                while (rs.next()) {
                    try {
                        var chessGame = gson.fromJson(rs.getString("game_status"), ChessGame.class);
                        games.add(new GameData(
                                rs.getInt("game_id"),
                                rs.getString("white_username"),
                                rs.getString("black_username"),
                                rs.getString("game_name"),
                                chessGame
                        ));
                    } catch (Exception e) {
                        throw new DataAccessException("Couldn't deserialize game!" + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't list games");
        }
        return games;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if(gameID == 0 || game == null){
            throw new DataAccessException("GameID should be a positive int and game should not be null");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = """
                UPDATE games
                SET white_username = ?, black_username = ?, game_name = ?, game_status = ?
                WHERE game_id = ?
                """;
            try (var updateGame = conn.prepareStatement(statement)) {
                var gson = new Gson();
                var gameStatusJSON = gson.toJson(game.game());

                updateGame.setString(1, game.whiteUsername());
                updateGame.setString(2, game.blackUsername());
                updateGame.setString(3, game.gameName());
                updateGame.setString(4, gameStatusJSON);
                updateGame.setInt(5, gameID);

                updateGame.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't update game: " + e.getMessage());
        }

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