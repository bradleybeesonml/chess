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

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM auth")) {

            if (rs.next()) {
                int rowCount = rs.getInt(1);
                System.out.println("Checking rows after insert: " + rowCount);
                assertEquals(1, rowCount);
            }
        }
        catch(SQLException e){
            throw new DataAccessException("Couldn't get count from auth");
        }

    }

    @Test
    @DisplayName("Insert Auth - Negative Case")
    void insertAuthTestNegative() throws DataAccessException {
        AuthData testAuthData = new AuthData("", "");

        assertThrows(DataAccessException.class, ()->
        authDAO.insertAuth(testAuthData));

        AuthData testAuthDataNull = new AuthData(null, null);

        assertThrows(DataAccessException.class, ()->
                authDAO.insertAuth(testAuthDataNull));

    }

    @Test
    @DisplayName("Get Auth - Success")
    void getAuthTest() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "INSERT INTO auth (auth_token, username) " +
                            "VALUES ('testAuthToken', 'testUsername')"
            );
        }

        catch(SQLException e){
            throw new DataAccessException("Couldn't insert into auth");
        }

        AuthData resultAuth = authDAO.getAuth("testAuthToken");
        System.out.println("Returned auth: " + resultAuth);
        assertNotNull(resultAuth, "GetAuth should retrieve the test authdata");

    }

    @Test
    @DisplayName("Get Auth - Negative Case")
    void getAuthTestNegative() throws DataAccessException {

        AuthData resultAuth = authDAO.getAuth("nonexistentAuthToken");
        assertNull(resultAuth, "Auth should return null if auth table is empty.");



    }

    @Test
    @DisplayName("Clear Auth")
    void clearAuth() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "INSERT INTO auth (auth_token, username) " +
                            "VALUES ('testAuthToken', 'testUsername')"
            );
        }

        catch(SQLException e){
            throw new DataAccessException("Couldn't insert into auth");
        }

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM auth")) {

            if (rs.next()) {
                int rowCount = rs.getInt(1);
                System.out.println("Row count: " + rowCount);
                assertEquals(1, rowCount);
            }
        }
        catch(SQLException e){
            throw new DataAccessException("Couldn't get count from auth");
        }

        authDAO.clear();
        System.out.println("Clearing Auth...");

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM auth")) {

            if (rs.next()) {
                int rowCount = rs.getInt(1);
                System.out.println("Row count: " + rowCount);
                assertEquals(0, rowCount);
            }
        }
        catch(SQLException e){
            throw new DataAccessException("Couldn't get count from auth");
        }

    }

}