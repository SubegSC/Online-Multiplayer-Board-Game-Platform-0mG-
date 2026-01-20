# Test Suite Instructions

This project uses **JUnit 5** for all automated testing.  
All tests are located inside the `test/` directory and are organized by subsystem.

---

## 📁 Test Suite Structure

```
test/
 ├─ chat/
 │   ├─ ChatChannelTest.java
 │   ├─ ChatServiceStubTest.java
 │   └─ GameSessionTest.java
 ├─ chess/
 │   ├─ ChessBoardTest.java
 │   └─ ChessGameTest.java
 ├─ go/
 │   └─ GoGameTest.java
 ├─ leaderboard/
 │   ├─ AdminControlsTest.java
 │   └─ RankingAlgorithmTest.java
 ├─ matchmaking/
 │   └─ MatchmakingTest.java
 └─ TicTacToe/
     ├─ TicTacToeBoardTest.java
     └─ TicTacToeGameTest.java
```

All tests will be automatically discovered by any **JUnit 5 compatible runner**.

---

# 🧪 Running Tests in an IDE (Recommended)

These instructions work for **IntelliJ IDEA**, **VS Code**, and **Eclipse**.

1. Open the project folder (`src` and `test` should be visible).
2. Mark source roots:
   - `src/` → main source root  
   - `test/` → test source root
3. Ensure your IDE is using the project’s Java SDK (e.g., Java 17).
4. Confirm JUnit 5 is on the classpath (most IDEs auto-detect it).
5. **Run all tests**:  
   - Right‑click the `test/` folder → *Run Tests*.
6. **Run a subsystem** (e.g., chat only):  
   - Right‑click `test/chat/` → *Run Tests*.
7. **Run a single test class**:  
   - Right‑click `ChatServiceStubTest.java` → Run.

If everything passes, you will see a **green test report**.

---

# ▶️ Running Tests from the Command Line (Generic JUnit 5 Setup)

Requires:  
`junit-platform-console-standalone-x.y.z.jar`

### 1. Compile sources:

```
javac -classpath junit-platform-console-standalone-x.y.z.jar -d out ^
  src/**/*.java test/**/*.java
```

(macOS/Linux: replace `^` with `\`)

### 2. Run all tests:

```
java -jar junit-platform-console-standalone-x.y.z.jar ^
  --class-path out ^
  --scan-class-path
```

JUnit will automatically detect and execute all test classes.

---

# 📌 Notes for This Project

- No HTML/JavaScript test harness is used.
- All tests are pure JUnit 5.
- Chat tests verify:
  - Correct ordering of messages.
  - Message persistence within a match.
  - GameSession → ChatService integration.
- Other test modules verify:
  - Chess and Go logic.
  - Matchmaking correctness.
  - Leaderboard ELO updates.
  - Tic-Tac-Toe board/game mechanics.

---

# ✔️ End of Test Instructions
Place this file in the **root of the repository** as `instructions.md`.
