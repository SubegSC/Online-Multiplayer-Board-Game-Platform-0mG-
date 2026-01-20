package TicTacToe_Logic;

public enum TicTacToePlayer {
    X, O;

    /** for cleaner code in switchPlayer function */
    public TicTacToePlayer nextTurn() {
        if (this == TicTacToePlayer.X) {
            return TicTacToePlayer.O;
        } else {
            return TicTacToePlayer.X;
        }
    }
}
