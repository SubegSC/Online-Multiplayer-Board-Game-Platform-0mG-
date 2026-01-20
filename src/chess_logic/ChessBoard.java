package chess_logic;

import java.util.ArrayList;
import java.util.List;

public final class ChessBoard implements Cloneable {
    private final Piece[][] squares; // [file][rank]
    private boolean whiteCastleKingSide = true;
    private boolean whiteCastleQueenSide = true;
    private boolean blackCastleKingSide = true;
    private boolean blackCastleQueenSide = true;

    public ChessBoard() {
        squares = new Piece[8][8];
        setupInitial();
    }

    private ChessBoard(Piece[][] squares,
                       boolean wCK, boolean wCQ,
                       boolean bCK, boolean bCQ) {
        this.squares = squares;
        this.whiteCastleKingSide = wCK;
        this.whiteCastleQueenSide = wCQ;
        this.blackCastleKingSide = bCK;
        this.blackCastleQueenSide = bCQ;
    }

    public void setupInitial() {
        // clear
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                squares[f][r] = null;
            }
        }
        // pawns
        for (int f = 0; f < 8; f++) {
            squares[f][1] = new Piece(PieceType.PAWN, Color.WHITE, false);
            squares[f][6] = new Piece(PieceType.PAWN, Color.BLACK, false);
        }
        // rooks
        squares[0][0] = new Piece(PieceType.ROOK, Color.WHITE, false);
        squares[7][0] = new Piece(PieceType.ROOK, Color.WHITE, false);
        squares[0][7] = new Piece(PieceType.ROOK, Color.BLACK, false);
        squares[7][7] = new Piece(PieceType.ROOK, Color.BLACK, false);

        // knights
        squares[1][0] = new Piece(PieceType.KNIGHT, Color.WHITE, false);
        squares[6][0] = new Piece(PieceType.KNIGHT, Color.WHITE, false);
        squares[1][7] = new Piece(PieceType.KNIGHT, Color.BLACK, false);
        squares[6][7] = new Piece(PieceType.KNIGHT, Color.BLACK, false);

        // bishops
        squares[2][0] = new Piece(PieceType.BISHOP, Color.WHITE, false);
        squares[5][0] = new Piece(PieceType.BISHOP, Color.WHITE, false);
        squares[2][7] = new Piece(PieceType.BISHOP, Color.BLACK, false);
        squares[5][7] = new Piece(PieceType.BISHOP, Color.BLACK, false);

        // queens
        squares[3][0] = new Piece(PieceType.QUEEN, Color.WHITE, false);
        squares[3][7] = new Piece(PieceType.QUEEN, Color.BLACK, false);

        // kings
        squares[4][0] = new Piece(PieceType.KING, Color.WHITE, false);
        squares[4][7] = new Piece(PieceType.KING, Color.BLACK, false);

        whiteCastleKingSide = whiteCastleQueenSide = true;
        blackCastleKingSide = blackCastleQueenSide = true;
    }

    public Piece getPiece(int file, int rank) {
        if (!inBounds(file, rank)) return null;
        return squares[file][rank];
    }

    public void setPiece(int file, int rank, Piece piece) {
        if (!inBounds(file, rank)) return;
        squares[file][rank] = piece;
    }

    private boolean inBounds(int f, int r) {
        return f >= 0 && f < 8 && r >= 0 && r < 8;
    }

    @Override
    public ChessBoard clone() {
        Piece[][] copy = new Piece[8][8];
        for (int f = 0; f < 8; f++) {
            System.arraycopy(this.squares[f], 0, copy[f], 0, 8);
        }
        return new ChessBoard(copy,
                whiteCastleKingSide, whiteCastleQueenSide,
                blackCastleKingSide, blackCastleQueenSide);
    }

    // ---------------------- Move generation helpers ----------------------

    public List<ChessMove> generateLegalMoves(Color color) {
        List<ChessMove> moves = new ArrayList<>();
        List<ChessMove> pseudo = generatePseudoLegalMoves(color);

        for (ChessMove m : pseudo) {
            ChessBoard copy = this.clone();
            copy.applyMoveNoValidation(m, color);
            if (!copy.isKingInCheck(color)) {
                moves.add(m);
            }
        }
        return moves;
    }

    private List<ChessMove> generatePseudoLegalMoves(Color color) {
        List<ChessMove> moves = new ArrayList<>();
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                Piece p = getPiece(f, r);
                if (p != null && p.getColor() == color) {
                    generatePieceMoves(color, f, r, p, moves);
                }
            }
        }
        // Castling moves
        addCastlingMoves(color, moves);
        return moves;
    }

    private void generatePieceMoves(Color color, int f, int r, Piece p, List<ChessMove> out) {
        switch (p.getType()) {
            case PAWN:
                generatePawnMoves(color, f, r, out);
                break;
            case KNIGHT:
                generateKnightMoves(color, f, r, out);
                break;
            case BISHOP:
                generateSlidingMoves(color, f, r, out, 1, 1, -1, 1, -1, -1, 1, -1);
                break;
            case ROOK:
                generateSlidingMoves(color, f, r, out, 1, 0, -1, 0, 0, 1, 0, -1);
                break;
            case QUEEN:
                generateSlidingMoves(color, f, r, out,
                        1, 0, -1, 0, 0, 1, 0, -1,
                        1, 1, -1, 1, -1, -1, 1, -1);
                break;
            case KING:
                generateKingMoves(color, f, r, out);
                break;
        }
    }

    private void generatePawnMoves(Color color, int f, int r, List<ChessMove> out) {
        int dir = (color == Color.WHITE) ? 1 : -1;
        int startRank = (color == Color.WHITE) ? 1 : 6;
        int promotionRank = (color == Color.WHITE) ? 7 : 0;

        int forwardRank = r + dir;
        // forward one
        if (inBounds(f, forwardRank) && getPiece(f, forwardRank) == null) {
            boolean promotion = forwardRank == promotionRank;
            out.add(new ChessMove(f, r, f, forwardRank,
                    false, false, promotion));
            // forward two from start
            if (r == startRank) {
                int twoForward = r + 2 * dir;
                if (getPiece(f, twoForward) == null) {
                    out.add(new ChessMove(f, r, f, twoForward));
                }
            }
        }

        // captures
        for (int df = -1; df <= 1; df += 2) {
            int cf = f + df;
            int cr = r + dir;
            if (!inBounds(cf, cr)) continue;
            Piece target = getPiece(cf, cr);
            if (target != null && target.getColor() != color) {
                boolean promotion = cr == promotionRank;
                out.add(new ChessMove(f, r, cf, cr,
                        false, false, promotion));
            }
        }
        // en passant intentionally NOT implemented
    }

    private void generateKnightMoves(Color color, int f, int r, List<ChessMove> out) {
        int[][] deltas = {
                {1, 2}, {2, 1}, {-1, 2}, {-2, 1},
                {1, -2}, {2, -1}, {-1, -2}, {-2, -1}
        };
        for (int[] d : deltas) {
            int nf = f + d[0];
            int nr = r + d[1];
            if (!inBounds(nf, nr)) continue;
            Piece target = getPiece(nf, nr);
            if (target == null || target.getColor() != color) {
                out.add(new ChessMove(f, r, nf, nr));
            }
        }
    }

    private void generateSlidingMoves(Color color, int f, int r,
                                      List<ChessMove> out, int... deltas) {
        for (int i = 0; i < deltas.length; i += 2) {
            int df = deltas[i];
            int dr = deltas[i + 1];
            int nf = f + df;
            int nr = r + dr;
            while (inBounds(nf, nr)) {
                Piece target = getPiece(nf, nr);
                if (target == null) {
                    out.add(new ChessMove(f, r, nf, nr));
                } else {
                    if (target.getColor() != color) {
                        out.add(new ChessMove(f, r, nf, nr));
                    }
                    break;
                }
                nf += df;
                nr += dr;
            }
        }
    }

    private void generateKingMoves(Color color, int f, int r, List<ChessMove> out) {
        for (int df = -1; df <= 1; df++) {
            for (int dr = -1; dr <= 1; dr++) {
                if (df == 0 && dr == 0) continue;
                int nf = f + df;
                int nr = r + dr;
                if (!inBounds(nf, nr)) continue;
                Piece target = getPiece(nf, nr);
                if (target == null || target.getColor() != color) {
                    out.add(new ChessMove(f, r, nf, nr));
                }
            }
        }
        // castling moves added separately in addCastlingMoves
    }

    private void addCastlingMoves(Color color, List<ChessMove> out) {
        int rank = (color == Color.WHITE) ? 0 : 7;
        Piece king = getPiece(4, rank);
        if (king == null || king.getType() != PieceType.KING || king.hasMoved()) {
            return;
        }
        boolean kingSideAllowed = (color == Color.WHITE)
                ? whiteCastleKingSide : blackCastleKingSide;
        boolean queenSideAllowed = (color == Color.WHITE)
                ? whiteCastleQueenSide : blackCastleQueenSide;
        if (!kingSideAllowed && !queenSideAllowed) return;

        // squares must be empty and not under attack
        if (kingSideAllowed &&
                getPiece(5, rank) == null &&
                getPiece(6, rank) == null &&
                !isSquareAttacked(4, rank, color.opposite()) &&
                !isSquareAttacked(5, rank, color.opposite()) &&
                !isSquareAttacked(6, rank, color.opposite())) {
            Piece rook = getPiece(7, rank);
            if (rook != null && rook.getType() == PieceType.ROOK && !rook.hasMoved()) {
                out.add(new ChessMove(4, rank, 6, rank,
                        true, false, false));
            }
        }

        if (queenSideAllowed &&
                getPiece(1, rank) == null &&
                getPiece(2, rank) == null &&
                getPiece(3, rank) == null &&
                !isSquareAttacked(4, rank, color.opposite()) &&
                !isSquareAttacked(3, rank, color.opposite()) &&
                !isSquareAttacked(2, rank, color.opposite())) {
            Piece rook = getPiece(0, rank);
            if (rook != null && rook.getType() == PieceType.ROOK && !rook.hasMoved()) {
                out.add(new ChessMove(4, rank, 2, rank,
                        false, true, false));
            }
        }
    }

    // ---------------------- King in check helpers ----------------------

    public boolean isKingInCheck(Color color) {
        int kingFile = -1;
        int kingRank = -1;
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                Piece p = getPiece(f, r);
                if (p != null && p.getColor() == color && p.getType() == PieceType.KING) {
                    kingFile = f;
                    kingRank = r;
                }
            }
        }
        if (kingFile == -1) return false; // shouldn't happen
        return isSquareAttacked(kingFile, kingRank, color.opposite());
    }

    private boolean isSquareAttacked(int file, int rank, Color byColor) {
        // Scan board and see if any pseudo-legal move from byColor hits this square
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                Piece p = getPiece(f, r);
                if (p != null && p.getColor() == byColor) {
                    if (attacksSquare(byColor, f, r, p, file, rank)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean attacksSquare(Color color, int f, int r, Piece p,
                                  int targetFile, int targetRank) {
        int df = targetFile - f;
        int dr = targetRank - r;

        switch (p.getType()) {
            case PAWN:
                int dir = (color == Color.WHITE) ? 1 : -1;
                return dr == dir && Math.abs(df) == 1;
            case KNIGHT:
                return (Math.abs(df) == 1 && Math.abs(dr) == 2) ||
                       (Math.abs(df) == 2 && Math.abs(dr) == 1);
            case BISHOP:
                if (Math.abs(df) != Math.abs(dr)) return false;
                return isPathClear(f, r, targetFile, targetRank);
            case ROOK:
                if (df != 0 && dr != 0) return false;
                return isPathClear(f, r, targetFile, targetRank);
            case QUEEN:
                if (df == 0 || dr == 0 || Math.abs(df) == Math.abs(dr)) {
                    return isPathClear(f, r, targetFile, targetRank);
                }
                return false;
            case KING:
                return Math.max(Math.abs(df), Math.abs(dr)) == 1;
            default:
                return false;
        }
    }

    private boolean isPathClear(int f, int r, int tf, int tr) {
        int df = Integer.compare(tf, f);
        int dr = Integer.compare(tr, r);
        f += df;
        r += dr;
        while (f != tf || r != tr) {
            if (getPiece(f, r) != null) return false;
            f += df;
            r += dr;
        }
        return true;
    }

    // ---------------------- Applying a move ----------------------

    public void applyMoveNoValidation(ChessMove move, Color movingColor) {
        int fromF = move.getFromFile();
        int fromR = move.getFromRank();
        int toF = move.getToFile();
        int toR = move.getToRank();

        Piece moving = getPiece(fromF, fromR);
        if (moving == null) return;

        // Castling
        if (move.isCastleKingSide() || move.isCastleQueenSide()) {
            // move king
            setPiece(fromF, fromR, null);
            setPiece(toF, toR, moving.withMoved());
            // move rook
            int rookFromF = move.isCastleKingSide() ? 7 : 0;
            int rookToF = move.isCastleKingSide() ? 5 : 3;
            Piece rook = getPiece(rookFromF, fromR);
            setPiece(rookFromF, fromR, null);
            if (rook != null) {
                setPiece(rookToF, fromR, rook.withMoved());
            }
            updateCastleRightsAfterMove(movingColor, true, true);
            return;
        }

        // Normal move / capture
        setPiece(fromF, fromR, null);

        // Promotion
        if (move.isPromotion()) {
            setPiece(toF, toR, new Piece(PieceType.QUEEN, movingColor, true));
        } else {
            setPiece(toF, toR, moving.withMoved());
        }

        updateCastleRightsAfterMove(movingColor,
                moving.getType() == PieceType.KING,
                moving.getType() == PieceType.ROOK && fromR == ((movingColor == Color.WHITE) ? 0 : 7));
    }

    private void updateCastleRightsAfterMove(Color color, boolean kingMoved, boolean rookMoved) {
        if (color == Color.WHITE) {
            if (kingMoved) {
                whiteCastleKingSide = false;
                whiteCastleQueenSide = false;
            }
            if (rookMoved) {
                // if rook moved from a1 or h1, disable respective side
                Piece left = getPiece(0, 0);
                if (left == null || left.getColor() != Color.WHITE || left.hasMoved()) {
                    whiteCastleQueenSide = false;
                }
                Piece right = getPiece(7, 0);
                if (right == null || right.getColor() != Color.WHITE || right.hasMoved()) {
                    whiteCastleKingSide = false;
                }
            }
        } else {
            if (kingMoved) {
                blackCastleKingSide = false;
                blackCastleQueenSide = false;
            }
            if (rookMoved) {
                Piece left = getPiece(0, 7);
                if (left == null || left.getColor() != Color.BLACK || left.hasMoved()) {
                    blackCastleQueenSide = false;
                }
                Piece right = getPiece(7, 7);
                if (right == null || right.getColor() != Color.BLACK || right.hasMoved()) {
                    blackCastleKingSide = false;
                }
            }
        }
    }

    // for GUI: safe copy
    public Piece[][] copySquares() {
        Piece[][] copy = new Piece[8][8];
        for (int f = 0; f < 8; f++) {
            System.arraycopy(squares[f], 0, copy[f], 0, 8);
        }
        return copy;
    }
}
