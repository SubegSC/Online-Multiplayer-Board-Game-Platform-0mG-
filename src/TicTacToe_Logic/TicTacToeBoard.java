package TicTacToe_Logic;

public class TicTacToeBoard {
    private final int width = 3;
    private final int height = 3;

    private final TicTacToeMove[][] board;

    public TicTacToeBoard() {
        this.board = new TicTacToeMove[width][height];
    }

    /**
     * to clear board
     */
    public void reset() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                board[row][col] = null;
            }
        }
    }

    /**
     * validating if row and col are in boundaries on board and validity of move
     */
    private boolean inBound(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    public boolean isValidMove(TicTacToeMove move) {
        int row = move.getY();
        int col = move.getX();
        return inBound(row, col) && board[row][col] == null;
    }

    public boolean takeMove(TicTacToeMove move) {
        int row = move.getY();
        int col = move.getX();
        if (!isValidMove(move)) {
            return false;
        }
        board[row][col] = move;
        return true;
    }

    /**
     * helper functions
     */
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFull() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (board[row][col] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public TicTacToePlayer getPlayerAt(int row, int col) {
        TicTacToeMove move = board[row][col];
        if (move == null) {
            return null;
        } else {
            return move.getPlayer();
        }
    }

    public TicTacToeMove getGridCell(int row, int col) {
        if (!inBound(row, col)) {
            return null;
        }
        return board[row][col];
    }
}