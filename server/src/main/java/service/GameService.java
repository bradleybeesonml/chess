package service;
import dataaccess.*;
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
}
