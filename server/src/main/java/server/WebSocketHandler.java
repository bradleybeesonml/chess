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

import java.util.Objects;

public class WebSocketHandler {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void onMessage(WsContext ctx, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            if (command.getCommandType() == UserGameCommand.CommandType.CONNECT) {
                handleConnect(ctx, command);
            }
        } catch (Exception e) {
            sendError(ctx, "Error processing message: " + e.getMessage());
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: Invalid auth token");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
            sendMessage(ctx, loadMsg);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void sendMessage(WsContext ctx, Object message) {
        ctx.send(gson.toJson(message));
    }

    private void sendError(WsContext ctx, String errorMessage) {
        sendMessage(ctx, new ErrorMessage(errorMessage));
    }
}