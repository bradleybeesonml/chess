package server;

import com.google.gson.Gson;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class WebSocketHandler {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final ConnectionManager connections;
    private final Gson gson = new Gson();

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO, ConnectionManager connections) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
        this.connections = connections;
    }

    public void onMessage(WsContext ctx, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                // Other cases will come later
            }
        } catch (Exception e) {
            sendError(ctx, "Error processing message: " + e.getMessage());
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) {
        try {
            // 1. Verify authToken
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: Invalid auth token");
                return;
            }

            // 2. Get game
            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            // 3. Add this connection to the game
            connections.addConnection(command.getGameID(), ctx);

            // 4. Send LOAD_GAME to the connecting user (root client)
            LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
            sendMessage(ctx, loadMsg);

            // 5. Determine if player or observer
            String username = auth.username();
            String role = determineRole(gameData, username);

            // 6. Send NOTIFICATION to all OTHER users in the game
            NotificationMessage notification = new NotificationMessage(
                    username + " joined the game as " + role
            );
            connections.broadcast(
                    command.getGameID(),
                    gson.toJson(notification),
                    ctx  // exclude the connecting user
            );

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private String determineRole(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return "white player";
        } else if (username.equals(gameData.blackUsername())) {
            return "black player";
        } else {
            return "an observer";
        }
    }

    private void sendMessage(WsContext ctx, Object message) {
        ctx.send(gson.toJson(message));
    }

    private void sendError(WsContext ctx, String errorMessage) {
        sendMessage(ctx, new ErrorMessage(errorMessage));
    }
}