package client;

import dataaccess.exceptions.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws ResponseException {
        facade.clear();
    }

    @Test
    @DisplayName("Clear Database")
    void clearDatabase_works() throws ResponseException {
        facade.register("user1", "pass1", "u1@test.com");
        facade.register("user2", "pass2", "u2@test.com");

        assertDoesNotThrow(() -> facade.clear(), "Clear should not throw exception");
        
        assertThrows(ResponseException.class,
                () -> facade.login("user1", "pass1"),
                "User should not exist after clear");
    }

    @Test
    @DisplayName("Register success")
    void registerTestSuccess() throws ResponseException{
        AuthData testUser = facade.register("testUser", "testPassword", "test@test.com");
        assertNotNull(testUser);

        AuthData testLoginAfterRegister = facade.login("testUser", "testPassword");
        assertEquals("testUser", testLoginAfterRegister.username());
    }

    @Test
    @DisplayName("Register negative")
    void registerTestNegative() throws ResponseException{
        AuthData testUser = facade.register("testUser", "123", "test@test.com");
        assertNotNull(testUser);

        assertThrows(ResponseException.class,
                () -> facade.register("testUser", "123", "test@test.com"),
                "Should throw an error when registering a user that already exists.");
    }

    @Test
    @DisplayName("Login successful")
    void loginTestSuccess() throws ResponseException{
        AuthData testUser = facade.register("testUser", "123", "test@test.com");
        assertNotNull(testUser);

        AuthData loginUser = facade.login("testUser", "123");

        assertNotNull(loginUser);
        assertEquals("testUser", loginUser.username());
    }

    @Test
    @DisplayName("Login negative")
    void loginTestNegative() throws ResponseException{

        assertThrows(ResponseException.class,
                () ->facade.login("testUser", "012"),
                "Should get a response exception when trying to login as a nonexistent user");

        AuthData testUser = facade.register("testUser", "testPassword", "test@test.com");


        assertThrows(ResponseException.class,
                () ->facade.login("testUser", "012"),
                "Should get a response exception when trying to login with the wrong password");
    }

    @Test
    @DisplayName("Logout - Success")
    void logoutSuccess() throws ResponseException {
        AuthData authData = facade.register("logoutUserTest", "password", "logout@test.com");

        assertDoesNotThrow(() -> facade.logout(authData.authToken()),
                "Should logout with valid token");
    }

    @Test
    @DisplayName("Logout negative")
    void logoutTestNegative() throws ResponseException {
        assertThrows(ResponseException.class, () -> facade.logout("invalidAuthToken"),
                "Should throw an error if no valid authtoken");
    }

    @Test
    @DisplayName("Create Game success")
    void createGameSuccess() throws ResponseException {
        AuthData authData = facade.register("createGameTester", "password", "game@test.com");
        int gameID = facade.createGame(authData.authToken(), "Game Test 123");

        GameData[] gamesList = facade.listGames(authData.authToken());
        assertEquals(1, gamesList.length);
        assertEquals("Game Test 123", gamesList[0].gameName());

    }

    @Test
    @DisplayName("Create Game negative")
    void createGameNegative() throws ResponseException {
        assertThrows(ResponseException.class, () ->
        facade.createGame("randomAuthToken", "Game Test 123"),
                "Shouldn't create game if not logged in");

    }

    @Test
    @DisplayName("List Games success")
    void listGamesSuccess() throws ResponseException {
        AuthData authData = facade.register("createGameTester", "password", "game@test.com");
        int gameID = facade.createGame(authData.authToken(), "Game Test 123");
        int gameID2 = facade.createGame(authData.authToken(), "Game Test 1234");
        int gameID3 = facade.createGame(authData.authToken(), "Game Test 12345");

        GameData[] gamesList = facade.listGames(authData.authToken());
        assertEquals(3, gamesList.length);
        assertEquals("Game Test 123", gamesList[0].gameName());
        assertEquals("Game Test 1234", gamesList[1].gameName());
        assertEquals("Game Test 12345", gamesList[2].gameName());

    }

    @Test
    @DisplayName("List Games negative")
    void listGamesNegative() throws ResponseException {
        AuthData authData = facade.register("createGameTester", "password", "game@test.com");
        int gameID = facade.createGame(authData.authToken(), "Game Test 123");
        int gameID2 = facade.createGame(authData.authToken(), "Game Test 1234");
        int gameID3 = facade.createGame(authData.authToken(), "Game Test 12345");

        assertThrows(ResponseException.class, () ->
        facade.listGames("invalidAuthToken"),
                "Only list games if logged in"
                );
    }

    @Test
    @DisplayName("Join Game success")
    void joinGameSuccess() throws ResponseException {
        AuthData authData = facade.register("testUser", "password123", "test@test.com");
        int gameID = facade.createGame(authData.authToken(), "Testing Join Game 12345");

        assertDoesNotThrow(() -> facade.joinGame(authData.authToken(), gameID, "WHITE"),
                "joining as white player");

        GameData[] games = facade.listGames(authData.authToken());
        assertEquals("testUser", games[0].whiteUsername());

        int gameID2 = facade.createGame(authData.authToken(), "Testing Join Game 2");
        assertDoesNotThrow(() -> facade.joinGame(authData.authToken(), gameID2, "BLACK"),
                "joining as black");
        GameData[] gamesNew = facade.listGames(authData.authToken());
        assertEquals("testUser", gamesNew[1].blackUsername());


    }

    @Test
    @DisplayName("Join Game negative")
    void joinGameNegative() throws ResponseException {
        AuthData authData = facade.register("testUser", "password123", "test@test.com");
        int gameID = 3;

        assertThrows(ResponseException.class, () ->
            facade.joinGame(authData.authToken(), gameID, "WHITE"));


    }









}
