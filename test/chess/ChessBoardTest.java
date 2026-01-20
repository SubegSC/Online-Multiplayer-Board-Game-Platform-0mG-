package chess;

import org.junit.Test;

import chess_logic.ChessBoard;
import chess_logic.ChessMove;
import chess_logic.Color;
import chess_logic.Piece;
import chess_logic.PieceType;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Lower-level tests that talk directly to ChessBoard.
 */
public class ChessBoardTest {

    @Test
    public void kingSideAndQueenSideCastlingMovesGeneratedWhenAllowed() {
        ChessBoard board = new ChessBoard();

        // Clear board completely
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                board.setPiece(f, r, null);
            }
        }

        // Put white king and rooks in starting squares, untouched
        board.setPiece(4, 0, new Piece(PieceType.KING, Color.WHITE, false));  // e1
        board.setPiece(0, 0, new Piece(PieceType.ROOK, Color.WHITE, false));  // a1
        board.setPiece(7, 0, new Piece(PieceType.ROOK, Color.WHITE, false));  // h1

        List<ChessMove> moves = board.generateLegalMoves(Color.WHITE);

        boolean hasKingSideCastle = false;
        boolean hasQueenSideCastle = false;

        for (ChessMove m : moves) {
            if (m.isCastleKingSide()) hasKingSideCastle = true;
            if (m.isCastleQueenSide()) hasQueenSideCastle = true;
        }

        assertTrue("Should allow king-side castling when path is clear and pieces unmoved",
                hasKingSideCastle);
        assertTrue("Should allow queen-side castling when path is clear and pieces unmoved",
                hasQueenSideCastle);
    }

    @Test
    public void pawnPromotionCreatesQueen() {
        ChessBoard board = new ChessBoard();

        // Clear board
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                board.setPiece(f, r, null);
            }
        }

        // White pawn on e7 (ready to promote to e8)
        board.setPiece(4, 6, new Piece(PieceType.PAWN, Color.WHITE, false));

        List<ChessMove> moves = board.generateLegalMoves(Color.WHITE);
        ChessMove promotionMove = null;

        for (ChessMove m : moves) {
            if (m.isPromotion()) {
                promotionMove = m;
                break;
            }
        }

        assertNotNull("There should be a promotion move for pawn on e7", promotionMove);

        // Apply the promotion move
        board.applyMoveNoValidation(promotionMove, Color.WHITE);

        Piece promoted = board.getPiece(promotionMove.getToFile(), promotionMove.getToRank());
        assertNotNull(promoted);
        assertEquals("Promotion should produce a queen", PieceType.QUEEN, promoted.getType());
        assertEquals(Color.WHITE, promoted.getColor());
    }

    @Test
    public void kingInCheckIsDetected() {
        ChessBoard board = new ChessBoard();

        // Clear board
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                board.setPiece(f, r, null);
            }
        }

        // Simple check: black rook on e8 checking white king on e1
        board.setPiece(4, 0, new Piece(PieceType.KING, Color.WHITE, false));  // white king e1
        board.setPiece(4, 7, new Piece(PieceType.ROOK, Color.BLACK, false));  // black rook e8

        assertTrue("White king on e1 should be in check from black rook on e8",
                board.isKingInCheck(Color.WHITE));
        assertFalse("Black king is not on the board, but should not be considered in check",
                board.isKingInCheck(Color.BLACK));
    }
}
