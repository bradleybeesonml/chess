package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;

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




}
