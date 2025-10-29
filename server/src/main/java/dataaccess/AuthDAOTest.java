package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTest {

    private AuthDAO authDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new MySQLAuthDAO();
        authDAO.clear(); //drop all rows from auth table
    }

    @Test
    @DisplayName("Insert Auth - Success")
    void insertAuthTest() throws DataAccessException {
        AuthData testAuthData = new AuthData("testAuthToken", "testUsername");
        authDAO.insertAuth(testAuthData);
        //ADD Test Case - querying database through the shell shows that the authData is being inserted correctly
    }

}