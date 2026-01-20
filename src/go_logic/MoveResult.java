package go_logic;
import java.util.Collections;
import java.util.Set;

/**
 * Result of applying a move: which stones (if any) were captured.
 */
public class MoveResult {
    private final Set<Point> captured;

    public MoveResult(Set<Point> captured) {
        this.captured = Collections.unmodifiableSet(captured);
    }

    public Set<Point> getCaptured() {
        return captured;
    }

    public int getCaptureCount() {
        return captured.size();
    }
}
