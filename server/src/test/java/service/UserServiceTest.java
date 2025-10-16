package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {
    
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    
    @BeforeEach
    void setUp() {
        // Create fresh DAO instances for each test to ensure isolation
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }
    
    @Test
    @Order(1)
    @DisplayName("Register User - Success")
    void registerUserSuccess() throws Exception {
        String username = "testuser";
        String password = "testpassword";
        String email = "test@example.com";
        RegisterRequest request = new RegisterRequest(username, password, email);

        RegisterResult result = userService.register(request);
        
        assertNotNull(result, "Register result should not be null");
        assertEquals(username, result.username(), "Username should match the request");
        assertNotNull(result.authToken(), "Auth token should be generated");
        assertFalse(result.authToken().isEmpty(), "Auth token should not be empty");
        
        UserData createdUser = userDAO.getUser(username);
        assertNotNull(createdUser, "User should be created in database");
        assertEquals(username, createdUser.username(), "Stored username should match");
        assertEquals(password, createdUser.password(), "Stored password should match");
        assertEquals(email, createdUser.email(), "Stored email should match");
        
        AuthData authData = authDAO.getAuth(result.authToken());
        assertNotNull(authData, "Auth data should be created");
        assertEquals(username, authData.username(), "Auth username should match");
        assertEquals(result.authToken(), authData.authToken(), "Auth token should match");
    }

    @Test
    @Order(1)
    @DisplayName("Register User - Success")
    void registerUserFail() {
        String username = "testuser";
        String password = null;
        String email = "test@example.com";
        RegisterRequest request = new RegisterRequest(username, password, email);

        assertThrows(BadRequestException.class, () -> {
            RegisterResult result = userService.register(request);
        });
    }
    
    @Test
    @DisplayName("Login User - Success")
    void loginUserSuccess() throws Exception{
        String username = "testuser";
        String password = "testpassword";
        String email = "test@email.com";

        UserData testUser = new UserData(username, password, email);
        userDAO.createUser(testUser);

        LoginRequest request = new LoginRequest(username, password);
        LoginResult result = userService.login(request);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Login User - Bad Request")
    void loginUserFail() throws Exception, BadRequestException{
        String username = "";
        String password = "testpassword";
        String email = "test@email.com";

        UserData testUser = new UserData(username, password, email);
        userDAO.createUser(testUser);

        LoginRequest request = new LoginRequest(username, password);

        assertThrows(BadRequestException.class, () -> {
            LoginResult result = userService.login(request);
        });
    }

}
