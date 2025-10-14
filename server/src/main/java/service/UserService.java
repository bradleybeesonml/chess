package service;
import model.*;
import dataaccess.*;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;

    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {

    

    
}