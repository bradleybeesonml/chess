package dataaccess.interfaces;
import dataaccess.exceptions.DataAccessException;
import model.AuthData;

public interface AuthDAO {
    void insertAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
