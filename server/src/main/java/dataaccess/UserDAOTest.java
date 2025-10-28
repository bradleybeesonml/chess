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
    void setUp() {
        userDAO = new MySQLUserDAO();
        try {
            userDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
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
    }

}