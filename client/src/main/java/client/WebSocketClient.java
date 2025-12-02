package client;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebSocketClient extends Endpoint {
    private final Session session;
    private final NotificationHandler notificationHandler;

    private final Gson gson = new Gson();

    public WebSocketClient(String url, NotificationHandler handler) throws Exception {
        this.notificationHandler = handler;

        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }
        });
    }

    private void handleMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage loadGame = gson.fromJson(message, LoadGameMessage.class);
                    notificationHandler.onLoadGame(loadGame);
                }
                case NOTIFICATION -> {
                    NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
                    notificationHandler.onNotification(notification);
                }
                case ERROR -> {
                    ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                    notificationHandler.onError(error);
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing message: " + e.getMessage());
        }
    }

    public void connect(String authToken, int gameID) throws IOException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT, authToken, gameID
        );
        sendCommand(command);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        sendCommand(command);
    }

    public void leave(String authToken, int gameID) throws IOException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.LEAVE, authToken, gameID
        );
        sendCommand(command);
    }

    public void resign(String authToken, int gameID) throws IOException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.RESIGN, authToken, gameID
        );
        sendCommand(command);
    }

    private void sendCommand(Object command) throws IOException {
        this.session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}