package TicTacToe;

import org.junit.Test;

import TicTacToe_Logic.TicTacToeGame;
import TicTacToe_Logic.TicTacToeGameResult;
import TicTacToe_Logic.TicTacToePlayer;

import static org.junit.Assert.*;

public class TicTacToeGameTest {

    @Test
    public void newGame_initialStateCorrect() {
        TicTacToeGame game = new TicTacToeGame();

        assertEquals(TicTacToePlayer.X, game.getCurrentPlayer());
        assertEquals(TicTacToeGameResult.IN_PROGRESS, game.getResult());
        assertFalse(game.getBoard().isFull());
    }

    @Test
    public void playMove_updatesBoardAndSwitchesPlayer() {
        TicTacToeGame game = new TicTacToeGame();

        // X plays at (0,0)
        boolean ok = game.playMove(0, 0);
        assertTrue(ok);
        assertEquals(TicTacToePlayer.X,
                game.getBoard().getPlayerAt(0, 0));

        // after a valid move, player should switch to O
        assertEquals(TicTacToePlayer.O, game.getCurrentPlayer());
        assertEquals(TicTacToeGameResult.IN_PROGRESS, game.getResult());
    }

    @Test
    public void game_detectsRowWinForX() {
        TicTacToeGame game = new TicTacToeGame();

        // X: (0,0), O: (0,1), X: (1,0), O: (1,1), X: (2,0) -> X wins on first column
        assertTrue(game.playMove(0, 0)); // X
        assertTrue(game.playMove(1, 0)); // O
        assertTrue(game.playMove(0, 1)); // X
        assertTrue(game.playMove(1, 1)); // O
        assertTrue(game.playMove(0, 2)); // X wins

        assertEquals(TicTacToeGameResult.X_WINS, game.getResult());
        // further moves should be rejected
        assertFalse(game.playMove(2, 2));
    }

    @Test
    public void game_detectsDraw() {
        TicTacToeGame game = new TicTacToeGame();

        game.playMove(0, 0); // X
        game.playMove(2, 0); // O
        game.playMove(1, 0); // X
        game.playMove(0, 1); // O
        game.playMove(2, 1); // X
        game.playMove(1, 1); // O
        game.playMove(0, 2); // X
        game.playMove(1, 2); // O
        game.playMove(2, 2); // X

        assertEquals(TicTacToeGameResult.DRAW, game.getResult());
        assertTrue(game.getBoard().isFull());
    }


    @Test
    public void reset_returnsGameToInitialState() {
        TicTacToeGame game = new TicTacToeGame();
        game.playMove(0, 0);
        game.playMove(1, 1);

        game.reset();

        assertEquals(TicTacToePlayer.X, game.getCurrentPlayer());
        assertEquals(TicTacToeGameResult.IN_PROGRESS, game.getResult());
        assertFalse(game.getBoard().isFull());
        for (int r = 0; r < game.getBoard().getHeight(); r++) {
            for (int c = 0; c < game.getBoard().getWidth(); c++) {
                assertNull(game.getBoard().getGridCell(r, c));
            }
        }
    }
}
