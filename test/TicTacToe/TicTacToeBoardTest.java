package TicTacToe;

import org.junit.Test;

import TicTacToe_Logic.TicTacToeBoard;
import TicTacToe_Logic.TicTacToeMove;
import TicTacToe_Logic.TicTacToePlayer;

import static org.junit.Assert.*;

public class TicTacToeBoardTest {

    @Test
    public void newBoard_isEmptyAndNotFull() {
        TicTacToeBoard board = new TicTacToeBoard();

        // every cell should be empty
        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                assertNull(board.getGridCell(r, c));
                assertNull(board.getPlayerAt(r, c));
            }
        }
        assertFalse(board.isFull());
    }

    @Test
    public void takeMove_placesMoveOnBoard() {
        TicTacToeBoard board = new TicTacToeBoard();
        TicTacToeMove move = new TicTacToeMove(TicTacToePlayer.X, 1, 1);

        assertTrue(board.takeMove(move));
        // now that cell should contain the move / player
        assertEquals(move, board.getGridCell(1, 1));
        assertEquals(TicTacToePlayer.X, board.getPlayerAt(1, 1));
        // that move should no longer be valid
        assertFalse(board.isValidMove(move));
    }

    @Test
    public void isValidMove_falseWhenOutOfBoundsOrOccupied() {
        TicTacToeBoard board = new TicTacToeBoard();

        // out of bounds
        TicTacToeMove m1 = new TicTacToeMove(TicTacToePlayer.X, -1, 0);
        TicTacToeMove m2 = new TicTacToeMove(TicTacToePlayer.X, 3, 0);
        TicTacToeMove m3 = new TicTacToeMove(TicTacToePlayer.X, 0, 3);

        assertFalse(board.isValidMove(m1));
        assertFalse(board.isValidMove(m2));
        assertFalse(board.isValidMove(m3));

        // occupied
        TicTacToeMove first = new TicTacToeMove(TicTacToePlayer.X, 0, 0);
        assertTrue(board.takeMove(first));
        TicTacToeMove second = new TicTacToeMove(TicTacToePlayer.O, 0, 0);
        assertFalse(board.isValidMove(second));
    }

    @Test
    public void isFull_trueWhenAllCellsFilled() {
        TicTacToeBoard board = new TicTacToeBoard();

        // fill every cell
        TicTacToePlayer current = TicTacToePlayer.X;
        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                board.takeMove(new TicTacToeMove(current, c, r));
                current = current.nextTurn();
            }
        }

        assertTrue(board.isFull());
    }

    @Test
    public void reset_clearsBoard() {
        TicTacToeBoard board = new TicTacToeBoard();
        board.takeMove(new TicTacToeMove(TicTacToePlayer.X, 0, 0));
        board.takeMove(new TicTacToeMove(TicTacToePlayer.O, 1, 1));

        board.reset();

        for (int r = 0; r < board.getHeight(); r++) {
            for (int c = 0; c < board.getWidth(); c++) {
                assertNull(board.getGridCell(r, c));
            }
        }
        assertFalse(board.isFull());
    }
}
