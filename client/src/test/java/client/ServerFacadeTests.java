package client;

import dataaccess.exceptions.DataAccessException;
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








}
