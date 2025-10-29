package dataaccess.interfaces;
import dataaccess.exceptions.DataAccessException;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    GameData insertGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(int gameID, GameData game) throws DataAccessException;
    void clear() throws DataAccessException;
}
