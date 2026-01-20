package go_logic;
public enum StoneColor {
    BLACK,
    WHITE;

    public StoneColor opposite() {
        return this == BLACK ? WHITE : BLACK;
    }
}
