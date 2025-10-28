package dataaccess;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import javax.xml.crypto.Data;

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
        } catch (DataAccessException e) { //commenting out to avoid clearing
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("Create User - Success") //Passing all tests, but I can't figure out why the database doesn't still have a user after running the tests!
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
            userDAO.createUser(testUserFail); // double insert of the same user

        });


    }

}