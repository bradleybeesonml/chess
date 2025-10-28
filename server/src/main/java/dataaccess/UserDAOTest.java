package dataaccess;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import javax.xml.crypto.Data;

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

}