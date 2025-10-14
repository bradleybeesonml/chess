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
        if (request.username() == null || request.password() == null || request.email() == null ||
                request.username().isEmpty() || request.password().isEmpty() || request.email().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        if (userDAO.getUser(request.username()) != null) {
            throw new DataAccessException("Already taken");
        }

        UserData newUser = new UserData(request.username(), request.email(), request.password());
        userDAO.insertUser(newUser);

        String authToken = UUID.randomUUID().toString();
        authDAO.insertAuth(new AuthData(authToken, request.username()));

        return new RegisterResult(request.username(), authToken);
    }
}