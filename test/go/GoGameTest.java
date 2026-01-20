package go;
import org.junit.Test;

import go_logic.GoBoard;
import go_logic.GoGame;
import go_logic.IllegalMoveException;
import go_logic.StoneColor;

import static org.junit.Assert.*;

public class GoGameTest {

    @Test
    public void placeStoneOnEmptyBoard() throws IllegalMoveException {
        GoGame game = new GoGame(9);

        game.playMove(4, 4); // Black
        assertEquals(StoneColor.WHITE, game.getCurrentPlayer());
        assertEquals(StoneColor.BLACK, game.getBoard().getStone(4, 4));
    }

    @Test
    public void suicideMoveNotAllowed() throws IllegalMoveException {
        GoGame game = new GoGame(5);
        GoBoard b = game.getBoard();

        // Create a white "eye" at (1,1) completely surrounded by black.
        game.playMove(0, 1); // B
        game.playMove(4, 4); // W
        game.playMove(1, 0); // B
        game.playMove(4, 3); // W
        game.playMove(1, 2); // B
        game.playMove(3, 4); // W
        game.playMove(2, 1); // B

        try {
            game.playMove(1, 1);
            fail("Expected IllegalMoveException for suicide move");
        } catch (IllegalMoveException ex) {
            assertTrue(ex.getMessage().contains("Suicide"));
        }
        assertTrue(b.isEmpty(1, 1));
    }

    @Test
    public void twoPassesEndGame() throws IllegalMoveException {
        GoGame game = new GoGame(9);

        assertFalse(game.isGameOver());
        game.pass(); // B passes
        assertFalse(game.isGameOver());
        game.pass(); // W passes
        assertTrue(game.isGameOver());

        try {
            game.playMove(0, 0);
            fail("Expected IllegalMoveException after game ended");
        } catch (IllegalMoveException ex) {
            // Expected
        }
    }
}
