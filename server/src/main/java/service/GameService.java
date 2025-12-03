package service;
import dataaccess.*;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import model.*;
import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    
    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }
    
    public ListGamesResult listGames(ListGamesRequest request) throws UnauthorizedException, DataAccessException {
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        
        Collection<GameData> games = gameDAO.listGames();
        return new ListGamesResult(games);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws UnauthorizedException, DataAccessException {
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        if (request.gameName() == null || request.gameName().isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }
        
        GameData game = gameDAO.insertGame(request.gameName());
        return new CreateGameResult(game.gameID());
    }

    public void joinGame(JoinGameRequest request) throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        
        if (request.gameID() <= 0) {
            throw new BadRequestException("Error: bad request");
        }
        
        GameData existingGame = gameDAO.getGame(request.gameID());
        if (existingGame == null) {
            throw new BadRequestException("Error: bad request");
        }

        if (request.playerColor() == null || 
            (!request.playerColor().equals("WHITE") && !request.playerColor().equals("BLACK"))) {
            throw new BadRequestException("Error: bad request");
        }

        AuthData rejoiningAuth = authDAO.getAuth(request.authToken());
        String rejoiningUsername = rejoiningAuth.username();

        if (request.playerColor().equals("WHITE")) {
            if (existingGame.whiteUsername() != null && !existingGame.whiteUsername().equals(rejoiningUsername)) {
                throw new AlreadyTakenException("Error: already taken");
            }
        }
        if (request.playerColor().equals("BLACK")) {
            if (existingGame.blackUsername() != null && !existingGame.blackUsername().equals(rejoiningUsername)) {
                throw new AlreadyTakenException("Error: already taken");
            }
        }
        
        GameData updatedGame;
        if (request.playerColor().equals("WHITE")) {
            updatedGame = new GameData(
                existingGame.gameID(),
                auth.username(),
                existingGame.blackUsername(),
                existingGame.gameName(),
                existingGame.game()
            );
        } else {
            updatedGame = new GameData(
                existingGame.gameID(),
                existingGame.whiteUsername(),
                auth.username(),
                existingGame.gameName(),
                existingGame.game()
            );
        }
        
        gameDAO.updateGame(request.gameID(), updatedGame);
    }


}
