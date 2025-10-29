package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.GameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTest {

    private GameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        gameDAO.clear(); //drop all rows from auth table
    }

    @Test
    @DisplayName("Clear")
    void clearGamesTest() throws DataAccessException {

        ChessGame testGame = new ChessGame();
        String gameJson = new Gson().toJson(testGame);

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO games (game_id, white_username, black_username, game_name, game_status) VALUES (?, ?, ?, ?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, 1234);
                ps.setString(2, "testWhiteUsername");
                ps.setString(3, "testBlackUsername");
                ps.setString(4, "testGame1");
                ps.setString(5, gameJson);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't insert test game", e);
        }

        gameDAO.clear();

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM games")) {

            assertFalse(rs.next());
        }
        catch(SQLException e){
            throw new DataAccessException("Couldn't get count from auth");
        }



    }
}