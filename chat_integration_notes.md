# Chat Integration Notes – Dec 3, 2025

## 1. Current matchmaking flow

- Matches are created in: `matchmaking/Matchmaker.java`
- A Match is started / game launched in: `matchmaking/XYZ.java` (method `startGame(...)`)
- Each match currently has identifier: `<how you identify a match>` (e.g., field `matchId`, or players pair)

## 2. Game startup flow

- For Chess:
  - Entry point class: `Chess_Logic/SomeController.java`
  - Called from: `<class/method>`
- For Go:
  - Entry point class: `go_logic/...`
  - Called from: `<class/method>`

## 3. Plan for chat integration

- When a match is created/started, we will:
  - Use `ChatService.openChannel(matchId)` to create a `ChatChannel`.
  - Wrap the `Match` and `ChatChannel` inside a `GameSession`.
- GUI and game controllers will later obtain:
  - `GameSession.getChatChannel()` for message history.
  - `ChatService` (via context) to send messages.

## 4. Current chat integration (Dec 3, 2025)

- Chat backend:
  - `matchmaking.AppContext` exposes a single shared `ChatService` instance via `AppContext.getChatService()`.
  - The concrete implementation is `ChatServiceStub`, which stores per-match chat history in memory.

- Game sessions:
  - `matchmaking.GameSession` wraps a `Match` together with its `ChatChannel`.
  - A `GameSession` is the unit that the GUI / game controllers should work with (instead of raw `Match`) so that chat and game state travel together.

- Controller layer:
  - `chat.ChatController` knows the `GameSession` and the shared `ChatService`.
  - It will be used by the GUI to:
    - send messages (`send(...)`),
    - retrieve history (`getHistory()`),
    - and register listeners on the underlying `ChatChannel`.

- Planned match start flow:
  - When a `Match` is successfully created / started:
    1. Obtain the shared chat service: `ChatService chatService = AppContext.getChatService();`
    2. Choose a match identifier (e.g., `String matchId = match.getGameType() + "-" + player1.getUsername() + "-" + player2.getUsername();` or a dedicated ID field).
    3. Open a chat channel: `ChatChannel channel = chatService.openChannel(matchId);`
    4. Create a `GameSession` with that `Match` and `ChatChannel`.
    5. Pass the `GameSession` to the game / GUI layer (instead of passing `Match` directly).

- Next step:
  - Wire this flow into the actual “match start” code path (currently in the matchmaking demo / future GUI controller) so that every running game automatically gets a chat channel and history.