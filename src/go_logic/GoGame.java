package go_logic;
import java.util.EnumMap;
import java.util.Map;

/**
 * High-level Go game: tracks whose turn it is, captures, and simple end-of-game.
 */
public class GoGame {

    private final GoBoard board;
    private StoneColor currentPlayer = StoneColor.BLACK;
    private int consecutivePasses = 0;
    private final Map<StoneColor, Integer> capturedByPlayer = new EnumMap<>(StoneColor.class);

    // NEW: track resignation loser so we can auto-pick a winner
    private StoneColor resignedLoser = null;

    public GoGame(int size) {
        this.board = new GoBoard(size);
        capturedByPlayer.put(StoneColor.BLACK, 0);
        capturedByPlayer.put(StoneColor.WHITE, 0);
    }

    public GoBoard getBoard() {
        return board;
    }

    public StoneColor getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return consecutivePasses >= 2 || resignedLoser != null;
    }

    public int getCapturedStones(StoneColor player) {
        return capturedByPlayer.get(player);
    }

    public void playMove(int row, int col) throws IllegalMoveException {
        if (isGameOver()) {
            throw new IllegalMoveException("Game already over");
        }

        MoveResult result = board.playStone(row, col, currentPlayer);
        int prev = capturedByPlayer.get(currentPlayer);
        capturedByPlayer.put(currentPlayer, prev + result.getCaptureCount());

        consecutivePasses = 0;
        currentPlayer = currentPlayer.opposite();
    }

    public void pass() {
        if (isGameOver()) return;
        consecutivePasses++;
        currentPlayer = currentPlayer.opposite();
    }

    public void resign() {
        if (isGameOver()) return;
        // current side resigns; opponent wins
        resignedLoser = currentPlayer;
        // force game over
        consecutivePasses = 2;
    }

    /* ------------------------
       AUTO WINNER/SCORING API
       ------------------------ */

    /** Return winner color if determinable (resign or higher captures), else null for tie/unknown. */
    public StoneColor getWinnerColor() {
        // Resignation has priority
        if (resignedLoser != null) {
            return resignedLoser.opposite();
        }
        // If ended by two passes, approximate using captures as score proxy
        if (consecutivePasses >= 2) {
            int b = getBlackScore();
            int w = getWhiteScore();
            if (b > w) return StoneColor.BLACK;
            if (w > b) return StoneColor.WHITE;
            return null; // tie/unknown
        }
        return null; // still running / unknown
    }

    /** Simple score proxy: captured stones by BLACK. */
    public int getBlackScore() {
        Integer v = capturedByPlayer.get(StoneColor.BLACK);
        return v == null ? 0 : v;
    }

    /** Simple score proxy: captured stones by WHITE. */
    public int getWhiteScore() {
        Integer v = capturedByPlayer.get(StoneColor.WHITE);
        return v == null ? 0 : v;
    }
}
