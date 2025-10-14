package dataaccess;
import model.UserData;
import java.util.Map;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }

    public UserData getUser(String username){
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }


}
