package ui;

import chess.ChessBoard;
import client.ServerFacade;
import client.ResponseException;
import model.AuthData;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import model.GameData;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner scanner;
    private String authToken = null;
    private String username = null;
    private State state = State.LOGGED_OUT;
    private Map<Integer, Integer> gameNumberToId = new HashMap<>();

    private enum State {
        LOGGED_OUT,
        LOGGED_IN
    }

    public ChessClient(String serverUrl) {
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

    private void printPrompt() {
        if (state == State.LOGGED_OUT) {
            System.out.print("\n[LOGGED_OUT] >>> ");
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
                System.out.println("Sorry, that doesn't look right. To play a game: <game-number> WHITE | BLACK");
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
            System.out.println("Successfully joined game as " + color);

            ChessBoard board = new ChessBoard();
            board.resetBoard();

            if (color.equals("BLACK")) {
                BoardRender.drawBlackBoard(board);
            } else {
                BoardRender.drawWhiteBoard(board);
            }

        } catch (ResponseException e) {
            if (e.getStatusCode() == 403) {
                System.out.println("Sorry, that color is already taken");
            } else if (e.getStatusCode() == 400) {
                System.out.println("Sorry, that game can't be joined right now.");
            } else {
                System.out.println("Error: Could not join game");
            }
        }
    }

    private void observeGame(String[] tokens) {
        System.out.println("Observe game not yet implemented");
    }
}