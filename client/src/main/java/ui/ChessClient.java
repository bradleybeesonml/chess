package ui;

import chess.ChessBoard;
import client.ServerFacade;
import client.ResponseException;
import model.AuthData;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner scanner;
    private String authToken = null;
    private String username = null;
    private State state = State.LOGGED_OUT;

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

            System.out.println("Success! You are now logged in as " + username);

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
        System.out.println("Create game not yet implemented");
    }

    private void listGames() {
        System.out.println("List games not yet implemented");
    }

    private void playGame(String[] tokens) {
        System.out.println("Play game not yet implemented");
    }

    private void observeGame(String[] tokens) {
        System.out.println("Observe game not yet implemented");
    }
}