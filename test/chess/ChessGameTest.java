package chess;

import org.junit.Test;

import chess_logic.ChessGame;
import chess_logic.ChessMove;
import chess_logic.Color;
import chess_logic.GameStatus;
import chess_logic.Piece;
import chess_logic.PieceType;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration-style tests through the ChessGame API.
 */
public class ChessGameTest {

    @Test
    public void initialSetupHasKingsAndQueens() {
        ChessGame game = new ChessGame();
        Piece[][] board = game.getBoardSnapshot();

        // White pieces
        Piece whiteKing = board[4][0]; // e1
        Piece whiteQueen = board[3][0]; // d1

        assertNotNull("White king should be on e1", whiteKing);
        assertEquals(PieceType.KING, whiteKing.getType());
        assertEquals(Color.WHITE, whiteKing.getColor());

        assertNotNull("White queen should be on d1", whiteQueen);
        assertEquals(PieceType.QUEEN, whiteQueen.getType());
        assertEquals(Color.WHITE, whiteQueen.getColor());

        // Black pieces
        Piece blackKing = board[4][7]; // e8
        Piece blackQueen = board[3][7]; // d8

        assertNotNull("Black king should be on e8", blackKing);
        assertEquals(PieceType.KING, blackKing.getType());
        assertEquals(Color.BLACK, blackKing.getColor());

        assertNotNull("Black queen should be on d8", blackQueen);
        assertEquals(PieceType.QUEEN, blackQueen.getType());
        assertEquals(Color.BLACK, blackQueen.getColor());

        // Side to move should be white, status running
        assertEquals(Color.WHITE, game.getSideToMove());
        assertEquals(GameStatus.RUNNING, game.getStatus());
    }

    @Test
    public void whitePawnHasOneAndTwoStepFromStart() {
        ChessGame game = new ChessGame();
        // e2 pawn: file=4 (a=0), rank=1 (2nd rank)
        List<ChessMove> moves = game.getLegalMovesFrom(4, 1);

        boolean oneStep = false;
        boolean twoStep = false;
        for (ChessMove m : moves) {
            if (m.getToFile() == 4 && m.getToRank() == 2) oneStep = true;  // e3
            if (m.getToFile() == 4 && m.getToRank() == 3) twoStep = true;  // e4
        }

        assertTrue("Pawn on e2 should be able to move to e3", oneStep);
        assertTrue("Pawn on e2 should be able to move to e4 from start", twoStep);
    }

    @Test
    public void illegalMoveIsRejected() {
        ChessGame game = new ChessGame();
        Piece[][] before = game.getBoardSnapshot();

        // Try moving white queen from d1 to d4 at the start, which is illegal (blocked by pawn)
        boolean ok = game.makeMove(3, 0, 3, 3); // d1 -> d4

        assertFalse("Blocked queen move should be illegal", ok);

        Piece[][] after = game.getBoardSnapshot();

        // Ensure queen is still on d1 and pawn still on d2
        assertNotNull(after[3][0]);
        assertEquals(PieceType.QUEEN, after[3][0].getType());
        assertNotNull(after[3][1]);
        assertEquals(PieceType.PAWN, after[3][1].getType());

        // Board should be unchanged on those squares
        assertSame(before[3][0], after[3][0]);
        assertSame(before[3][1], after[3][1]);
    }

    @Test
    public void foolsMateIsCheckmate() {
        ChessGame game = new ChessGame();

        // 1. f2-f3
        assertTrue(game.makeMove(5, 1, 5, 2));
        // 1... e7-e5
        assertTrue(game.makeMove(4, 6, 4, 4));
        // 2. g2-g4
        assertTrue(game.makeMove(6, 1, 6, 3));
        // 2... Qd8-h4#
        assertTrue(game.makeMove(3, 7, 7, 3));

        assertEquals("Game should be checkmate after Fool's Mate",
                GameStatus.CHECKMATE, game.getStatus());
        assertEquals("After black checkmates, it should be white's turn",
                Color.WHITE, game.getSideToMove());
    }
}
