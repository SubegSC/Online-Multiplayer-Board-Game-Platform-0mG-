import javax.swing.*;
import java.awt.*;
import matchmaking.*;
import auth_logic.PlayerData;

public class MainGui {
    // Colors
    public static final Color BG_COL = new Color(20, 20, 20);
    public static final Color T_COL = new Color(228, 228, 228);
    public static final Color BTN_COL = new Color(35, 35, 35);

    private static JFrame frame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    private static String currentUsername = "Guest";
    private static String currentBio = "Ready to play some games!";
    private static JLabel lobbyWelcomeLabel; 
    private static JLabel lobbyStatsLabel; // Stats label on lobby screen
    private static JComboBox<String> lobbyGameSelector; // Game selector on lobby screen

    // --- Task 2: matchmaking state ---
    /** Single LobbyManager instance used by Create/Join lobby actions. */
    private static LobbyManager lobbyManager = new LobbyManager();
    
    /** Centralized friend list with online/offline status. 
     * This is the single source of truth for friend status across the application. */
    private static String[] getFriendsList() {
        return new String[] {
            "Alice (Online - In Lobby)",
            "Bob (Online - In Lobby)",
            "Charlie (Online - In Lobby)",
            "Dave (Offline)"
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGui::start);
    }

    // Checks whether logged in
    public static boolean isLoggedIn() {
        return !currentUsername.equals("Guest");
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    // Logout sets default parameters
    public static void logout() {
        currentUsername = "Guest"; // Reset to guest
        currentBio = "Ready to play some games!";
        showCard("LANDING");
    }

    static void start() {
        frame = new JFrame("OMG -- Online Multiplayer Board Game Platform");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        frame.setContentPane(mainPanel);

        // List of different screens
        // Start-up /landing screen
        mainPanel.add(createLandingPanel(), "LANDING");
        
        // Login screen
        mainPanel.add(createLoginPanel(), "LOGIN");

        // Post-login lobby screen
        mainPanel.add(createLobbyPanel(), "LOBBY");

        // Profile screen
        mainPanel.add(createProfilePanel(), "PROFILE");

        // Register new account screen
        mainPanel.add(createRegisterPanel(), "REGISTER");
        
        // Leaderboard screen
        mainPanel.add(new LeaderboardPanel(), "LEADERBOARD");
        
        // Setup the landing screen to show first
        cardLayout.show(mainPanel, "LANDING");
        frame.setVisible(true);
    }

    public static void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
        // Update stats when returning to lobby
        if (cardName.equals("LOBBY")) {
            updateLobbyStats();
        }
    }
    
    /** Update the lobby stats label with real player data for the selected game. */
    private static void updateLobbyStats() {
        if (lobbyStatsLabel == null) return;
        
        if (currentUsername.equals("Guest")) {
            lobbyStatsLabel.setText("Rank: -- | Wins: -- | Losses: --");
            return;
        }
        
        // Get selected game type from dropdown
        GameType selectedGame = GameType.GO; // Default to Go
        if (lobbyGameSelector != null) {
            String selectedGameStr = (String) lobbyGameSelector.getSelectedItem();
            if (selectedGameStr != null) {
                if (selectedGameStr.toLowerCase().contains("go")) {
                    selectedGame = GameType.GO;
                } else if (selectedGameStr.toLowerCase().contains("chess")) {
                    selectedGame = GameType.CHESS;
                } else {
                    selectedGame = GameType.TIC_TAC_TOE;
                }
            }
        }
        
        // Get player from PlayerData
        auth_logic.Player player = PlayerData.getOrCreateByName(currentUsername);
        if (player == null) {
            lobbyStatsLabel.setText("Rank: -- | Wins: -- | Losses: --");
            return;
        }
        
        auth_logic.PlayerStats stats = player.getPlayerStats();
        int rating, wins, losses;
        
        // Get stats for the selected game
        switch (selectedGame) {
            case CHESS:
                rating = stats.getRankChess();
                wins = stats.getWinsChess();
                losses = stats.getLossChess();
                break;
            case GO:
                rating = stats.getRankGo();
                wins = stats.getWinsGo();
                losses = stats.getLossGo();
                break;
            case TIC_TAC_TOE:
                rating = stats.getRankTTT();
                wins = stats.getWinsTTT();
                losses = stats.getLossTTT();
                break;
            default:
                rating = stats.getRankGo();
                wins = stats.getWinsGo();
                losses = stats.getLossGo();
                break;
        }
        
        lobbyStatsLabel.setText(String.format("Rank: %d | Wins: %d | Losses: %d", 
            rating, wins, losses));
    }

    /** Get a player's rating for a specific game type. */
    private static int getPlayerRatingForGame(auth_logic.Player player, GameType gameType) {
        if (player == null) return 1200; // Default rating
        
        auth_logic.PlayerStats stats = player.getPlayerStats();
        switch (gameType) {
            case CHESS:
                return stats.getRankChess();
            case GO:
                return stats.getRankGo();
            case TIC_TAC_TOE:
                return stats.getRankTTT();
            default:
                return 1200;
        }
    }
    
    /** Find a suitable opponent from PlayerData based on ELO rating and online status. */
    private static matchmaking.Player findSuitableOpponent(String myUsername, GameType gameType, int myRating) {
        // Get list of all friends with their status
        String[] allFriends = getFriendsList();
        
        // Build sets for online friends and all friend names
        java.util.Set<String> onlineFriendNames = new java.util.HashSet<>();
        java.util.Set<String> allFriendNames = new java.util.HashSet<>();
        for (String friend : allFriends) {
            String name = friend.split("\\s+")[0];
            allFriendNames.add(name);
            if (!friend.contains("Offline")) {
                onlineFriendNames.add(name);
            }
        }
        
        // Get all players from PlayerData
        java.util.List<auth_logic.Player> allPlayers = PlayerData.getAllPlayers();
        
        // Filter and find best match
        matchmaking.Player bestOpponent = null;
        int bestRatingDiff = Integer.MAX_VALUE;
        final int MAX_RATING_DIFF = 200; // Same as Matchmaker uses
        
        for (auth_logic.Player player : allPlayers) {
            // Skip self
            if (player.getUsername().equalsIgnoreCase(myUsername)) {
                continue;
            }
            
            // Get opponent's rating for this game
            int opponentRating = getPlayerRatingForGame(player, gameType);
            int ratingDiff = Math.abs(myRating - opponentRating);
            
            // Check if within acceptable range and better than current best
            if (ratingDiff <= MAX_RATING_DIFF && ratingDiff < bestRatingDiff) {
                boolean isFriend = allFriendNames.contains(player.getUsername());
                boolean isOnlineFriend = onlineFriendNames.contains(player.getUsername());
                
                // Match if:
                // 1. They're an online friend, OR
                // 2. They're not a friend at all (can match with non-friends)
                // Skip offline friends
                if (isOnlineFriend || !isFriend) {
                    bestOpponent = new matchmaking.Player(
                        "player:" + player.getUsername(), 
                        player.getUsername(), 
                        opponentRating
                    );
                    bestRatingDiff = ratingDiff;
                }
            }
        }
        
        return bestOpponent;
    }

    /** Show a dialog to select an online friend to play against. */
    private static String showFriendSelectionDialog(JPanel parent) {
        // Get list of online friends (filter out offline ones)
        String[] allFriends = getFriendsList();
        
        // Filter to only online friends
        java.util.List<String> onlineFriends = new java.util.ArrayList<>();
        for (String friend : allFriends) {
            if (!friend.contains("Offline")) {
                // Extract just the name (remove status info)
                String name = friend.split("\\s+")[0];
                onlineFriends.add(name);
            }
        }
        
        if (onlineFriends.isEmpty()) {
            JOptionPane.showMessageDialog(parent, 
                "No online friends available. Please try again later.",
                "No Friends Online", 
                JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        
        // Create selection dialog
        String[] friendArray = onlineFriends.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(
            parent,
            "Select a friend to play against:",
            "Select Friend",
            JOptionPane.QUESTION_MESSAGE,
            null,
            friendArray,
            friendArray[0]
        );
        
        return selected; // Returns null if user cancels
    }

    /** Remove any existing game panel before adding a new one to prevent duplicate recordings. */
    private static void removeExistingGamePanel() {
        Component[] components = mainPanel.getComponents();
        boolean removed = false;
        for (Component comp : components) {
            if (comp instanceof GoGamePanel || comp instanceof ChessGamePanel || comp instanceof TicTacToeGamePanel) {
                System.out.println("Removing existing game panel: " + comp.getClass().getSimpleName());
                mainPanel.remove(comp);
                removed = true;
            }
        }
        if (removed) {
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    // Panel setups
    private static JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BTN_COL);
        button.setForeground(T_COL);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private static JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Top of screen
        JLabel header = new JLabel("Edit Profile", SwingConstants.CENTER);
        header.setForeground(T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(header, gbc);

        // Display name
        JLabel userLabel = new JLabel("Display Name:");
        userLabel.setForeground(T_COL);
        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(userLabel, gbc);

        JTextField userField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(userField, gbc);

        // Bio info
        JLabel bioLabel = new JLabel("Bio / Status:");
        bioLabel.setForeground(T_COL);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(bioLabel, gbc);

        JTextArea bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        JScrollPane bioScroll = new JScrollPane(bioArea);
        gbc.gridx = 1;
        panel.add(bioScroll, gbc);

        // Buttons for profile edits
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton cancelBtn = styledButton("Cancel");
        JButton saveBtn = styledButton("Save Changes");
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                userField.setText(currentUsername);
                bioArea.setText(currentBio);
            }
        });

        saveBtn.addActionListener(e -> {
            String newName = userField.getText().trim();
            if (!newName.isEmpty()) {
                currentUsername = newName;
                currentBio = bioArea.getText().trim();
                
                // Update lobby welcome message
                if (lobbyWelcomeLabel != null) {
                    lobbyWelcomeLabel.setText("Welcome, " + currentUsername + "!");
                }
                
                // Sort of a profile stub
                JOptionPane.showMessageDialog(panel, "Profile Updated Successfully!");
                showCard("LOBBY");
            } else {
                JOptionPane.showMessageDialog(panel, "Username cannot be empty.");
            }
        });

        cancelBtn.addActionListener(e -> showCard("LOBBY"));

        return panel;
    }

    private static JPanel createLandingPanel() {
        JPanel base = new JPanel(new BorderLayout(16, 16));
        base.setBackground(BG_COL);

        JLabel title = new JLabel("Online Multiplayer Board Game Platform", SwingConstants.CENTER);
        title.setForeground(T_COL);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        base.add(title, BorderLayout.NORTH);

        // Buttons
        JButton loginBtn = styledButton("Log In");
        JButton regBtn = styledButton("Register");
        JButton lbrdBtn = styledButton("View Leaderboard");

        // Possible actions
        loginBtn.addActionListener(e -> showCard("LOGIN"));
        regBtn.addActionListener(e -> showCard("REGISTER"));
        lbrdBtn.addActionListener(e -> showCard("LEADERBOARD"));

        // Layout for buttons
        JPanel startButtons = new JPanel();
        startButtons.setOpaque(false);
        startButtons.setLayout(new BoxLayout(startButtons, BoxLayout.Y_AXIS));
        
        startButtons.add(Box.createVerticalGlue());
        startButtons.add(loginBtn);
        startButtons.add(Box.createVerticalStrut(10));
        startButtons.add(regBtn);
        startButtons.add(Box.createVerticalStrut(10));
        startButtons.add(lbrdBtn);
        startButtons.add(Box.createVerticalGlue());

        base.add(startButtons, BorderLayout.CENTER);
        return base;
    }

    // Login panel
    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); 
        panel.setBackground(BG_COL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.gridx = 0; 

        // Player login header
        JLabel header = new JLabel("Player Login", SwingConstants.CENTER);
        header.setForeground(T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridy = 0; 
        panel.add(header, gbc);

        // Username boxes
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(T_COL);
        gbc.gridy = 1; 
        panel.add(userLabel, gbc);

        JTextField userField = new JTextField(20);
        gbc.gridy = 2; 
        panel.add(userField, gbc);

        // Password boxes
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(T_COL);
        gbc.gridy = 3; 
        panel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(20);
        gbc.gridy = 4; 
        panel.add(passField, gbc);

        // More buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton backBtn = styledButton("Back");
        JButton submitBtn = styledButton("Login");
        
        buttonPanel.add(backBtn);
        buttonPanel.add(submitBtn);

        gbc.gridy = 5;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(buttonPanel, gbc);

        submitBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            // Ensure fields are not empty
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(panel, 
                    "Please enter both a username and password.", 
                    "Input Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Cannot name your account Guest
            if (username.equalsIgnoreCase("Guest")) {
                JOptionPane.showMessageDialog(panel, 
                    "Login Failed: Invalid credentials.", 
                    "Invalid Username", 
                    JOptionPane.ERROR_MESSAGE);
                return;}

            // Included for possible demo purposes
            // Entering "testfail" as the username will always simulate a failed authentication
            if (username.equalsIgnoreCase("testfail")) {
                JOptionPane.showMessageDialog(panel, 
                    "Login Failed: Invalid credentials or account locked.", 
                    "Auth Error", 
                    JOptionPane.ERROR_MESSAGE);
                passField.setText(""); // clear password to retry
                return;
            }

            // Check if player exists in PlayerData (must have registered first)
            if (!PlayerData.playerExists(username)) {
                JOptionPane.showMessageDialog(panel, 
                    "Login Failed: Account not found. Please register first.", 
                    "Account Not Found", 
                    JOptionPane.ERROR_MESSAGE);
                passField.setText(""); // clear password to retry
                return;
            }

            // Successful login, changes lobby welcome message
            currentUsername = username;
            
            // Get the existing player from PlayerData
            PlayerData.getOrCreateByName(username);
            
            if (lobbyWelcomeLabel != null) {
                lobbyWelcomeLabel.setText("Welcome, " + currentUsername + "!");
            }
            
            // Clear fields so they are empty if user logs out later
            userField.setText("");
            passField.setText("");
            
            // To lobby
            showCard("LOBBY");
        });
        
        backBtn.addActionListener(e -> showCard("LANDING"));
        
        return panel;
    }

    // Register account panel
    private static JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel header = new JLabel("Create Account", SwingConstants.CENTER);
        header.setForeground(T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridy = 0;
        panel.add(header, gbc);

        // Username box
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(T_COL);
        gbc.gridy = 1;
        panel.add(userLabel, gbc);

        JTextField userField = new JTextField(20);
        gbc.gridy = 2;
        panel.add(userField, gbc);

        // 2. Email box
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setForeground(T_COL);
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        gbc.gridy = 4;
        panel.add(emailField, gbc);

        // Password box
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(T_COL);
        gbc.gridy = 5;
        panel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(20);
        gbc.gridy = 6;
        panel.add(passField, gbc);

        // Confirm password box
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setForeground(T_COL);
        gbc.gridy = 7;
        panel.add(confirmLabel, gbc);

        JPasswordField confirmField = new JPasswordField(20);
        gbc.gridy = 8;
        panel.add(confirmField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton backBtn = styledButton("Cancel");
        JButton regBtn = styledButton("Sign Up");

        buttonPanel.add(backBtn);
        buttonPanel.add(regBtn);

        gbc.gridy = 9;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(buttonPanel, gbc);

        regBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String mail = emailField.getText().trim();
            String p1 = new String(passField.getPassword());
            String p2 = new String(confirmField.getPassword());

            // Empty fields error
            if (u.isEmpty() || mail.isEmpty() || p1.isEmpty()) {
                JOptionPane.showMessageDialog(panel, 
                    "Please fill in all fields.", 
                    "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (u.equalsIgnoreCase("Guest")) {
                JOptionPane.showMessageDialog(panel, 
                    "Cannot cannot choose that username.", 
                    "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Mismatched password boxes error
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(panel, 
                    "Passwords do not match.", 
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create and store the new player in PlayerData
            // New players start with default stats: 0 games, rating 1200 (default in PlayerStats)
            PlayerData.getOrCreateByName(u);
            
            // Success, still a stub cuz no real authentication performed
            JOptionPane.showMessageDialog(panel, 
                "Account created successfully!\nPlease log in with your new credentials.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear fields
            userField.setText("");
            emailField.setText("");
            passField.setText("");
            confirmField.setText("");
            
            // Navigate to Login screen
            showCard("LOGIN");
        });

        backBtn.addActionListener(e -> showCard("LANDING"));

        return panel;
    }

    // Main lobby panel
    private static JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_COL);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        
        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);

        lobbyWelcomeLabel = new JLabel("Welcome, " + currentUsername + "!", SwingConstants.CENTER);
        lobbyWelcomeLabel.setForeground(T_COL);
        lobbyWelcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        lobbyWelcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Dynamic stats label - will be updated with real player data
        lobbyStatsLabel = new JLabel("", SwingConstants.CENTER);
        lobbyStatsLabel.setForeground(Color.GRAY);
        lobbyStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lobbyStatsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateLobbyStats(); // Initialize with current player's stats

        textWrapper.add(lobbyWelcomeLabel);
        textWrapper.add(Box.createVerticalStrut(5));
        textWrapper.add(lobbyStatsLabel);
        
        // Update stats when panel is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                updateLobbyStats();
            }
        });

        // Edit profile button
        JButton editProfileBtn = new JButton("Edit Profile");
        editProfileBtn.setBackground(BTN_COL);
        editProfileBtn.setForeground(T_COL);
        editProfileBtn.setFocusPainted(false);
        editProfileBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        editProfileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JPanel editBtnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editBtnWrapper.setOpaque(false);
        editBtnWrapper.add(editProfileBtn);

        profilePanel.add(textWrapper, BorderLayout.CENTER);
        profilePanel.add(editBtnWrapper, BorderLayout.EAST);
        
        panel.add(profilePanel, BorderLayout.NORTH);

        // Main dashboard area
        JPanel dashboard = new JPanel(new GridBagLayout());
        dashboard.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] gameOptions = {"Go (9x9)", "Chess", "Tic-Tac-Toe"};
        JComboBox<String> gameSelector = new JComboBox<>(gameOptions);
        lobbyGameSelector = gameSelector; // Store reference for stats updates
        gameSelector.setBackground(BTN_COL); 
        gameSelector.setForeground(T_COL);
        gameSelector.setFont(new Font("SansSerif", Font.BOLD, 14));
        gameSelector.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Select Game Mode",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.PLAIN, 12), Color.GRAY
        ));
        
        // Update stats when game selection changes
        gameSelector.addActionListener(e -> updateLobbyStats());

        JButton queueBtn = styledButton("Find Match (Queue)");
        queueBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        queueBtn.setBackground(new Color(40, 100, 40)); 
        
        JButton createBtn = styledButton("Create Private Lobby");
        JButton lbrdBtn = styledButton("View Leaderboard");
        JButton friendsBtn = styledButton("Friends List");

        JPanel joinPanel = new JPanel(new BorderLayout(5, 0));
        joinPanel.setOpaque(false);
        JTextField lobbyIdField = new JTextField("Enter Lobby ID...");
        JButton joinBtn = styledButton("Join");
        joinPanel.add(lobbyIdField, BorderLayout.CENTER);
        joinPanel.add(joinBtn, BorderLayout.EAST);

        // Layout of components on lobby screen
        // Game Selector & Queue
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        dashboard.add(gameSelector, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        dashboard.add(queueBtn, gbc);

        // Create and join lobby
        gbc.gridx = 0; gbc.gridy = 1; 
        dashboard.add(createBtn, gbc);
        
        gbc.gridx = 1;
        dashboard.add(joinPanel, gbc);

        // Leaderboard and friends
        gbc.gridx = 0; gbc.gridy = 2; 
        dashboard.add(lbrdBtn, gbc);
        
        gbc.gridx = 1;
        dashboard.add(friendsBtn, gbc);

        panel.add(dashboard, BorderLayout.CENTER);

        // Bottom area options
        JButton logoutBtn = styledButton("Log Out");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(logoutBtn);
        panel.add(footer, BorderLayout.SOUTH);

        editProfileBtn.addActionListener(e -> showCard("PROFILE")); 

        // Stub for Friends list box, no real functionality currently
        friendsBtn.addActionListener(e -> {
            String[] mockFriends = getFriendsList();
            
            JOptionPane.showMessageDialog(panel, 
                new JList<>(mockFriends), 
                "Friends List", 
                JOptionPane.PLAIN_MESSAGE);
        });

        // Creates a matchmaker
        Matchmaker matchmaker = new Matchmaker();

        // === Task 2: Create Private Lobby ===
        // Turns the "Create Private Lobby" button into a working feature.
        // 1) Reads the currently selected game type
        // 2) Creates a private lobby via LobbyManager
        // 3) Shows the lobby ID (and pre-fills the join text box)
        createBtn.addActionListener(e -> {
            try {
                String selectedGameStr = (String) gameSelector.getSelectedItem();

                GameType gameType;
                if (selectedGameStr == null) {
                    JOptionPane.showMessageDialog(panel, "Please select a game first.");
                    return;
                } else if (selectedGameStr.toLowerCase().contains("go")) {
                    gameType = GameType.GO;
                } else if (selectedGameStr.toLowerCase().contains("chess")) {
                    gameType = GameType.CHESS;
                } else {
                    gameType = GameType.TIC_TAC_TOE; // fallback
                }

                Player host = new Player("host:" + currentUsername, currentUsername, 1200);
                String lobbyId = lobbyManager.createLobby(host, gameType);

                JOptionPane.showMessageDialog(
                    panel,
                    "Private lobby created!\n\nLobby ID: " + lobbyId + "\n\nShare this ID with your friend so they can join.",
                    "Lobby Created",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Pre-fill join field for convenience
                lobbyIdField.setText(lobbyId);
                lobbyIdField.requestFocus();
                lobbyIdField.selectAll();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to create lobby: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // === Task 3: Join Private Lobby ===
        // Reads the lobby ID, asks LobbyManager to join, and opens the right game panel.
        joinBtn.addActionListener(e -> {
            try {
                String id = lobbyIdField.getText().trim();
                if (id.isEmpty() || id.equalsIgnoreCase("Enter Lobby ID...")) {
                    JOptionPane.showMessageDialog(panel, "Please enter a lobby ID.");
                    return;
                }

                // Build a guest player using the current username
                Player guest = new Player("guest:" + currentUsername, currentUsername, 1200);

                // Attempt to join
                Lobby lobby = lobbyManager.joinLobby(id, guest);
                if (lobby == null) {
                    JOptionPane.showMessageDialog(panel, "Lobby not found or already full.");
                    return;
                }

                // Show friend selection popup for online friends
                String selectedFriend = showFriendSelectionDialog(panel);
                if (selectedFriend == null) {
                    // User cancelled, don't proceed
                    return;
                }

                // Decide which game to launch
                GameType gameType = lobby.getGameType();
                String p1 = lobby.getHost().getName();
                // Use selected friend as opponent
                String p2 = selectedFriend;

                // Remove any existing game panel to prevent duplicate recordings
                removeExistingGamePanel();

                if (gameType == GameType.GO) {
                    GoGamePanel goPanel = new GoGamePanel(p1, p2, () -> showCard("LOBBY"));
                    mainPanel.add(goPanel, "GAME");
                    goPanel.startNewGame();
                } else if (gameType == GameType.CHESS) {
                    ChessGamePanel chessPanel = new ChessGamePanel(p1, p2, () -> showCard("LOBBY"));
                    mainPanel.add(chessPanel, "GAME");
                } else if (gameType == GameType.TIC_TAC_TOE) {
                    TicTacToeGamePanel tttPanel = new TicTacToeGamePanel(p1, p2, () -> showCard("LOBBY"));
                    mainPanel.add(tttPanel, "GAME");
                } else {
                    JOptionPane.showMessageDialog(panel, "Unknown game type.");
                    return;
                }

                // Navigate to the game
                showCard("GAME");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Failed to join lobby: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        queueBtn.addActionListener(e -> {
            String selectedGameStr = (String) gameSelector.getSelectedItem();
            
            // String to enum matching for game type
            GameType gameType;
            if (selectedGameStr.startsWith("Go")) {
                gameType = GameType.GO;
            } else if (selectedGameStr.equals("Chess")) {
                gameType = GameType.CHESS;
            } else {
                gameType = GameType.TIC_TAC_TOE;
            }

            // Tic-Tac-Toe is now fully implemented

            queueBtn.setText("Queuing for " + gameType + "...");
            queueBtn.setEnabled(false);
            gameSelector.setEnabled(false);

            // Fake timer to act like it's searching since it's not really
            Timer t = new Timer(1500, evt -> {
                try {
                    // Get current player's ELO for the selected game
                    auth_logic.Player currentPlayer = PlayerData.getOrCreateByName(currentUsername);
                    int myRating = getPlayerRatingForGame(currentPlayer, gameType);
                    
                    // Create matchmaking player with correct rating
                    matchmaking.Player me = new matchmaking.Player("player:" + currentUsername, currentUsername, myRating);

                    // Find a suitable opponent from PlayerData (skill-based matching)
                    matchmaking.Player opponent = findSuitableOpponent(currentUsername, gameType, myRating);
                    
                    if (opponent == null) {
                        // No suitable opponent found
                        queueBtn.setText("Find Match (Queue)");
                        queueBtn.setEnabled(true);
                        gameSelector.setEnabled(true);
                        JOptionPane.showMessageDialog(panel, 
                            "No suitable opponent found. Please try again later.",
                            "No Match Found", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // Enqueue both players
                    matchmaker.enqueue(me, gameType);
                    matchmaker.enqueue(opponent, gameType);
                    
                    // Find the match
                    matchmaking.Match match = matchmaker.findMatch();

                    // Once a match has been setup
                    if (match != null) {
                        System.out.println("Match Created: " + match);
                        
                        queueBtn.setText("Find Match (Queue)");
                        queueBtn.setEnabled(true);
                        gameSelector.setEnabled(true);

                        String p1 = match.getPlayer1().getName();
                        String p2 = match.getPlayer2().getName();

                        // Remove any existing game panel to prevent duplicate recordings
                        removeExistingGamePanel();

                        // Launch the correct Game Panel with names
                        if (gameType == GameType.GO) {
                            // Pass names to Go Panel
                            GoGamePanel goPanel = new GoGamePanel(p1, p2, () -> showCard("LOBBY"));
                            mainPanel.add(goPanel, "GAME");
                            goPanel.startNewGame();
                        } else if (gameType == GameType.CHESS) {
                            // Pass names to Chess Panel
                            ChessGamePanel chessPanel = new ChessGamePanel(p1, p2, () -> showCard("LOBBY"));
                            mainPanel.add(chessPanel, "GAME");
                        } else if (gameType == GameType.TIC_TAC_TOE) {
                            // Pass names to Tic-Tac-Toe Panel
                            TicTacToeGamePanel tttPanel = new TicTacToeGamePanel(p1, p2, () -> showCard("LOBBY"));
                            mainPanel.add(tttPanel, "GAME");
                        }
                        
                        showCard("GAME"); 

                    } else {
                        // We shouldn't be able to reach this scenario but just in case
                        queueBtn.setText("Find Match (Queue)");
                        queueBtn.setEnabled(true);
                        gameSelector.setEnabled(true);
                        JOptionPane.showMessageDialog(panel, "No match found. Try again.");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    queueBtn.setText("Error");
                    queueBtn.setEnabled(true);
                }
            });
            t.setRepeats(false);
            t.start();
        });

        logoutBtn.addActionListener(e -> logout());
        lbrdBtn.addActionListener(e -> showCard("LEADERBOARD")); 

        return panel;
    }
}
