package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        ChessPiece piece = board.getPiece(myPosition); //get the piece at the current position

         //Call the correct helper function to return the moves for the piece
        switch (piece.getPieceType()) {
            case BISHOP:
                return bishopMoves(board, myPosition);
            case KING:
                return kingMoves(board, myPosition);
            case KNIGHT:
                return knightMoves(board, myPosition);
            case PAWN:
                return pawnMoves(board, myPosition);
            case QUEEN:
                return queenMoves(board, myPosition);
            case ROOK:
                return rookMoves(board, myPosition);
            default:
                throw new IllegalArgumentException("Error: no such piece: " + piece.getPieceType());
        }
    }

    private Collection<ChessMove> singleMoves(ChessBoard board, ChessPosition myPosition, int[][] steps) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] step : steps) {
            int newRow = myPosition.getRow() + step[0];
            int newCol = myPosition.getColumn() + step[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> continuousMoves(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] direction : directions) {
            int rowStep = direction[0];
            int colStep = direction[1];

            int currentRow = myPosition.getRow() + rowStep;
            int currentCol = myPosition.getColumn() + colStep;

            while (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else if (pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    break;
                }

                currentRow += rowStep;
                currentCol += colStep;
            }
        }
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] rookDirections = {
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1}
        };
        return continuousMoves(board, myPosition, rookDirections);
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] queenDirections = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1},
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1}
        };
        return continuousMoves(board, myPosition, queenDirections);
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        if (this.getTeamColor() == ChessGame.TeamColor.BLACK) {
            int[] capturableColumns = {myPosition.getColumn()-1, myPosition.getColumn()+1};
            for (int column : capturableColumns) {
                if (column <=8 && column >=1) {
                    ChessPosition enemyPosition = new ChessPosition(myPosition.getRow() - 1, column);
                    ChessPiece enemyPiece = board.getPiece(enemyPosition);
                    if (enemyPiece != null && enemyPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        if (myPosition.getRow()-1 == 1){
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.QUEEN));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, enemyPosition, null));
                        }
                    }
                }
            }
            if (myPosition.getRow() == 7) {
                ChessPosition oneStep = new ChessPosition(myPosition.getRow()-1, myPosition.getColumn());
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow()-2, myPosition.getColumn());

                if (board.getPiece(oneStep) == null && board.getPiece(twoSteps) == null) {
                    moves.add(new ChessMove(myPosition, twoSteps, null));
                }
            }
            int newRow = myPosition.getRow() - 1;
            if (newRow >=1) {
                ChessPosition newPosition = new ChessPosition(newRow, myPosition.getColumn());
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                if (pieceAtNewPosition == null){
                    if (newRow == 1){
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                    }
                    else{
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }

                }
            }
        }
        else if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {
            int[] capturableColumns = {myPosition.getColumn()-1, myPosition.getColumn()+1};
            for (int column : capturableColumns) {
                if (column >=1 && column <=8) {
                    ChessPosition enemyPosition = new ChessPosition(myPosition.getRow() + 1, column);
                    ChessPiece enemyPiece = board.getPiece(enemyPosition);
                    if (enemyPiece != null && enemyPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        if (myPosition.getRow()+1 == 8){
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, enemyPosition, PieceType.BISHOP));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, enemyPosition, null));
                        }

                    }
                }
            }
            if (myPosition.getRow() == 2) {
                ChessPosition oneStep = new ChessPosition(myPosition.getRow()+1, myPosition.getColumn());
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow()+2, myPosition.getColumn());

                if (board.getPiece(oneStep) == null && board.getPiece(twoSteps) == null) {
                    moves.add(new ChessMove(myPosition, twoSteps, null));
                }
            }
            int newRow = myPosition.getRow() + 1;
            if (newRow <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, myPosition.getColumn());
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                if (pieceAtNewPosition == null){
                    if (newRow == 8){
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                    }
                    else{
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] knightSteps = {
                {2, 1},
                {2, -1},
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2},
                {-2, 1},
                {-2, -1}
        };
        return singleMoves(board, myPosition, knightSteps);
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] kingSteps = {
                {1, 0},
                {1, 1},
                {1, -1},
                {0, 1},
                {0, -1},
                {-1, 0},
                {-1, -1},
                {-1, 1}
        };
        return singleMoves(board, myPosition, kingSteps);
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] bishopDirections = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };
        return continuousMoves(board, myPosition, bishopDirections);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
