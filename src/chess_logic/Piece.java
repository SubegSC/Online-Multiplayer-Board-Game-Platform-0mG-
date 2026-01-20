package chess_logic;

public final class Piece {
    private final PieceType type;
    private final Color color;
    private final boolean hasMoved;

    public Piece(PieceType type, Color color, boolean hasMoved) {
        this.type = type;
        this.color = color;
        this.hasMoved = hasMoved;
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public Piece withMoved() {
        if (hasMoved) return this;
        return new Piece(type, color, true);
    }

    @Override
    public String toString() {
        String c = color == Color.WHITE ? "w" : "b";
        switch (type) {
            case KING:   return c + "K";
            case QUEEN:  return c + "Q";
            case ROOK:   return c + "R";
            case BISHOP: return c + "B";
            case KNIGHT: return c + "N";
            case PAWN:   return c + "P";
            default:     return c + "?";
        }
    }
}
