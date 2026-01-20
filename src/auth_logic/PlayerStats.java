package auth_logic;
import java.util.ArrayList;
import java.util.List;

public class PlayerStats {
    // Basic Ratings (Default 1200)
    private int rankChess = 1200, rankGo = 1200, rankTTT = 1200;
    private int winsChess = 0, lossChess = 0, tieChess = 0;
    private int winsGo = 0, lossGo = 0, tieGo = 0;
    private int winsTTT = 0, lossTTT = 0, tieTTT = 0;
    private List<Integer> historyChess = new ArrayList<>();
    
    // --- Getters used by RankingAlgorithm ---
    public int getRankChess() { return rankChess; }
    public int getRankGo() { return rankGo; }
    public int getRankTTT() { return rankTTT; }
    
    // --- Setters used by RankingAlgorithm ---
    public void setRankChess(int r) { this.rankChess = r; }
    public void setRankGo(int r) { this.rankGo = r; }
    public void setRankTTT(int r) { this.rankTTT = r; }

    // --- Stats Helpers ---
    public void setWinsChess(int w) { this.winsChess = w; }
    public int getWinsChess() { return winsChess; }
    public void setLossChess(int l) { this.lossChess = l; }
    public int getLossChess() { return lossChess; }
    public void setTieChess(int t) { this.tieChess = t; }
    public int getTieChess() { return tieChess; }
    
    // Stubs for other games to prevent errors
    public void setWinsGo(int w) { this.winsGo = w; }
    public int getWinsGo() { return winsGo; }
    public void setLossGo(int l) { this.lossGo = l; }
    public int getLossGo() { return lossGo; }
    public void setTieGo(int t) { this.tieGo = t; }
    public int getTieGo() { return tieGo; }

    public void setWinsTTT(int w) { this.winsTTT = w; }
    public int getWinsTTT() { return winsTTT; }
    public void setLossTTT(int l) { this.lossTTT = l; }
    public int getLossTTT() { return lossTTT; }
    public void setTieTTT(int t) { this.tieTTT = t; }
    public int getTieTTT() { return tieTTT; }

    public List<Integer> getHistoricalRatings(String game) { return historyChess; }
    public void addHistoricalRating(String game, int rating) { historyChess.add(rating); }
}