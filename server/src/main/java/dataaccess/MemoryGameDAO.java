package dataaccess;
import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.GameDAO;
import model.GameData;
import chess.ChessGame;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);
    
    @Override
    public GameData insertGame(String gameName) throws DataAccessException {
        int gameId = nextGameId.getAndIncrement();
        GameData game = new GameData(gameId, null, null, gameName, new ChessGame());
        games.put(gameId, game);
        return game;
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (!games.containsKey(gameID)) {
            throw new DataAccessException("Game not found");
        }
        games.put(gameID, game);
    }
    
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }
    
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }
    
    
    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextGameId.set(1);
    }
}