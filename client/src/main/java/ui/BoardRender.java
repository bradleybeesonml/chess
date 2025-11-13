package ui;

import chess.ChessBoard;
import static ui.EscapeSequences.*;

public class BoardRender {
    
    public static void drawWhiteBoard(ChessBoard board) {
        System.out.print(ERASE_SCREEN);
        System.out.println("Drawing board for white player");
    }


    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println("Testing from White Players' perspective");
        drawWhiteBoard(board);

    }

    private static void drawHeaderRow(boolean isWhite){
        
    }



}
