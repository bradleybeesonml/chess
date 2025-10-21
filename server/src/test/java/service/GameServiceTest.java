package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        String username = "testuser";
        String authToken = "authToken";
        AuthData authData = new AuthData(authToken, username);
        authDAO.insertAuth(authData);

        Random random = new Random();
        int numGames = random.nextInt(8) + 3; //create between 3 and 8 games

        List<GameData> createdGames = new ArrayList<>();
        for (int i = 0; i < numGames; i++) {
            GameData game = gameDAO.insertGame("Rnadom game #: " + (i + 1));
            createdGames.add(game);
        }

        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = gameService.listGames(request);

        assertNotNull(result, "Should contain at least 3 games");
        assertEquals(numGames, result.games().size(),
                "Make sure all created games are listed");
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
