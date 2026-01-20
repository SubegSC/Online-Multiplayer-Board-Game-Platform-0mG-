package TicTacToe_Logic;

public class TicTacToeGame {

    private final TicTacToeBoard board;
    private TicTacToePlayer currentPlayer;
    private TicTacToeGameResult result;

    public TicTacToeGame() {
        this.board = new TicTacToeBoard();
        this.currentPlayer = TicTacToePlayer.X; // x start
        this.result = TicTacToeGameResult.IN_PROGRESS;
    }

    public TicTacToeBoard getBoard() {
        return board;
    }

    public TicTacToePlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public TicTacToeGameResult getResult() {
        return result;
    }

    public void reset() {
        board.reset();
        currentPlayer = TicTacToePlayer.X;
        result = TicTacToeGameResult.IN_PROGRESS;
    }

    public boolean playMove(int x, int y) {
        if (result != TicTacToeGameResult.IN_PROGRESS) {
            return false; // game done
        }

        TicTacToeMove move = new TicTacToeMove(currentPlayer, x, y);
        if (!board.takeMove(move)) {
            return false; // illegal move
        }

        updateResult(); // after move

        if (result == TicTacToeGameResult.IN_PROGRESS) {
            switchPlayer();
        }
        return true;
    }

    private void switchPlayer() {
        currentPlayer = currentPlayer.nextTurn();
    }

    private void updateResult() {
        TicTacToePlayer winner = getWinner();

        if (winner == TicTacToePlayer.X) {
            result = TicTacToeGameResult.X_WINS;
            return;
        }

        if (winner == TicTacToePlayer.O) {
            result = TicTacToeGameResult.O_WINS;
            return;
        }

        // no winner
        if (board.isFull()) {
            result = TicTacToeGameResult.DRAW;
        } else {
            result = TicTacToeGameResult.IN_PROGRESS;
        }
    }

    private TicTacToePlayer getWinner() {
        int height = board.getHeight();
        int width = board.getWidth();

        // cols
        for (int col = 0; col < width; col++) {
            TicTacToePlayer player = board.getPlayerAt(0, col);
            if (player != null &&
                    player == board.getPlayerAt(1, col) &&
                    player == board.getPlayerAt(2, col)) {
                return player;
            }
        }

        // rows
        for (int row = 0; row < height; row++) {
            TicTacToePlayer player = board.getPlayerAt(row, 0);
            if (player != null &&
                    player == board.getPlayerAt(row, 1) &&
                    player == board.getPlayerAt(row, 2)) {
                return player;
            }
        }

        // diagonals
        TicTacToePlayer player = board.getPlayerAt(0, 0);
        if (player != null &&
                player == board.getPlayerAt(1, 1) &&
                player == board.getPlayerAt(2, 2)) {
            return player;
        }

        player = board.getPlayerAt(0, 2);
        if (player != null &&
                player == board.getPlayerAt(1, 1) &&
                player == board.getPlayerAt(2, 0)) {
            return player;
        }

        return null;
    }
}
