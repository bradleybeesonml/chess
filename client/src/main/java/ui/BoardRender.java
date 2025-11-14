package ui;

import chess.ChessBoard;
import static ui.EscapeSequences.*;

public class BoardRender {
    
    public static void drawWhiteBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);
        drawHeaderRow(true);
        for (int row = 8; row >= 1; row--) {
            drawRow(board, row, true);
        }
        drawHeaderRow(true);

    }

    public static void drawBlackBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);
        drawHeaderRow(false);
        for (int row = 1; row <= 8; row++) {
            drawRow(board, row, false);
        }
        drawHeaderRow(false);
    }


    private static void drawHeaderRow(boolean isWhite){
        System.out.print(EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);

        if(isWhite){
            System.out.print("   ");
            for (char columnLetter = 'a'; columnLetter <= 'h'; columnLetter++) {
                System.out.print(" " + columnLetter + " ");
            }
            System.out.print("   ");
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.println("");
        }

        else {
            System.out.print("   ");
            for (char columnLetter = 'h'; columnLetter >= 'a'; columnLetter--) {
                System.out.print(" " + columnLetter + " ");
            }
            System.out.print("   ");
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.println("");

        }
    }

    private static String getPieceSymbol(chess.ChessPiece piece) {
        if (piece.getTeamColor() == chess.ChessGame.TeamColor.WHITE) {
            return switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
        } else {
            return switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        }
    }

    private static void drawRow(ChessBoard board, int row, boolean isWhite) {
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");

        for (int col = 1; col <= 8; col++) {
            int actualCol = isWhite ? col : (9 - col);

            boolean isLightSquare = (row + actualCol) % 2 == 0;

            if (isLightSquare) {
                System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            } else {
                System.out.print(EscapeSequences.SET_BG_COLOR_BLUE);
            }

            chess.ChessPosition position = new chess.ChessPosition(row, actualCol);
            chess.ChessPiece piece = board.getPiece(position);

            if (piece == null) {
                System.out.print(EscapeSequences.EMPTY);
            } else {
                System.out.print(getPieceSymbol(piece));
            }
        }

        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        System.out.print(" " + row + " ");

        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }



}
