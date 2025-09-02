package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP){
            return bishopMoves(board, myPosition);
        }

        else if (piece.getPieceType() == PieceType.KING){
            return kingMoves(board, myPosition);
        }

        else if (piece.getPieceType() == PieceType.KNIGHT){
            return knightMoves(board, myPosition);
        }

        else if (piece.getPieceType() == PieceType.PAWN){
            return pawnMoves(board, myPosition);
        }

        return List.of();
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        if (this.getTeamColor() == ChessGame.TeamColor.BLACK) {
            if (myPosition.getRow() == 7) { //initial move (moves by 2)
                ChessPosition newPosition = new ChessPosition(myPosition.getRow()-2, myPosition.getColumn());
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            int newRow = myPosition.getRow() - 1;
            if (newRow <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, myPosition.getColumn());
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                if (pieceAtNewPosition == null){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        else if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 2) { //initial move (moves by 2)
                ChessPosition newPosition = new ChessPosition(myPosition.getRow()+2, myPosition.getColumn());
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            int newRow = myPosition.getRow() + 1;
            if (newRow <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, myPosition.getColumn());
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                if (pieceAtNewPosition == null){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] knightsteps = { //all possible movements of Knight
                {2, 1},
                {2, -1},
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2},
                {-2, 1},
                {-2, -1}
        };
        for (int[] knightstep : knightsteps) {
            int newRow = myPosition.getRow() + knightstep[0];
            int newCol = myPosition.getColumn() + knightstep[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
                else if (pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] kingsteps = { //all possible movements of King
                {1, 0},
                {1, 1},
                {1, -1},
                {0, 1},
                {0, -1},
                {-1, 0},
                {-1, -1},
                {-1, 1}
        };
        for (int[] step : kingsteps) {
            int newRow =  myPosition.getRow() + step[0];
            int newCol =  myPosition.getColumn() + step[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
                else if (pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            else {
                break;
            }
        }
        return moves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] possibledirections = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };
    
        for (int[] direction : possibledirections) {
            int rowStep = direction[0];
            int colStep = direction[1];
    
            int currentRow = myPosition.getRow() + rowStep;
            int currentCol = myPosition.getColumn() + colStep;
    
            while (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }

                else if (pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
                else {
                    break;
                }

    
                currentRow += rowStep;
                currentCol += colStep;
            }
        }
    
        return moves;
    }
}
