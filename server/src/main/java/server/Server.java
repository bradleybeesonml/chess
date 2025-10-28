package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import dataaccess.*;
import service.*;

import java.util.*;

public class Server {

    private final Javalin server;
    
    // Data Access Objects
    private final UserDAO userDAO = new MySQLUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MySQLGameDAO();

    // Services
    private final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);
    private final UserService userService = new UserService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);
    

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> {
            try {
                clearService.clear();
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

    private void joinGame(Context ctx) {
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            var req = serializer.fromJson(ctx.body(), Map.class);
            
            String playerColor = (String) req.get("playerColor");
            Integer gameID = null;
            
            Object gameIdObj = req.get("gameID");
            if (gameIdObj instanceof Number) {
                gameID = ((Number) gameIdObj).intValue();
            }
            
            JoinGameRequest request = new JoinGameRequest(authToken, playerColor, gameID != null ? gameID : 0);
            gameService.joinGame(request);
            
            ctx.status(200);
            ctx.result("{}");
            
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.result(serializer.toJson(Map.of("message", "Error: already taken")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }


    private void listGames(Context ctx){
        var serializer = new Gson();
        try{
            String authToken = ctx.header("authorization");

            ListGamesRequest request = new ListGamesRequest(authToken);
            ListGamesResult result = gameService.listGames(request);

            var gamesList = result.games().stream().map(game -> {
                var gameMap = new HashMap<String, Object>();
                gameMap.put("gameID", game.gameID());
                gameMap.put("whiteUsername", game.whiteUsername());
                gameMap.put("blackUsername", game.blackUsername());
                gameMap.put("gameName", game.gameName());
                return gameMap;
            }).toList();

            ctx.status(200);
            ctx.result(serializer.toJson(Map.of("games", gamesList)));
        }
        catch(UnauthorizedException e){
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
        }
        catch(DataAccessException e){
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void createGame(Context ctx) {
        var serializer = new Gson();
        try {
            String authToken = ctx.header("authorization");
            var req = serializer.fromJson(ctx.body(), Map.class);
            String gameName = (String) req.get("gameName");
    
            CreateGameRequest request = new CreateGameRequest(authToken, gameName);
            CreateGameResult result = gameService.createGame(request);
    
            ctx.status(200);
            ctx.result(serializer.toJson(Map.of("gameID", result.gameID())));
    
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.result(serializer.toJson(Map.of("message", "Error: bad request")));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void logout(Context ctx){
        var serializer = new Gson();
        

        try{
            String authToken = ctx.header("authorization");
            LogoutRequest request = new LogoutRequest(authToken);
            userService.logout(request);
            ctx.status(200);
            ctx.result("{}");
        }
        catch(UnauthorizedException e){
            ctx.status(401);
            ctx.result(serializer.toJson(Map.of("message", "Error: unauthorized")));
        }
        catch(DataAccessException e){
            ctx.status(500);
            ctx.result(serializer.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
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
