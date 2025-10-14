package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import dataaccess.*;
import service.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class Server {

    private final Javalin server;
    
    // Data Access Objects
    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

    // Services
    private final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);
    private final UserService userService = new UserService(userDAO, authDAO);
    

    private final Set<String> existingUsernames = new HashSet<>();
    private final Map<String, String> userPasswords = new HashMap<>();
    private final Map<String, String> userEmails = new HashMap<>();
    private final Map<String, String> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> {
            try {
                clearService.clear();
                
                // Also clear old data structures until other endpoints are refactored
                existingUsernames.clear();
                userPasswords.clear();
                userEmails.clear();
                authTokens.clear();
                games.clear();
                nextGameId.set(1);
                
                ctx.status(200);
                ctx.result("{}");
            } catch (DataAccessException e) {
                ctx.status(500);
                ctx.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        });

        server.post("user", ctx -> register(ctx));
        server.post("session", ctx -> login(ctx));
        server.delete("session", ctx -> logout(ctx));
        server.post("game", ctx -> createGame(ctx));
        server.get("game", ctx -> listGames(ctx));
        server.put("game", ctx -> joinGame(ctx));

        // Register your endpoints and exception handlers here.

    }

    private void joinGame(Context ctx){
        var serializer = new Gson();
        String authToken = ctx.header("authorization");
        String username = authTokens.get(authToken);

        if (!authTokens.containsKey(authToken)){ // validate auth
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
            return;
        }

        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, Map.class);

        Integer gameID = null;
        Object gameIdObj = req.get("gameID");
        if (gameIdObj instanceof Number) {
            gameID = ((Number) gameIdObj).intValue();
        }

        String color = (String) req.get("playerColor");

        if(color == null){
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        else if(!color.equals("WHITE") && !color.equals("BLACK")){
                ctx.status(400);
                ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
                return;
        }

        if (gameID == null) {
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        else{
            GameData existingGame = games.get(gameID);
            if (existingGame == null) {
                ctx.status(400);
                ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
                return;
            }
            if (color.equals("WHITE") && existingGame.whiteUsername() != null) {
                ctx.status(403);
                ctx.result(serializer.toJson(Map.of("message", "Error: already taken")));
                return;
            }
            if (color.equals("BLACK") && existingGame.blackUsername() != null) {
                ctx.status(403);
                ctx.result(serializer.toJson(Map.of("message", "Error: already taken")));
                return;
            }

            GameData updatedGame = getGameData(color, existingGame, username);
            games.put(gameID, updatedGame);
    
            ctx.status(200);
            ctx.result("{}");

        }

    }

    @NotNull
    private static GameData getGameData(String color, GameData existingGame, String username) {
        GameData updatedGame;
        if (color.equals("WHITE")) {
            updatedGame = new GameData(
                    existingGame.gameID(),
                    username,
                    existingGame.blackUsername(),
                    existingGame.gameName(),
                    existingGame.game()
            );
        }
        else {
            updatedGame = new GameData(
                    existingGame.gameID(),
                    existingGame.whiteUsername(),
                    username,
                    existingGame.gameName(),
                    existingGame.game()
            );
        }
        return updatedGame;
    }

    private void listGames(Context ctx){
        var serializer = new Gson();
        String authToken = ctx.header("authorization");

        if (!authTokens.containsKey(authToken)){ // validate auth
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
            return;
        }

        List gamesList = games.values().stream().map(game -> {
            var gameMap = new HashMap<String, Object>();
            gameMap.put("gameID", game.gameID());
            gameMap.put("blackUsername", game.blackUsername());
            gameMap.put("whiteUsername", game.whiteUsername());
            gameMap.put("gameName", game.gameName());
            return gameMap;
        }).toList();

        ctx.status(200);
        var res = Map.of("games", gamesList);
        ctx.result(serializer.toJson(res));
    }

    private void createGame(Context ctx){
        var serializer = new Gson();
        String authToken = ctx.header("authorization");
        String reqJson = ctx.body();
        var req = serializer.fromJson(reqJson, Map.class);
        String gameName = (String) req.get("gameName");

        if (gameName == null || gameName.isEmpty()){
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        if (!authTokens.containsKey(authToken)){ // validate auth
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
            return;
        }

        int gameID = nextGameId.getAndIncrement();
        GameData game = new GameData(gameID, null, null, gameName, new chess.ChessGame());
        games.put(gameID, game);

        ctx.status(200);
        var res = Map.of("gameID", gameID);
        ctx.result(serializer.toJson(res));

    }

    private void logout(Context ctx){
        var serializer = new Gson();
        String authToken = ctx.header("authorization");

        if (!authTokens.containsKey(authToken)){
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
            return;
        }

        authTokens.remove(authToken);
        ctx.status(200);
        ctx.result("{}");

    }

    private void login(Context ctx) {
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), Map.class);
            String username = (String) req.get("username");
            String password = (String) req.get("password");

            LoginRequest request = new LoginRequest(username, password);
            LoginResult result = userService.login(request);

            ctx.status(200);
            ctx.result(serializer.toJson(Map.of(
                "username", result.username(),
                "authToken", result.authToken()
            )));

        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void register(Context ctx){
        var serializer = new Gson();
        try{
            var req = serializer.fromJson(ctx.body(), Map.class);
        String username = (String) req.get("username");
        String password = (String) req.get("password");
        String email = (String) req.get("email");

        RegisterRequest request = new RegisterRequest(username, password, email);
        RegisterResult result = userService.register(request);
        ctx.status(200);
        ctx.result(serializer.toJson(Map.of("username", result.username(), "authToken", result.authToken())));
        }
        catch(BadRequestException e){
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
            return;
        }
        catch(AlreadyTakenException e){
            ctx.status(403);
            ctx.result(serializer.toJson(Map.of("message", "Error: already taken")));
            return;
        }

        catch(DataAccessException e){
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: internal server error")));
            return;
        }


        }


    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
