package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTest {
    
    private GameService gameService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    
    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);
    }
    
    @Test
    @Order(1)
    @DisplayName("List Games - Success")
    void listGamesSuccess() throws Exception {
    }
    
    @Test
    @Order(2)
    @DisplayName("List Games - Unauthorized")
    void listGamesFail() throws Exception {
    }
    
    @Test
    @Order(3)
    @DisplayName("Create Game - Success")
    void createGameSuccess() throws Exception {
    }
    
    @Test
    @Order(4)
    @DisplayName("Create Game - Bad Request")
    void createGameFail() throws Exception {
    }
    
    @Test
    @Order(5)
    @DisplayName("Join Game - Success")
    void joinGameSuccess() throws Exception {
    }
    
    @Test
    @Order(6)
    @DisplayName("Join Game - Already Taken")
    void joinGameFail() throws Exception {
    }
}
