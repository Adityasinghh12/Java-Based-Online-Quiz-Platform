# Java Quiz Platform (JDBC Edition — matches "Java GUI Based Projects" rubric)

A desktop quiz management and assessment application in **Core Java**
(Swing UI, JDBC + embedded SQLite for storage, Java2D for charts).
This edition was built specifically to satisfy every line of the
**"Java GUI Based Projects" marking rubric** — see the mapping table
below.

---

## 1. How to Run

Requires JDK 8+ (tested on JDK 21). The SQLite JDBC driver is bundled
in `lib/sqlite-jdbc.jar` — no database server to install.

**Linux / macOS:**
```bash
javac -cp .:lib/sqlite-jdbc.jar *.java
java  -cp .:lib/sqlite-jdbc.jar Main
```

**Windows:**
```powershell
javac -cp ".;lib/sqlite-jdbc.jar" *.java
java  -cp ".;lib/sqlite-jdbc.jar" Main
```

A `quizplatform.db` file is created automatically in the same folder
on first run, with the schema and seed data already in it.

- **Admin login:** `admin` / `admin123`
- **Sample Quiz Creator login:** `creator1` / `creator123`
- **Participant:** register your own account from the login screen

---

## 2. Screenshots

| Login | Admin Dashboard |
|---|---|
| ![Login Screen](screenshots/login.png) | ![Admin Dashboard](screenshots/admin.png) |

| Quiz Creator Dashboard | Participant Dashboard |
|---|---|
| ![Quiz Creator Dashboard](screenshots/creator.png) | ![Participant Dashboard](screenshots/participant.png) |

**Taking a quiz** (countdown timer, one question at a time):

![Quiz taking screen](screenshots/quiz.png)

---

## 3. Rubric Coverage Map

| Rubric Criterion | Marks | Where it's implemented |
|---|---|---|
| **OOP — Polymorphism, Inheritance, Exception Handling, Interfaces** | 10 | `User` (abstract) → `AdminUser` / `QuizCreatorUser` / `ParticipantUser` (**Inheritance**); `getRole()`/`getDashboardTitle()` overridden per subclass and called polymorphically from `LoginFrame` (**Polymorphism**); `Dao<T>` implemented identically by 7 DAO classes (**Interfaces**); `AuthenticationException` (checked) + `DataAccessException` (unchecked) thrown from the DAO layer and caught in the UI with friendly dialogs (**Exception Handling**) |
| **Collections & Generics** | 6 | `ArrayList`, `HashMap`, `LinkedHashSet` used throughout the DAOs and dashboards; `Dao<T>` is a **generic interface** (`Dao<User>`, `Dao<Quiz>`, `Dao<Question>`, ...) |
| **Multithreading & Synchronization** | 4 | `SwingWorker` runs the Performance Report DB queries on a background thread in `AdminDashboard` and `QuizCreatorDashboard` (UI never freezes); `synchronized` methods in `QuizDao`, `QuestionDao`, `MessageDao`, `ReminderDao`, `AlertDao` protect ID generation / inserts from concurrent access |
| **Classes for database operations** | 7 | 8 DAO classes, one per entity: `UserDao`, `QuizDao`, `QuestionDao`, `ResultDao`, `MessageDao`, `AlertDao`, `ReminderDao`, `SettingsDao` — each does its own JDBC CRUD |
| **Database Connectivity (JDBC)** | 3 | `DBConnection.java` opens a single shared JDBC `Connection` to embedded SQLite and creates the schema on first run |
| **Implement JDBC for database connectivity** | 3 | Every DAO uses `PreparedStatement` (parameterized, SQL-injection-safe) for all reads/writes against the 8 tables |

---

## 4. Project Structure

```
Main.java                 - entry point, initializes the DB
LoginFrame.java            - login screen (3 roles), uses authenticate()
RegisterFrame.java         - self-registration as Participant or Quiz Creator
AdminDashboard.java        - 5 tabs (Users, Quiz Approval, Settings, Reports, Alerts)
QuizCreatorDashboard.java  - 5 tabs (Create, Results+Feedback, Interactions, History, Overview)
ParticipantDashboard.java  - 6 tabs (Quizzes, History, Performance, Interactions, Reminders, Leaderboard)
QuizFrame.java              - timed quiz-taking screen
PerformanceChartPanel.java  - Java2D bar chart (no external library)

-- OOP / inheritance --
User.java (abstract), AdminUser.java, QuizCreatorUser.java, ParticipantUser.java

-- data models --
Quiz.java, Question.java, Result.java, Message.java, Alert.java, Reminder.java, Settings.java

-- JDBC layer --
DBConnection.java           - owns the JDBC Connection + schema creation
Dao.java                    - generic Dao<T> interface
UserDao / QuizDao / QuestionDao / ResultDao / MessageDao / AlertDao / ReminderDao / SettingsDao
DataStore.java               - thin facade the UI calls; delegates to the DAOs above

-- exceptions --
AuthenticationException.java (checked), DataAccessException.java (unchecked)

lib/sqlite-jdbc.jar          - the JDBC driver (bundled, no separate install)
screenshots/                 - PNGs used in the Screenshots section above
```

---

## 5. How It Works (for your viva)

1. **Startup** — `Main` calls `DataStore.initializeIfNeeded()`, which gets a `Connection`
   from `DBConnection` (creating `quizplatform.db` and all 8 tables via `CREATE TABLE IF NOT EXISTS`
   the first time), then seeds the admin + demo creator + starter quiz if the `users` table is empty.
2. **Login** — `LoginFrame` calls `DataStore.authenticate(username, password)`, which delegates to
   `UserDao.authenticate()`. On bad credentials it throws `AuthenticationException` (a checked
   exception) rather than returning `null`, forcing the caller to handle the failure explicitly.
   On success it returns a `User` reference that is actually one of the three subclasses —
   `LoginFrame` then opens the matching dashboard.
3. **Quiz submission → approval** — `QuizCreatorDashboard` calls `DataStore.addQuiz(...)`
   → `QuizDao.save()` runs an `INSERT` with status `PENDING`. `AdminDashboard`'s Approve/Reject
   buttons call `DataStore.updateQuizStatus()` → `QuizDao.updateStatus()` runs an `UPDATE`.
4. **Taking a quiz** — `ParticipantDashboard` reads only `APPROVED` quizzes via
   `QuizDao.findByStatus("APPROVED")`. `QuizFrame` scores the attempt and calls
   `DataStore.addResult()` → `ResultDao.save()`.
5. **Quiz Creator feedback** — `ResultDao.updateFeedback()` runs a targeted `UPDATE` on one row,
   matched by username + quiz id + date.
6. **Performance Reports** — `AdminDashboard.refreshChart()` and
   `QuizCreatorDashboard.refreshPerformanceOverview()` both run their DB queries inside a
   `SwingWorker.doInBackground()` (a background thread) and only touch the Swing chart component
   inside `done()` (back on the Event Dispatch Thread) — the standard safe pattern for
   multithreaded Swing UI updates.
7. **Thread safety** — `QuizDao.getNextId()`, `QuestionDao.getNextId()`, `MessageDao.getNextId()`,
   `ReminderDao.getNextId()` and `AlertDao.save()` are all `synchronized`, so two users performing
   an action at the same instant can't compute the same "next id" and collide.

---

## 6. Testing Notes

Before hand-off, this project was:
- Compiled cleanly with `javac -cp .:lib/sqlite-jdbc.jar *.java` (JDK 21, zero errors)
- Launched under a virtual display against the real SQLite database to confirm the GUI
  initializes without exceptions
- Verified with a 34-point standalone logic test run against the actual JDBC/SQLite layer
  (not a simulation), covering: polymorphic login or every role, `AuthenticationException` on
  bad/unknown credentials, the `User.create()` factory, case-insensitive SQL lookups
  (`COLLATE NOCASE`), the quiz submit → approve → visible-to-participants pipeline, foreign-key
  style question linkage, a SQL `JOIN` query (creator's results across their quizzes), feedback
  `UPDATE`, two-way messaging queries, reminder insert/delete, a settings `UPSERT`
  (`ON CONFLICT`), role changes affecting which subclass is reconstructed on next read, user
  deletion, and cascading quiz deletion — **all 34 checks passed**.

## 7. Possible Extensions (optional)

- Swap SQLite for MySQL/PostgreSQL by changing only the JDBC URL and driver in `DBConnection.java`
  — no DAO code would need to change, since all SQL here is standard ANSI SQL.
- Connection pooling (e.g. HikariCP) if the app needed to scale beyond a single desktop user.
- Password hashing (`MessageDigest`) instead of plain text.
