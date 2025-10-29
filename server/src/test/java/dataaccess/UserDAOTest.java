package dataaccess;

import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.UserDAO;
import model.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MySQLUserDAO();
        userDAO.clear(); //drop all rows from user table
    }

    @Test
    @DisplayName("Clear Users - Success")
    void clearTest() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    @DisplayName("Get User - Success")
    void getUserSuccess() throws DataAccessException{
        String testUsername = "bradley";
        String testPassword = "unhashedpass";
        String testEmail = "test@email.com";

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, testUsername);
                ps.setString(2, testPassword);
                ps.setString(3, testEmail);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't insert into users.", e);
        }
        UserData resultUser = userDAO.getUser(testUsername);

        assertNotNull(resultUser, "Retrieved user should not be null");
        assertEquals(testUsername, resultUser.username(), "Username should be bradley");

        try {
            userDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("Get User - Negative Case")
    void getUserNegative() throws DataAccessException{
        String testUsername = "negativeTestUser";
        String testPassword = "unhashedPassword";
        String testEmail = "test@email.com";

        try (var conn = DatabaseManager.getConnection()) {
            var getUserNegativeStatement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (var ps = conn.prepareStatement(getUserNegativeStatement)) {
                ps.setString(1, testUsername);
                ps.setString(2, testPassword);
                ps.setString(3, testEmail);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't insert into users.", e);
        }


        UserData resultUser = userDAO.getUser("ThisIsn'tARealUsername");
        assertNull(resultUser, "Should return null if no user exists for the specified username.");

    }

    @Test
    @DisplayName("Create User - Success")
    void createUserSuccess() throws DataAccessException{
        String testUsername = "testUser";
        String testPassword = "plaintextpassword";
        String testEmail = "test@email.com";
        UserData testUser = new UserData(testUsername, testPassword, testEmail);

        userDAO.createUser(testUser);
        UserData createdUser = userDAO.getUser("testUser");

        assertNotNull(createdUser, "Retrieved user should not be null");
        System.out.println("Created User:" + createdUser.username());
        assertEquals(testUsername, createdUser.username(), "Username should match.");
    }

    @Test
    @DisplayName("Create User - Negative Case")
    void createUserNegative() throws DataAccessException{
        String testUsername = "testUserFail";
        String testPassword = "plaintextpasswordFail";
        String testEmail = "test@emailFail.com";
        UserData testUserFail = new UserData(testUsername, testPassword, testEmail);

        assertThrows(DataAccessException.class, ()-> {
            userDAO.createUser(testUserFail);
            userDAO.createUser(testUserFail); // double insert of the same user should throw a DataAccessException

        });


    }

}