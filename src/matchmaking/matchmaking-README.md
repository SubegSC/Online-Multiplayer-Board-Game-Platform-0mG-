Matchmaking and Lobby Design

The matchmaking subsystem is implemented in the matchmaking package and is responsible for pairing players into games (UC4 – Queue for Match) and supporting friend lobbies (UC5 – Create Lobby and UC6 – Join Lobby). This module is self-contained and does not depend on the chess, Go, GUI, or auth logic, which makes it easy to integrate from different parts of the system.

Matchmaking (UC4 – Queue for Match)

The Matchmaker class manages a separate queue of players for each GameType (CHESS, GO, TIC_TAC_TOE). Each player is represented by a simple Player object (containing an ID, display name, and rating), and an internal QueuedPlayer structure tracks their rating, game type, and time of enqueue.

	•	When a player chooses a game and clicks “Play / Find Match”, the system calls:

    matchmaker.enqueue(player, gameType);
    which adds them to the appropriate queue if they are not already queued.

	•	If a player cancels matchmaking, the system calls:
    matchmaker.dequeue(player);
    which removes them from all queues (UC4 alternative: “Player cancels → remove and return to Lobby”).

	•	Periodically (or when a new player joins), the system calls:
    Match match = matchmaker.findMatch();
    findMatch() scans each game’s queue, sorts players by rating, and selects the closest-rated adjacent pair within a configurable rating threshold. If a suitable pair is found, both players are removed from the queue and returned as a Match object containing the GameType and the two Players. Higher layers (e.g., game/session manager) can then create the actual ChessGame or GoGame instance for that match.

	•	To support the UC4 alternative of showing an estimated wait time, the method:
    int etaSeconds = matchmaker.estimateWait(player);
    computes a rough ETA based on the player’s position in the queue (number of players ahead translated into a number of expected matching “rounds”).

    Lobbies (UC5 – Create Lobby, UC6 – Join Lobby)

    Friend lobbies are represented by the Lobby class and managed by the LobbyManager class.
        •	When a host wants to create a lobby for a specific game (UC5), the system calls:
        String lobbyId = lobbyManager.createLobby(hostPlayer, gameType);
        This generates a unique short lobbyId, creates a new Lobby with that ID, the host, and the chosen GameType, and stores it internally. The returned ID can be shown to the host or shared with friends.

	•	A second player can join using the lobby code (UC6) by calling:
    Lobby lobby = lobbyManager.joinLobby(lobbyId, joiningPlayer);
    joinLobby checks that the lobby exists, is not full, and does not already contain that player. If the join is valid, the player is added and the lobby status moves to FULL (since we only support 1v1 games). If the lobby is missing, full, or otherwise invalid, joinLobby returns null, corresponding to alternative flows where the lobby is unavailable.

	•	The Lobby class itself tracks:
	•	id (join code),
	•	gameType,
	•	the host player,
	•	a list of participants,
	•	and a simple Status (WAITING or FULL).
This makes it easy for the GUI or session manager to inspect who is in a lobby and which game it is for.

Overall, the matchmaking module provides a clean API:
	•	enqueue, dequeue, findMatch, and estimateWait for ranked queue matchmaking, and
	•	createLobby and joinLobby for direct friend lobbies.

Other subsystems (auth, leaderboard, GUI, and game logic) interact only by passing Player objects and GameType values into these methods and then using the resulting Match or Lobby to construct and display the actual game sessions.