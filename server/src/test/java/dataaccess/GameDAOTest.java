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
        gameDAO.clear(); //drop all rows from games table
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
            System.out.println("No rows returned. Games table cleared successfully.");
        }
        catch(SQLException e){
            throw new DataAccessException("Couldn't SELECT rows from games.");
        }
    }

    @Test
    @DisplayName("Insert Game - Success")
    void insertGameSuccess() throws DataAccessException {
        String testGameName = "Test Chess Game";

        GameData insertedGame = gameDAO.insertGame(testGameName);

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM games WHERE game_name = ?")) {

            stmt.setString(1, testGameName);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                int rowCount = rs.getInt(1);
                System.out.println("Games with name [" + testGameName + "]: " + rowCount);
                assertEquals(1, rowCount, "Should have exactly one game with the test name");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't verify game insertion", e);
        }

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement("SELECT game_id, game_name, game_status FROM games WHERE game_name = ?")) {

            stmt.setString(1, testGameName);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                int dbGameId = rs.getInt("game_id");
                String dbGameName = rs.getString("game_name");
                String dbGameStatus = rs.getString("game_status");

                assertNotNull(dbGameStatus, "Game status json shouldn't be null");

                Gson gson = new Gson();
                ChessGame deserializedGame = gson.fromJson(dbGameStatus, ChessGame.class);
                assertNotNull(deserializedGame, "Deserialized game shouldn't be null");

                System.out.println("Successfully inserted and verified game: " + dbGameName + " with ID: " + dbGameId);
            } else {
                throw new DataAccessException("Couldn't retrieve inserted game!");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't retrieve inserted game!", e);
        }

    }

    @Test
    @DisplayName("Insert Game - Negative Case (Empty gameName string")
    void insertGameNegative() throws DataAccessException {
        String testGameName = "";

        assertThrows(DataAccessException.class, ()->{
            GameData insertedGame = gameDAO.insertGame(testGameName);
                });

    }

    @Test
    @DisplayName("Get Game - Success")
    void getGameTest() throws DataAccessException {

        ChessGame getTestGame = new ChessGame();
        String gameJson = new Gson().toJson(getTestGame);

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO games (game_id, white_username, black_username, game_name, game_status) VALUES (?, ?, ?, ?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, 9991);
                ps.setString(2, "testGetWhiteUsername");
                ps.setString(3, "testGetBlackUsername");
                ps.setString(4, "testGetGame1");
                ps.setString(5, gameJson);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't insert test game", e);
        }

        GameData returnedGame = gameDAO.getGame(9991);

        assertNotNull(returnedGame);
        System.out.println("Succesfully returned GameData :" + returnedGame);
        assertEquals(9991, returnedGame.gameID());
        assertEquals("testGetGame1", returnedGame.gameName());

    }

    @Test
    @DisplayName("Get Game - Negative Case")
    void getGameTestNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, ()->{
            GameData returnedGame = gameDAO.getGame(1);
        });

    }

    @Test
    @DisplayName("List Games - Success")
    void listGamesTest() throws DataAccessException {


    }
}