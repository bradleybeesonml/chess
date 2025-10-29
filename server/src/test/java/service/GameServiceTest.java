package service;

import dataaccess.*;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
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
        assertThrows(UnauthorizedException.class, ()->{
            ListGamesRequest request = new ListGamesRequest("Invalid Auth Token!");
            ListGamesResult result = gameService.listGames(request);
    });

    }
    
    @Test
    @Order(3)
    @DisplayName("Create Game - Success")
    void createGameSuccess() throws Exception {
        String username = "testuser";
        String authToken = "authToken";
        AuthData authData = new AuthData(authToken, username);
        authDAO.insertAuth(authData);

        CreateGameRequest request = new CreateGameRequest(authToken, "Test Game");
        CreateGameResult result = gameService.createGame(request);

        GameData testGame = gameDAO.getGame(result.gameID());
        assertNotNull(testGame);

    }
    
    @Test
    @Order(4)
    @DisplayName("Create Game - Bad Request & Unauthorized")
    void createGameFail() throws Exception {
        String username = "testuser";
        String authToken = "authToken";
        AuthData authData = new AuthData(authToken, username);
        authDAO.insertAuth(authData);


        assertThrows(UnauthorizedException.class, () -> {
            CreateGameRequest request = new CreateGameRequest("Invalid AuthToken", "Test Game");
            CreateGameResult result = gameService.createGame(request);

        });

        assertThrows(BadRequestException.class, () -> {
            CreateGameRequest request = new CreateGameRequest(authToken, ""); //Game Name shouldn't be empty
            CreateGameResult result = gameService.createGame(request);

        });

    }
    
    @Test
    @Order(5)
    @DisplayName("Join Game - Success")
    void joinGameSuccess() throws Exception {
        String whiteUsername = "whiteUsername";
        String blackUsername = "blackUsername";
        String authToken = "authToken";
        String blackAuthToken = "blackAuthToken";
        AuthData authData = new AuthData(authToken, whiteUsername);
        AuthData blackAuthData = new AuthData(blackAuthToken, blackUsername);
        authDAO.insertAuth(authData);
        authDAO.insertAuth(blackAuthData);

        GameData testGgame = gameDAO.insertGame("Test Chess Game");
        int gameID = testGgame.gameID();

        JoinGameRequest whitePlayerRequest = new JoinGameRequest(authToken, "WHITE", gameID);
        gameService.joinGame(whitePlayerRequest);

        GameData joinedGame = gameDAO.getGame(gameID);

        assertEquals(whiteUsername, joinedGame.whiteUsername());
        assertNull(joinedGame.blackUsername(), "Only white should've joined the game at this point");

        //now join with black
        JoinGameRequest blackPlayerRequest = new JoinGameRequest(blackAuthToken, "BLACK", gameID);
        gameService.joinGame(blackPlayerRequest);

        GameData sameGame = gameDAO.getGame(gameID);

        assertEquals(blackUsername, sameGame.blackUsername());

    }


    
    @Test
    @Order(6)
    @DisplayName("Join Game - Already Taken")
    void joinGameFail() throws Exception {
        String username1 = "player1";
        String username2 = "player2";
        String authToken1 = "auth1";
        String authToken2 = "auth2";

        authDAO.insertAuth(new AuthData(authToken1, username1));
        authDAO.insertAuth(new AuthData(authToken2, username2));

        GameData game = gameDAO.insertGame("Test Game");
        int gameID = game.gameID();

        JoinGameRequest firstRequest = new JoinGameRequest(authToken1, "WHITE", gameID);
        gameService.joinGame(firstRequest);

        JoinGameRequest secondRequest = new JoinGameRequest(authToken2, "WHITE", gameID);

        assertThrows(AlreadyTakenException.class, () -> {
            gameService.joinGame(secondRequest);
        });
    }
}
