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
        return List.of();
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] possibledirections = {
                {1, 1},   // up one, over one
                {1, -1},  // up one, to the left one
                {-1, 1},  // down one, over one
                {-1, -1}  // down one, to the left one
        };
    
        for (int[] direction : possibledirections) {
            int rowStep = direction[0];
            int colStep = direction[1];
    
            int currentRow = myPosition.getRow() + rowStep;
            int currentCol = myPosition.getColumn() + colStep;
    
            while (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                moves.add(new ChessMove(myPosition, newPosition, null));
    
                currentRow += rowStep;
                currentCol += colStep;
            }
        }
    
        return moves;
    }
}
