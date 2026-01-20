package TicTacToe_Logic;

public class TicTacToeMove {
    private final int x; // column
    private final int y; // row
    private final TicTacToePlayer player; // x or o

    public TicTacToeMove(TicTacToePlayer player, int x, int y) {
        this.player = player;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TicTacToePlayer getPlayer() {
        return player;
    }
}
