package go_logic;

import java.util.*;

/**
 * Represents the Go board and all capture logic.
 */
public class GoBoard {

    private final int size;
    private final StoneColor[][] grid;

    public GoBoard(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be > 0");
        }
        this.size = size;
        this.grid = new StoneColor[size][size];
    }

    public int getSize() {
        return size;
    }

    public boolean isOnBoard(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public StoneColor getStone(int row, int col) {
        if (!isOnBoard(row, col)) {
            throw new IndexOutOfBoundsException("Off-board: " + row + "," + col);
        }
        return grid[row][col];
    }

    public boolean isEmpty(int row, int col) {
        return getStone(row, col) == null;
    }

    /**
     * Plays a stone for 'color' at (row, col).
     * - Captures adjacent opponent groups with no liberties.
     * - Forbids suicide: if the new group has no liberties and captured nothing.
     */
    public MoveResult playStone(int row, int col, StoneColor color) throws IllegalMoveException {
        if (color == null) {
            throw new IllegalArgumentException("color must not be null");
        }
        if (!isOnBoard(row, col)) {
            throw new IllegalMoveException("Move is off-board: " + row + "," + col);
        }
        if (!isEmpty(row, col)) {
            throw new IllegalMoveException("Intersection is already occupied: " + row + "," + col);
        }

        // Tentatively place the stone
        grid[row][col] = color;

        Set<Point> totalCaptured = new HashSet<>();

        // 1. Capture adjacent opponent groups with no liberties
        for (Point n : neighbours(row, col)) {
            StoneColor neighbourColor = getStone(n.row(), n.col());
            if (neighbourColor == null || neighbourColor == color) {
                continue;
            }
            Set<Point> group = collectGroup(n.row(), n.col(), neighbourColor);
            if (!hasLiberty(group)) {
                for (Point p : group) {
                    grid[p.row()][p.col()] = null;
                }
                totalCaptured.addAll(group);
            }
        }

        // 2. Check suicide for our own group
        Set<Point> myGroup = collectGroup(row, col, color);
        if (!hasLiberty(myGroup) && totalCaptured.isEmpty()) {
            grid[row][col] = null; // revert
            throw new IllegalMoveException("Suicide move at " + row + "," + col);
        }

        return new MoveResult(totalCaptured);
    }

    private List<Point> neighbours(int row, int col) {
        List<Point> res = new ArrayList<>(4);
        if (isOnBoard(row - 1, col)) res.add(new Point(row - 1, col));
        if (isOnBoard(row + 1, col)) res.add(new Point(row + 1, col));
        if (isOnBoard(row, col - 1)) res.add(new Point(row, col - 1));
        if (isOnBoard(row, col + 1)) res.add(new Point(row, col + 1));
        return res;
    }

    private Set<Point> collectGroup(int row, int col, StoneColor color) {
        Set<Point> group = new HashSet<>();
        if (!isOnBoard(row, col)) return group;
        if (getStone(row, col) != color) return group;

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(row, col));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            if (!group.add(p)) continue;
            for (Point n : neighbours(p.row(), p.col())) {
                if (getStone(n.row(), n.col()) == color && !group.contains(n)) {
                    stack.push(n);
                }
            }
        }
        return group;
    }

    private boolean hasLiberty(Set<Point> group) {
        for (Point p : group) {
            for (Point n : neighbours(p.row(), p.col())) {
                if (getStone(n.row(), n.col()) == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
