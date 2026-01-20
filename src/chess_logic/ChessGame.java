package chess_logic;

import java.util.ArrayList;
import java.util.List;

public final class ChessGame {
    private final ChessBoard board;
    private Color sideToMove;
    private GameStatus status;

    public ChessGame() {
        this.board = new ChessBoard();
        this.sideToMove = Color.WHITE;
        this.status = GameStatus.RUNNING;
    }

    public Color getSideToMove() {
        return sideToMove;
    }

    public GameStatus getStatus() {
        return status;
    }

    /** For GUI: snapshot of the board (do not modify the returned array). */
    public Piece[][] getBoardSnapshot() {
        return board.copySquares();
    }

    /** For GUI: get all legal moves starting from a square. */
    public List<ChessMove> getLegalMovesFrom(int file, int rank) {
        List<ChessMove> all = board.generateLegalMoves(sideToMove);
        List<ChessMove> result = new ArrayList<>();
        for (ChessMove m : all) {
            if (m.getFromFile() == file && m.getFromRank() == rank) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Main entry for the GUI.
     * Returns true if the move was legal and applied, false otherwise.
     */
    public boolean makeMove(int fromFile, int fromRank, int toFile, int toRank) {
        if (status != GameStatus.RUNNING) return false;

        List<ChessMove> legal = board.generateLegalMoves(sideToMove);
        ChessMove chosen = null;
        for (ChessMove m : legal) {
            if (m.getFromFile() == fromFile &&
                m.getFromRank() == fromRank &&
                m.getToFile()   == toFile &&
                m.getToRank()   == toRank) {
                chosen = m;
                break;
            }
        }
        if (chosen == null) {
            return false; // illegal
        }

        board.applyMoveNoValidation(chosen, sideToMove);

        // update game state
        sideToMove = sideToMove.opposite();
        updateStatus();
        return true;
    }

    private void updateStatus() {
        if (board.generateLegalMoves(sideToMove).isEmpty()) {
            if (board.isKingInCheck(sideToMove)) {
                status = GameStatus.CHECKMATE;
            } else {
                status = GameStatus.STALEMATE;
            }
        } else {
            status = GameStatus.RUNNING;
        }
    }
}
