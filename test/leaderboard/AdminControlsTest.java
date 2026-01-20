package leaderboard;

import org.junit.Test;
import static org.junit.Assert.*;
import auth_logic.Player;
import auth_logic.PlayerData;
import leaderboard_logic.AdminControls;

public class AdminControlsTest {

    @Test
    public void resetLeaderboardForChess() {
        // Create test players
        Player player1 = new Player("TestPlayer1");
        Player player2 = new Player("TestPlayer2");
        player1.getPlayerStats().setRankChess(1500);
        player1.getPlayerStats().setWinsChess(10);
        player1.getPlayerStats().setLossChess(5);
        player2.getPlayerStats().setRankChess(1400);
        player2.getPlayerStats().setWinsChess(8);
        player2.getPlayerStats().setLossChess(3);
        
        // Add to PlayerData for testing
        PlayerData.players.add(player1);
        PlayerData.players.add(player2);
        
        AdminControls.resetLeaderboard("chess");
        
        // Check that ratings are reset to 1200
        assertEquals(1200, player1.getPlayerStats().getRankChess());
        assertEquals(1200, player2.getPlayerStats().getRankChess());
        
        // Check that wins and losses are reset to 0
        assertEquals(0, player1.getPlayerStats().getWinsChess());
        assertEquals(0, player1.getPlayerStats().getLossChess());
        assertEquals(0, player2.getPlayerStats().getWinsChess());
        assertEquals(0, player2.getPlayerStats().getLossChess());
    }

    @Test
    public void resetLeaderboardForGo() {
        Player player = new Player("TestPlayer");
        player.getPlayerStats().setRankGo(1600);
        player.getPlayerStats().setWinsGo(15);
        player.getPlayerStats().setLossGo(2);
        
        PlayerData.players.add(player);
        
        AdminControls.resetLeaderboard("go");
        
        assertEquals(1200, player.getPlayerStats().getRankGo());
        assertEquals(0, player.getPlayerStats().getWinsGo());
        assertEquals(0, player.getPlayerStats().getLossGo());
    }

    @Test
    public void resetLeaderboardForTicTacToe() {
        Player player = new Player("TestPlayer");
        player.getPlayerStats().setRankTTT(1350);
        player.getPlayerStats().setWinsTTT(12);
        player.getPlayerStats().setLossTTT(4);
        
        PlayerData.players.add(player);
        
        AdminControls.resetLeaderboard("tic tac toe");
        
        assertEquals(1200, player.getPlayerStats().getRankTTT());
        assertEquals(0, player.getPlayerStats().getWinsTTT());
        assertEquals(0, player.getPlayerStats().getLossTTT());
    }

    @Test
    public void resetLeaderboardThrowsExceptionForNullGame() {
        try {
            AdminControls.resetLeaderboard(null);
            fail("Expected IllegalArgumentException for null game");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid game type"));
        }
    }

    @Test
    public void resetLeaderboardThrowsExceptionForEmptyGame() {
        try {
            AdminControls.resetLeaderboard("");
            fail("Expected IllegalArgumentException for empty game");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid game type"));
        }
    }

    @Test
    public void resetLeaderboardThrowsExceptionForInvalidGame() {
        try {
            AdminControls.resetLeaderboard("InvalidGame");
            fail("Expected IllegalArgumentException for invalid game");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid game type"));
        }
    }
}

