package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.NotificationHandler;
import client.ServerFacade;
import client.ResponseException;
import client.WebSocketClient;
import model.AuthData;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import model.GameData;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;


public class ChessClient implements NotificationHandler {
    private final ServerFacade server;
    private final Scanner scanner;
    private String authToken = null;
    private String username = null;
    private State state = State.LOGGED_OUT;
    private final Map<Integer, Integer> gameNumberToId = new HashMap<>();
    private WebSocketClient ws;
    private int currentGameID;
    private ChessGame.TeamColor playerColor;  // null if observing
    private ChessGame currentGame;
    private final String serverUrl;

    private enum State {
        LOGGED_OUT,
        LOGGED_IN,
        PLAYING
    }

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Welcome to Chess! " +
                EscapeSequences.BLACK_BISHOP +
                EscapeSequences.WHITE_BISHOP +
                EscapeSequences.BLACK_KNIGHT +
                EscapeSequences.WHITE_KNIGHT +
                EscapeSequences.BLACK_QUEEN +
                EscapeSequences.WHITE_QUEEN);
        System.out.println("Type 'Help' to see available commands.");

        boolean running = true;
        while (running) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                running = handleCommand(line);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Thanks for playing!");
    }

    private boolean handleGameplayCommand(String command, String[] tokens) {
        switch (command) {
            case "help" -> {
                showGameplayHelp();
            }
            case "redraw" -> {
                redrawBoard();
            }
            case "leave" -> {
                leaveGame();
            }
            case "move" -> {
                makeMove(tokens);
            }
            case "resign" -> {
                resignGame();
            }
            case "highlight" -> {
                highlightMoves(tokens);
            }
            default -> {
                System.out.println("Unknown command. Type 'help' for options.");
            }
        }
        return true;
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
        if (state == State.LOGGED_OUT) {
            System.out.print("\n[LOGGED_OUT] >>> ");
        } else if (state == State.PLAYING) {
            System.out.print("\n[IN GAME] >>> ");
        } else {
            System.out.print("\n[" + username + "] >>> ");
        }
    }

    private boolean handleCommand(String input) {
        String[] tokens = input.trim().split("\\s+");

        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return true;
        }

        String command = tokens[0].toLowerCase();

        if (state == State.LOGGED_OUT) {
            return handlePreloginCommand(command, tokens);
        } else if (state == State.PLAYING) {
            return handleGameplayCommand(command, tokens);
        } else {
            return handlePostloginCommand(command, tokens);
        }
    }

    private boolean handlePreloginCommand(String command, String[] tokens) {
        return switch (command) {
            case "help" -> {
                showPreloginHelp();
                yield true;
            }
            case "quit" -> false;

            case "login" -> {
                login(tokens);
                yield true;
            }
            case "register" -> {
                register(tokens);
                yield true;
            }

            default -> {
                System.out.println("Sorry, I didn't get that. Type 'help' to see available commands.");
                yield true;
            }
        };
    }

    private boolean handlePostloginCommand(String command, String[] tokens) {
        return switch (command) {
            case "help" -> {
                showPostloginHelp();
                yield true;
            }
            case "quit" -> false;
            case "logout" -> {
                logout();
                yield true;
            }
            case "create" -> {
                createGame(tokens);
                yield true;
            }
            case "list" -> {
                listGames();
                yield true;
            }
            case "play" -> {
                playGame(tokens);
                yield true;
            }
            case "observe" -> {
                observeGame(tokens);
                yield true;
            }
            default -> {
                System.out.println("Sorry, I didn't get that. Type 'help' to see available commands.");
                yield true;
            }
        };
    }

    private void showPreloginHelp() {
        System.out.println("""
            Commands:
              'register': <username> <password> <email> - Create an account
              'login': <username> <password> - Log into your existing chess account
              'quit': - Close the chess client
            """);
    }

    private void showPostloginHelp() {
        System.out.println("""
            Available commands:
              'list': - List all games
              'create': <game-name> - Create a new chess game
              'play': <game-number> <WHITE|BLACK> - Join a game as a player
              'observe': <game-number> - Watch a game
              'logout': - Log out of your account
              'quit' - Close the chess client
            """);
    }

    private void login(String[] tokens) {
        try {
            if (tokens.length != 3) {
                System.out.println("""
                Sorry, please try again. To login:
                'login' <username> <password>
                """);
                return;
            }

            String username = tokens[1];
            String password = tokens[2];

            AuthData authData = server.login(username, password);

            this.authToken = authData.authToken();
            this.username = authData.username();
            this.state = State.LOGGED_IN;

            System.out.println("Success! You are now logged in as " + username + ". Type 'Help' to see more commands.");

        } catch (ResponseException e) {
            if (e.getStatusCode() == 401) {
                System.out.println("Sorry, your username or password weren't quite right. Please try again.");
            } else {
                System.out.println("Error: Unable to login");
            }
        }
    }

    private void register(String[] tokens){
        try {
            if (tokens.length != 4) {
                System.out.println("""
                    Sorry, please try again. To register:
                    'register': <username> <password> <email>
                    """);
                return;
            }
            String username = tokens[1];
            String password = tokens[2];
            String email = tokens[3];

            AuthData authData = server.register(username, password, email);
            this.authToken = authData.authToken();
            this.username = authData.username();
            this.state = State.LOGGED_IN;

            System.out.println("Success! Account created: " +this.username+". You are now logged in." );

        } catch (ResponseException e) {
            if (e.getStatusCode() == 403) {
                System.out.println("Sorry, that username is taken. Please try again.");
            } else if (e.getStatusCode() == 400) {
                System.out.println("Sorry, your account info doesn't look quite right. Please try again.");
            } else {
                System.out.println("Sorry, something went wrong with your registration. Please try again.");
            }
        }
    }

    private void logout() {
        try {
            server.logout(this.authToken);
            this.state=State.LOGGED_OUT;
            this.authToken = null;
            this.username = null;
            System.out.println("Success! You have been logged out.");

        } catch (ResponseException e) {
            System.out.println("Sorry, we couldn't log you out of your account. Please try again.");
        }
    }

    private void createGame(String[] tokens) {
        try {
            if (tokens.length < 2) {
                System.out.println("""
                        Sorry, you need to provide a name for the game:
                        Please try again: create <game-name>""");
                return;
            }

            String[] gameNameWords = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, gameNameWords, 0, tokens.length - 1);
            String gameNameFinal = String.join(" ", gameNameWords);

            int gameID = server.createGame(this.authToken, gameNameFinal);

            System.out.println("Success: Game '" + gameNameFinal + "' created successfully!");

        } catch (ResponseException e) {
            System.out.println("Sorry, we couldn't create your game. Please try again.");
        }

    }

    private void listGames() {
        try {
            GameData[] games = server.listGames(this.authToken);
            gameNumberToId.clear();

            if (games.length == 0) {
                System.out.println("No games yet. Create one with the 'create' command.");
                return;
            }

            System.out.println("\nAvailable Games:");

            for (int i = 0; i < games.length; i++) {
                int displayNumber = i + 1;
                GameData game = games[i];
                gameNumberToId.put(displayNumber, game.gameID());

                String whitePlayer;
                if (game.whiteUsername() != null) {
                    whitePlayer = game.whiteUsername();
                } else {
                    whitePlayer = "(no white player)";
                }

                String blackPlayer;
                if (game.blackUsername() != null) {
                    blackPlayer = game.blackUsername();
                } else {
                    blackPlayer = "(no black player)";
                }

                System.out.printf("%d. %s", displayNumber, game.gameName());
                System.out.printf("| White: %s | Black: %s", whitePlayer, blackPlayer + "\n");
            }


        } catch (ResponseException e) {
            System.out.println("Sorry, an error occurred while listing games.");
        }
    }

    private void playGame(String[] tokens) {
        try {
            if (tokens.length != 3) {
                System.out.println("Sorry, that doesn't look right. To play a game: play <game-number> WHITE|BLACK");
                return;
            }

            int displayNumber;
            try {
                displayNumber = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Game number must be a number");
                return;
            }

            Integer gameID = gameNumberToId.get(displayNumber);
            if (gameID == null) {
                System.out.println("Error: Invalid game number. Use 'list' to see available games.");
                return;
            }

            String color = tokens[2].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Error: Color must be WHITE or BLACK");
                return;
            }

            server.joinGame(this.authToken, gameID, color);

            String wsUrl = serverUrl.replace("http", "ws") + "/ws";
            ws = new WebSocketClient(wsUrl, this);
            ws.connect(authToken, gameID);

            this.currentGameID = gameID;
            this.playerColor = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            this.state = State.PLAYING;

            System.out.println("Joined game as " + color + ". Type 'help' for commands.");

        } catch (ResponseException e) {
            if (e.getStatusCode() == 403) {
                System.out.println("Sorry, that color is already taken");
            } else if (e.getStatusCode() == 400) {
                System.out.println("Sorry, that game can't be joined right now.");
            } else {
                System.out.println("Error: Could not join game");
            }
        } catch (Exception e) {
            System.out.println("Error connecting to game: " + e.getMessage());
        }
    }

    private void observeGame(String[] tokens) {
        try {
            if (tokens.length != 2) {
                System.out.println("That didn't look quite right. To watch a game: 'observe' <game-number>");
                return;
            }

            int displayNumber = Integer.parseInt(tokens[1]);

            Integer gameID = gameNumberToId.get(displayNumber);
            if (gameID == null) {
                System.out.println("Error: Invalid game number. Use 'list' to see available games.");
                return;
            }

            String wsUrl = serverUrl.replace("http", "ws") + "/ws";
            ws = new WebSocketClient(wsUrl, this);
            ws.connect(authToken, gameID);

            this.currentGameID = gameID;
            this.playerColor = null;
            this.state = State.PLAYING;

            System.out.println("Observing game. Type 'help' for commands.");

        } catch (Exception e) {
            System.out.println("Error: Could not observe game - " + e.getMessage());
        }
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
            ws.leave(authToken, currentGameID);
            ws.close();
            ws = null;
            currentGame = null;
            currentGameID = 0;
            playerColor = null;
            state = State.LOGGED_IN;
            System.out.println("Left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving game: " + e.getMessage());
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
                ws.resign(authToken, currentGameID);
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        } else {
            System.out.println("Resignation cancelled.");
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
            ws.makeMove(authToken, currentGameID, move);

        } catch (Exception e) {
            System.out.println("Invalid move: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Position must be like 'e2'");
        }
        char colChar = Character.toLowerCase(pos.charAt(0));
        char rowChar = pos.charAt(1);

        int col = colChar - 'a' + 1;  // a=1, b=2, etc.
        int row = rowChar - '0';       // 1-8

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

    private void highlightMoves(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: highlight <position>");
            System.out.println("Example: highlight e2");
            return;
        }

        try {
            ChessPosition pos = parsePosition(tokens[1]);
            System.out.println("Highlight moves for " + tokens[1] + " - (not yet implemented)");
        } catch (Exception e) {
            System.out.println("Invalid position: " + e.getMessage());
        }
    }

}