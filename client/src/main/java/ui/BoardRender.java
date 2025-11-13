package ui;

import chess.ChessBoard;
import static ui.EscapeSequences.*;

public class BoardRender {
    
    public static void drawWhiteBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);
        drawHeaderRow(true);
    }

    public static void drawBlackBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);
        drawHeaderRow(false);
    }


    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println("White:");
        drawWhiteBoard(board);

        System.out.print(RESET_BG_COLOR);
        System.out.print(RESET_TEXT_COLOR);
        System.out.println("\nBlack:");
        drawBlackBoard(board);

    }

    private static void drawHeaderRow(boolean isWhite){
        System.out.print(EscapeSequences.SET_TEXT_COLOR_DARK_GREY);
        System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);

        if(isWhite){
            for (char columnLetter = 'a'; columnLetter <= 'h'; columnLetter++) {
                System.out.print(" " + columnLetter + " ");
            }
        }

        else {
            for (char columnLetter = 'h'; columnLetter >= 'a'; columnLetter--) {
                System.out.print(" " + columnLetter + " ");
            }
        }
    }



}
