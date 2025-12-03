package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.NotificationHandler;
import client.WebSocketClient;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Scanner;

public class GamePlayUI implements NotificationHandler {
    private final Scanner scanner;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor playerColor;  // null if observer
    private WebSocketClient ws;
    private ChessGame currentGame;

    public GamePlayUI(String serverUrl, String authToken, int gameID,
                      ChessGame.TeamColor playerColor, Scanner scanner) throws Exception {
        this.scanner = scanner;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;

        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        this.ws = new WebSocketClient(wsUrl, this);
        ws.connect(authToken, gameID);
    }

    public void run() {
        System.out.println("Entered game. Type 'help' for commands.");

        boolean inGame = true;
        while (inGame) {
            System.out.print("\n[IN GAME] >>> ");
            String input = scanner.nextLine().trim();
            inGame = handleCommand(input);
        }
    }

    private boolean handleCommand(String input) {
        String[] tokens = input.split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()){
            return true;
        }

        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> {
                showGameplayHelp();
                yield true;
            }
            case "redraw" -> {
                redrawBoard();
                yield true;
            }
            case "leave" -> {
                leaveGame();
                yield false;
            }
            case "move" -> {
                makeMove(tokens);
                yield true;
            }
            case "resign" -> {
                resignGame();
                yield true;
            }
            case "highlight" -> {
                highlightMoves(tokens);
                yield true;
            }
            default -> {
                System.out.println("Unknown command. Type 'help'.");
                yield true;
            }
        };
    }

    private void showGameplayHelp() {
        System.out.println("""
                Gameplay commands:
                  'redraw' - Redraw the chess board
                  'move' <from> <to> [promotion] - Make a move (e.g., 'move e2 e4' or 'move e7 e8 q')
                  'highlight' <position> - Show legal moves for a piece (e.g., 'highlight e2')
                  'resign' - Forfeit the game
                  'leave' - Leave the game (return to main menu)
                  'help' - Show this help message
                """);
    }

    private void printPrompt() {
        System.out.print("\n[IN GAME] >>> ");
    }

    private void redrawBoard() {
        if (currentGame == null) {
            System.out.println("No game to display.");
            return;
        }

        ChessBoard board = currentGame.getBoard();
        if (playerColor == ChessGame.TeamColor.BLACK) {
            BoardRender.drawBlackBoard(board);
        } else {
            BoardRender.drawWhiteBoard(board);  // White or observer
        }
    }

    private void leaveGame() {
        try {
            ws.leave(authToken, gameID);
            ws.close();
            System.out.println("Left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void makeMove(String[] tokens) {
        if (playerColor == null) {
            System.out.println("Observers cannot make moves.");
            return;
        }

        if (tokens.length < 3) {
            System.out.println("Usage: move <from> <to> [promotion]");
            System.out.println("Example: move e2 e4");
            System.out.println("Example: move e7 e8 q  (pawn promotion to queen)");
            return;
        }

        try {
            ChessPosition from = parsePosition(tokens[1]);
            ChessPosition to = parsePosition(tokens[2]);

            chess.ChessPiece.PieceType promotion = null;
            if (tokens.length >= 4) {
                promotion = parsePromotion(tokens[3]);
            }

            ChessMove move = new ChessMove(from, to, promotion);
            ws.makeMove(authToken, gameID, move);

        } catch (Exception e) {
            System.out.println("Invalid move: " + e.getMessage());
        }
    }

    private void resignGame() {
        if (playerColor == null) {
            System.out.println("Observers cannot resign.");
            return;
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) {
            try {
                ws.resign(authToken, gameID);
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void highlightMoves(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: highlight <position>");
            System.out.println("Example: highlight e2");
            return;
        }

        try {
            ChessPosition pos = parsePosition(tokens[1]);

            if (currentGame.getBoard().getPiece(pos) == null) {
                System.out.println("No piece at " + tokens[1]);
                return;
            }

            if (playerColor == ChessGame.TeamColor.BLACK) {
                BoardRender.highlightMovesBlack(currentGame, pos);
            } else {
                BoardRender.highlightMovesWhite(currentGame, pos);
            }

        } catch (Exception e) {
            System.out.println("Invalid position: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Position must be like 'e2'");
        }
        char colChar = Character.toLowerCase(pos.charAt(0));
        char rowChar = pos.charAt(1);

        int col = colChar - 'a' + 1;
        int row = rowChar - '0';

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Position out of range");
        }

        return new ChessPosition(row, col);
    }

    private chess.ChessPiece.PieceType parsePromotion(String piece) {
        return switch (piece.toLowerCase()) {
            case "q", "queen" -> chess.ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> chess.ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> chess.ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> chess.ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece");
        };
    }

    @Override
    public void onLoadGame(LoadGameMessage message) {
        this.currentGame = message.getGame();
        System.out.println();
        redrawBoard();
        printPrompt();
    }

    @Override
    public void onNotification(NotificationMessage message) {
        System.out.println("\n" + message.getMessage());
        printPrompt();
    }

    @Override
    public void onError(ErrorMessage message) {
        System.out.println("\nError: " + message.getErrorMessage());
        printPrompt();
    }

}



