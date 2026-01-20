package chess_logic;

public final class ChessMove {
    private final int fromFile; // 0..7 (a..h)
    private final int fromRank; // 0..7 (white side at rank 0)
    private final int toFile;
    private final int toRank;
    private final boolean isCastleKingSide;
    private final boolean isCastleQueenSide;
    private final boolean isPromotion;

    public ChessMove(int fromFile, int fromRank, int toFile, int toRank) {
        this(fromFile, fromRank, toFile, toRank, false, false, false);
    }

    public ChessMove(int fromFile, int fromRank, int toFile, int toRank,
                     boolean castleKingSide, boolean castleQueenSide,
                     boolean promotion) {
        this.fromFile = fromFile;
        this.fromRank = fromRank;
        this.toFile = toFile;
        this.toRank = toRank;
        this.isCastleKingSide = castleKingSide;
        this.isCastleQueenSide = castleQueenSide;
        this.isPromotion = promotion;
    }

    public int getFromFile() { return fromFile; }
    public int getFromRank() { return fromRank; }
    public int getToFile()   { return toFile; }
    public int getToRank()   { return toRank; }

    public boolean isCastleKingSide() { return isCastleKingSide; }
    public boolean isCastleQueenSide() { return isCastleQueenSide; }
    public boolean isPromotion() { return isPromotion; }
}
