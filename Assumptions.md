# Assumptions — Iteration 1 (OMG)

This note is just to spell out what we had in mind while we were doing P1. The assignment tells us to design the system and show where the DB/server would go, but not everything is actually built yet. The points below explain what we are pretending is true right now so the diagrams and use cases make sense.

## 1. General scope
- For P1 we are mainly doing design: use cases, class/sequence diagrams, repo setup, team file.
- We are not trying to ship a working online platform in this iteration.
- The files in the repo (use cases, class diagram, sequence diagram) describe the system we want to end up with, even if the code is only partial at this stage.

## 2. Authentication and profiles
- Login / registration / loading a profile goes through a simple stub object, not a real database.
- We are assuming the credentials it finds are valid, so we can focus on the flow instead of on security.
- Things like password reset or email verification are out of scope for P1.

## 3. Data and persistence
- Users, profiles, match history and leaderboard entries can all live in memory or be hard-coded.
- Nothing has to survive a restart in P1.
- The reason we still show a `DatabaseStub` in the diagram is to mark the spot where a real service would plug in later.

## 4. Supported games
- The platform is meant to handle more than one board game.
- For this iteration we only plan around the three the assignment mentions (Chess, Go, Tic-Tac-Toe) so we have examples to design against.
- New games would follow the same `Game` → concrete game idea in later iterations.

## 5. Matchmaking and leaderboard
- We are assuming a simple “there are two people waiting, make a match” style of matchmaking for now.
- Proper skill / rating matching can be pushed to P2/P3.
- Leaderboard updates come from the same stubbed data source as player stats.

## 6. Chat
- Chat is tied to the current game/session only.
- We are not storing messages or doing moderation.
- A real messaging backend would replace this later.

## 7. GUI
- A small GUI or driver class is allowed just to show how pieces connect.
- We are not aiming for final layout or every screen in P1.

## 8. Environment and tools
- Code is in Java (JDK 22) with no extra libraries.
- Repo is on the UCalgary GitLab instance so TAs can see our commits.

## 9. Out of scope for P1
- Actual server hosting or networked play
- Handling lag/concurrency issues
- Security features (tokens, rate limits, etc.)
- Writing to a real database

These points are here so the TA can read the P1 docs with the same assumptions we had when we made them.