package leaderboard;

import org.junit.Test;
import static org.junit.Assert.*;
import auth_logic.Player;
import leaderboard_logic.RankingAlgorithm;

public class RankingAlgorithmTest {

    @Test
    public void getPlayerRatingForGo() {
        Player player = new Player("TestPlayer");
        player.getPlayerStats().setRankGo(1500);
        
        int rating = RankingAlgorithm.getPlayerRating(player, "Go");
        assertEquals(1500, rating);
    }

    @Test
    public void getPlayerRatingForChess() {
        Player player = new Player("TestPlayer");
        player.getPlayerStats().setRankChess(1400);
        
        int rating = RankingAlgorithm.getPlayerRating(player, "Chess");
        assertEquals(1400, rating);
    }

    @Test
    public void getPlayerRatingForTicTacToe() {
        Player player = new Player("TestPlayer");
        player.getPlayerStats().setRankTTT(1300);
        
        int rating = RankingAlgorithm.getPlayerRating(player, "Tic Tac Toe");
        assertEquals(1300, rating);
    }

    @Test
    public void getPlayerRatingReturnsDefaultWhenPlayerIsNull() {
        int rating = RankingAlgorithm.getPlayerRating(null, "Go");
        assertEquals(1200, rating);
    }

    @Test
    public void getPlayerRatingReturnsDefaultForUnknownGame() {
        Player player = new Player("TestPlayer");
        int rating = RankingAlgorithm.getPlayerRating(player, "UnknownGame");
        assertEquals(1200, rating);
    }

    @Test
    public void recordGoResultUpdatesRatings() {
        Player winner = new Player("Winner");
        Player loser = new Player("Loser");
        winner.getPlayerStats().setRankGo(1500);
        loser.getPlayerStats().setRankGo(1200);
        
        int winnerBefore = winner.getPlayerStats().getRankGo();
        int loserBefore = loser.getPlayerStats().getRankGo();
        
        RankingAlgorithm.recordGoResult(winner, loser);
        
        // Winner should gain rating, loser should lose rating
        assertTrue(winner.getPlayerStats().getRankGo() > winnerBefore);
        assertTrue(loser.getPlayerStats().getRankGo() < loserBefore);
    }

    @Test
    public void recordGoResultUpdatesWinLossStats() {
        Player winner = new Player("Winner");
        Player loser = new Player("Loser");
        
        int winsBefore = winner.getPlayerStats().getWinsGo();
        int lossesBefore = loser.getPlayerStats().getLossGo();
        
        RankingAlgorithm.recordGoResult(winner, loser);
        
        assertEquals(winsBefore + 1, winner.getPlayerStats().getWinsGo());
        assertEquals(lossesBefore + 1, loser.getPlayerStats().getLossGo());
    }

    @Test
    public void recordChessResultUpdatesRatings() {
        Player winner = new Player("Winner");
        Player loser = new Player("Loser");
        winner.getPlayerStats().setRankChess(1600);
        loser.getPlayerStats().setRankChess(1400);
        
        int winnerBefore = winner.getPlayerStats().getRankChess();
        int loserBefore = loser.getPlayerStats().getRankChess();
        
        RankingAlgorithm.recordChessResult(winner, loser);
        
        assertTrue(winner.getPlayerStats().getRankChess() > winnerBefore);
        assertTrue(loser.getPlayerStats().getRankChess() < loserBefore);
    }

    @Test
    public void recordChessResultUpdatesWinLossStats() {
        Player winner = new Player("Winner");
        Player loser = new Player("Loser");
        
        int winsBefore = winner.getPlayerStats().getWinsChess();
        int lossesBefore = loser.getPlayerStats().getLossChess();
        
        RankingAlgorithm.recordChessResult(winner, loser);
        
        assertEquals(winsBefore + 1, winner.getPlayerStats().getWinsChess());
        assertEquals(lossesBefore + 1, loser.getPlayerStats().getLossChess());
    }

    @Test
    public void recordTicTacToeResultUpdatesRatings() {
        Player winner = new Player("Winner");
        Player loser = new Player("Loser");
        winner.getPlayerStats().setRankTTT(1450);
        loser.getPlayerStats().setRankTTT(1250);
        
        int winnerBefore = winner.getPlayerStats().getRankTTT();
        int loserBefore = loser.getPlayerStats().getRankTTT();
        
        RankingAlgorithm.recordTicTacToeResult(winner, loser);
        
        assertTrue(winner.getPlayerStats().getRankTTT() > winnerBefore);
        assertTrue(loser.getPlayerStats().getRankTTT() < loserBefore);
    }

    @Test
    public void recordTicTacToeResultUpdatesWinLossStats() {
        Player winner = new Player("Winner");
        Player loser = new Player("Loser");
        
        int winsBefore = winner.getPlayerStats().getWinsTTT();
        int lossesBefore = loser.getPlayerStats().getLossTTT();
        
        RankingAlgorithm.recordTicTacToeResult(winner, loser);
        
        assertEquals(winsBefore + 1, winner.getPlayerStats().getWinsTTT());
        assertEquals(lossesBefore + 1, loser.getPlayerStats().getLossTTT());
    }

    @Test
    public void recordGoResultWithNullPlayersDoesNotCrash() {
        // Should not throw exception
        RankingAlgorithm.recordGoResult(null, null);
        RankingAlgorithm.recordGoResult(new Player("Test"), null);
        RankingAlgorithm.recordGoResult(null, new Player("Test"));
    }

    @Test
    public void recordGoResultWithSamePlayerDoesNotUpdate() {
        Player player = new Player("SamePlayer");
        player.getPlayerStats().setRankGo(1200);
        int ratingBefore = player.getPlayerStats().getRankGo();
        
        RankingAlgorithm.recordGoResult(player, player);
        
        // Rating should not change when player plays against themselves
        assertEquals(ratingBefore, player.getPlayerStats().getRankGo());
    }
}

