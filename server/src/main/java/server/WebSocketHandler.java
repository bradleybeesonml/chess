package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import java.util.HashSet;
import java.util.Set;

public class WebSocketHandler {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final ConnectionManager connections;
    private final Gson gson = new Gson();
    private final Set<Integer> finishedGames = new HashSet<>();

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
                case MAKE_MOVE -> handleMakeMove(ctx, message);
                case RESIGN -> handleResign(ctx, command);
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

            connections.addConnection(command.getGameID(), ctx);

            LoadGameMessage loadMsg = new LoadGameMessage(gameData.game());
            sendMessage(ctx, loadMsg);

            String username = auth.username();
            String role = determineRole(gameData, username);

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

    private void handleMakeMove(WsContext ctx, String message) {
        try {
            MakeMoveCommand command = gson.fromJson(message, MakeMoveCommand.class);

            if (finishedGames.contains(command.getGameID())) {
                sendError(ctx, "Sorry, the game is already over!");
                return;
            }

            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: invalid auth!");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "No game found");
                return;
            }

            ChessGame game = gameData.game();
            String username = auth.username();

            ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);
            if (playerColor == null) {
                sendError(ctx, "Error: You are not a player in this game");
                return;
            }
            if (game.getTeamTurn() != playerColor) {
                sendError(ctx, "Error: It's not your turn");
                return;
            }

            ChessMove move = command.getMove();
            game.makeMove(move);

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );

            gameDAO.updateGame(command.getGameID(), updatedGame);

            LoadGameMessage loadMsg = new LoadGameMessage(game);
            connections.broadcastToAll(command.getGameID(), gson.toJson(loadMsg));

            String moveDesc = createMoveForWebsocket(move);
            NotificationMessage notification = new NotificationMessage(
                    username + " made move: " + moveDesc
            );
            connections.broadcast(command.getGameID(), gson.toJson(notification), ctx);

            ChessGame.TeamColor opponentColor = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponentColor)) {
                NotificationMessage checkmateMsg = new NotificationMessage(
                        getPlayerUsername(gameData, opponentColor) + " is in checkmate!"
                );
                connections.broadcastToAll(command.getGameID(), gson.toJson(checkmateMsg));

            if (game.isInCheckmate(opponentColor) || game.isInStalemate(opponentColor)) {
                finishedGames.add(command.getGameID());
            }
            } else if (game.isInCheck(opponentColor)) {
                NotificationMessage checkMsg = new NotificationMessage(
                        getPlayerUsername(gameData, opponentColor) + " is in check!"
                );
                connections.broadcastToAll(command.getGameID(), gson.toJson(checkMsg));
            }

        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: Invalid move - " + e.getMessage());
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleResign(WsContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: invalid auth!");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            String username = auth.username();

            ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);
            if (playerColor == null) {
                sendError(ctx, "Error: Observers cannot resign");
                return;
            }

            if (finishedGames.contains(command.getGameID())) {
                sendError(ctx, "Error: Game is already over");
                return;
            }

            finishedGames.add(command.getGameID());

            NotificationMessage notification = new NotificationMessage(
                    username + " resigned. Game over!"
            );
            connections.broadcastToAll(command.getGameID(), gson.toJson(notification));

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Invalid auth token.");
                return;
            }

            GameData gameData = gameDAO.getGame(command.getGameID());
            if (gameData == null) {
                sendError(ctx, "Error: Game not found");
                return;
            }

            String username = auth.username();

            ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);
            if (playerColor != null) {
                GameData updatedGame = new GameData(
                        gameData.gameID(),
                        playerColor == ChessGame.TeamColor.WHITE ? null : gameData.whiteUsername(),
                        playerColor == ChessGame.TeamColor.BLACK ? null : gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game()
                );
                gameDAO.updateGame(command.getGameID(), updatedGame);
            }

            connections.removeConnection(command.getGameID(), ctx);

            NotificationMessage notification = new NotificationMessage(
                    username + " left the game"
            );
            connections.broadcast(command.getGameID(), gson.toJson(notification), ctx);

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private ChessGame.TeamColor getPlayerColor(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null; // Observer
    }

    private String getPlayerUsername(GameData gameData, ChessGame.TeamColor color) {
        return (color == ChessGame.TeamColor.WHITE)
                ? gameData.whiteUsername()
                : gameData.blackUsername();
    }

    private String createMoveForWebsocket(ChessMove move) {
        return move.getStartPosition().toString() + " -> " + move.getEndPosition().toString();
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