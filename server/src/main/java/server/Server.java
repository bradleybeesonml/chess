package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import model.GameData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private final Javalin server;

    private final Set<String> existingUsernames = new HashSet<>();
    private final Map<String, String> userPasswords = new HashMap<>();
    private final Map<String, String> userEmails = new HashMap<>();
    private final Map<String, String> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", ctx -> register(ctx));

        // Register your endpoints and exception handlers here.

    }

    private void register(Context ctx){
        var serializer = new Gson();
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, Map.class);

        String username = (String) req.get("username");
        String password = (String) req.get("password");
        String email = (String) req.get("email");

        if (username == null || password == null || email == null ||
                username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        var res = Map.of("username", req.get("username"), "authToken", "1234");
        ctx.result(serializer.toJson(res));


    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
