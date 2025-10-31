package service;
import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;
import model.*;
import dataaccess.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;

    }

    public RegisterResult register(RegisterRequest request) throws BadRequestException, AlreadyTakenException, DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null ||
                request.username().isEmpty() || request.password().isEmpty() || request.email().isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }

        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException("Already taken");
        }

        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        authDAO.insertAuth(new AuthData(authToken, request.username()));

        return new RegisterResult(request.username(), authToken);
    }

    public LoginResult login(LoginRequest request) throws BadRequestException, UnauthorizedException, DataAccessException{
        if (request.username() == null || request.password() == null ||
                request.username().isEmpty() || request.password().isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }

        UserData user = userDAO.getUser(request.username());
        if (user == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        if (!BCrypt.checkpw(request.password(), user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.insertAuth(new AuthData(authToken, request.username()));

        return new LoginResult(request.username(), authToken);
    }

    public void logout(LogoutRequest request) throws UnauthorizedException, DataAccessException{        
        AuthData authTokenLogout = authDAO.getAuth(request.authToken());
        if (authTokenLogout == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        else{
            authDAO.deleteAuth(request.authToken());
        }
    }

}