# 🎯 Project Overview


This project implements a multi-game platform supporting:

	•	Chess

	•	Go

	•	Tic-Tac-Toe

	•	Matchmaking (1v1)

	•	Lobby creation & joining

	•	Basic authentication logic (prototype storage)

	•	Leaderboard & ranking logic

	•	Graphical User Interface (GUI)


The system is modularized into separate subsystems, each responsible for a distinct part of the platform.




# 🧩 Subsystem Descriptions



1. Game Logic (Chess / Go / TicTacToe)



✔ Each game module includes:

	•	Board representation

	•	Move validation

	•	Turn management

	•	Game state transitions

	•	Basic win/draw logic (advanced for Chess and Go)



✔ Chess highlights:

	•	Full piece movement rules

	•	Check & checkmate detection

	•	Castling, pawn promotion, en passant

	•	Rich JUnit test coverage (JUnit required)



✔ Go highlights:

	•	Liberties, captures, suicide detection

	•	Ko handling (basic)

	•	Pass moves and end check

	•	Exception-driven illegal move handling



✔ TicTacToe highlights:

	•	Lightweight logic

	•	Win/draw detection

	•	Validation and turn handling

	•	JUnit tests


⸻


2. Matchmaking System



Responsible for pairing players in queues by game type.



Components:


	•	Matchmaker.java

	•	Enqueue players

	•	Detect match availability

	•	Assign two players to a match

	•	Lobby.java / LobbyManager.java

	•	Create lobby

	•	Join lobby

	•	Prevent joining full lobbies

	•	Store player list and lobby ID

	•	GameType.java

Defines supported games: CHESS, GO, TIC_TAC_TOE.




⸻



3. Authentication Logic (Prototype)





Includes:

	•	Player identity class

	•	Player statistics




Used to support:

	•	Leaderboard

	•	Matchmaking




⸻



4. Leaderboard Logic



Includes:

	•	Ranking algorithm (ELO-like or point-based)

	•	Leaderboard storage format

	•	Admin controls (reset, update, visibility)

	•	Achievement sharing (prototype stage)




GUI uses this to display:

	•	Top players

	•	Player statistics

	•	Win/loss ratios



⸻



5. GUI System

Includes:

	•	MainGui.java – entry point

	•	ChessGamePanel.java

	•	GoGamePanel.java

	•	TicTacToeGamePanel.java

	•	LeaderboardPanel.java



# 🧪 Running & Compilation Instructions





IMPORTANT:



project contains JUnit test files.



Compiling everything at once without JUnit on the classpath will produce errors.


⸻



✅ Compile WITHOUT tests (recommended for running the program)

javac $(find src -name "*.java" ! -name "*Test.java")

This compiles every .java file except JUnit tests, preventing thousands of errors.

⸻


▶️ Run the GUI

java -cp src MainGui
