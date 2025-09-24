package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;


    public ChessGame() {
        turn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> validMovesArray = new ArrayList<>();
        if(piece != null){
            Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
            for(ChessMove move : allMoves){
                ChessPiece endPiece = board.getPiece(move.getEndPosition());
                ChessPiece startPiece = board.getPiece(move.getStartPosition());

                board.addPiece(move.getEndPosition(), startPiece);
                board.addPiece(move.getStartPosition(), null);

                if(!isInCheck(piece.getTeamColor())){
                    validMovesArray.add(move);
                }

                board.addPiece(move.getStartPosition(), startPiece);
                board.addPiece(move.getEndPosition(), endPiece);
            }
        }
        else {
            return null;
        }

        return validMovesArray;

    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if(piece == null){
            throw new InvalidMoveException("No piece at move start position.");
        }
        if(piece.getTeamColor() != turn){
            throw new InvalidMoveException("This piece is trying to move out of turn");
        }
        Collection<ChessMove> validMove = validMoves(move.getStartPosition());
        if(!validMove.contains(move)){
            throw new InvalidMoveException("This move is not valid. Failed validMove validation.");
        }
        if(move.getPromotionPiece()==null) {

            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
        }
        else{
            ChessPiece promotedPawn = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promotedPawn);
            board.addPiece(move.getStartPosition(), null);
        }
        if(piece.getTeamColor() == TeamColor.WHITE){
            turn = TeamColor.BLACK;
        }
        else{
            turn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) throws IllegalArgumentException {
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++) { //get the current team's king position
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                        kingPosition = position;
                        break;
                    }
                }
            }
        }

        for (int row = 1; row <= 8; row++) { //iterate through all pieces on the board
            for (int col = 1; col <= 8; col++) {
                ChessPosition enemyPosition = new ChessPosition(row, col);
                ChessPiece enemyPiece = board.getPiece(enemyPosition);

                if (enemyPiece != null) {
                    if (enemyPiece.getTeamColor() != teamColor) { //if the current piece is an enemy
                        Collection<ChessMove> moves = enemyPiece.pieceMoves(board, enemyPosition);
                        for (ChessMove move : moves) { //iterate through every move of the current enemy piece
                            if (move.getEndPosition().equals(kingPosition)) {
                                return true; //if the end position is the King's position, the king is in check
                            }
                        }

                    }
                }
            }
        }
        return false;

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }

        for (int row = 1; row <= 8; row++) { //iterate through all pieces on the board
            for (int col = 1; col <= 8; col++) {
                ChessPosition friendPosition = new ChessPosition(row, col);
                ChessPiece friendlyPiece = board.getPiece(friendPosition);
                if (friendlyPiece != null && friendlyPiece.getTeamColor()==teamColor) {
                    Collection<ChessMove> escapeCheck = validMoves(friendPosition);

                    if(!escapeCheck.isEmpty()){
                        return false;
                    }
                }
            }
        }

        return true;

    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> staleMate = new ArrayList<>();
        if (isInCheck(teamColor)){
            return false;
        }

        for (int row = 1; row <= 8; row++) { //iterate through all pieces on the board
            for (int col = 1; col <= 8; col++) {
                ChessPosition friendPosition = new ChessPosition(row, col);
                ChessPiece friendlyPiece = board.getPiece(friendPosition);
                if (friendlyPiece != null && friendlyPiece.getTeamColor()==teamColor) {
                    Collection<ChessMove> moves = validMoves(friendPosition);
                    for(ChessMove move: moves){
                        staleMate.add(move);
                    }

                }
            }
        }
        if(staleMate.isEmpty()){
            return true;
        }

        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }
}
