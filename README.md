# Trail Tracker — GCA2 Group 1

## Domain Overview

Trail Tracker is a multi-user client-server application for managing hiking trails in Ireland. The system stores information about locations, route stops, trails, and trail media (images, audio, video). Users can connect to a central server to view, add, update, and delete trail records, and upload binary media files associated with each trail.

**Entities:**
- **Location** — GPS coordinates and address of a point of interest
- **RouteStop** — A named stop on a route, linked to a Location
- **Trail** — A named hiking trail with difficulty rating and a list of RouteStops
- **TrailMedia** — Media files (IMAGE, VIDEO, AUDIO) attached to a trail, including binary BLOB storage

---

## Group Members

| Name | Git Hub users name  |
|---|---------------------|
| Maryna Hordiienko | ms_maryna           |
| Aleksy Cieslak | MerchantGuildMember |

---

## How to Run

### Prerequisites
- Java 21+
- Maven
- MySQL / MariaDB (XAMPP recommended)

### Step 1 — Set up the database
Open phpMyAdmin or a MySQL terminal and run:
```
src/main/sql/mysqlSetup.sql
```
This creates the `oop_gca2` database, user, tables, and seed data from scratch.

### Step 2 — Set environment variables
```
URL=jdbc:mysql://localhost:3306/oop_gca2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
USER=oop_gca2_user
PASS=one
```
In IntelliJ: Run → Edit Configurations → Environment Variables

### Step 3 — Start the server
Run `server.Server` — listens on port 8080.

### Step 4 — Start the client
Run `client.Client` — connects to localhost:8080 and presents a menu.

### Step 5 — Run tests
Right-click `TrailMediaDaoTest` or `ServerIntegrationTest` → Run with Coverage.
Note: `ServerIntegrationTest` requires the server to be running first.

---

## Architecture Summary

```
Client (client.Client)
    │
    │  JSON over TCP socket (port 8080)
    ▼
Server (server.Server)
    │  ExecutorService — one thread per client
    ▼
ClientHandler (server.ClientHandler)
    │  dispatches by "action" field
    ▼
DAO Layer (DAO.JdbcXxxDAO)
    │  JDBC + PreparedStatement
    ▼
MySQL Database (oop_gca2)
```

All client-server communication uses newline-delimited JSON. Every request carries an `action` field. Every response is a `ServerResponse<T>` JSON object.

---

## JSON Communication Protocol

### ServerResponse shape
```json
{
  "fStatus":  "OK" | "ERROR",
  "fMessage": "human-readable message",
  "fData":    "<typed payload or null>"
}
```

### Location Requests

| Action | Payload | Response data |
|---|---|---|
| `GET_ALL_LOCATIONS` | none | `ArrayList<Location>` |
| `GET_LOCATION_BY_ID` | `"id": 1` | `Location` |
| `ADD_LOCATION` | `"data": {...}` | `Location` with generated id |
| `UPDATE_LOCATION` | `"data": {...}` | `Location` |
| `DELETE_LOCATION` | `"id": 1` | `null` |

### Trail Requests

| Action | Payload | Response data |
|---|---|---|
| `GET_ALL_TRAILS` | none | `ArrayList<Trail>` |
| `GET_TRAIL_BY_ID` | `"id": 1` | `Trail` |
| `ADD_TRAIL` | `"data": {...}` | `Trail` with generated id |
| `UPDATE_TRAIL` | `"data": {...}` | `Trail` |
| `DELETE_TRAIL` | `"id": 1` | `null` |

### RouteStop Requests

| Action | Payload | Response data |
|---|---|---|
| `GET_ALL_ROUTESTOPS` | none | `ArrayList<RouteStop>` |
| `GET_ROUTESTOP_BY_ID` | `"id": 1` | `RouteStop` |
| `ADD_ROUTESTOP` | `"data": {...}` | `RouteStop` with generated id |
| `UPDATE_ROUTESTOP` | `"data": {...}` | `RouteStop` |
| `DELETE_ROUTESTOP` | `"id": 1` | `null` |

### TrailMedia Requests

| Action | Payload | Response data |
|---|---|---|
| `GET_ALL_TRAILMEDIA` | none | `ArrayList<TrailMedia>` |
| `GET_TRAILMEDIA_BY_ID` | `"id": 1` | `TrailMedia` |
| `ADD_TRAILMEDIA` | `"data": {...}` | `TrailMedia` with generated id |
| `UPDATE_TRAILMEDIA` | `"data": {...}` | `TrailMedia` |
| `DELETE_TRAILMEDIA` | `"id": 1` | `null` |

### Binary File Requests (F18–F20)

| Action | Payload | Response data |
|---|---|---|
| `UPLOAD_FILE` | `trailId, mediaType, fileName, contentType, fileSize, fileData (Base64)` | `TrailMedia` with generated id |
| `GET_FILE` | `"id": 1` | `JsonObject` with `fileName, contentType, fileSize, fileData (Base64)` |
| `GET_METADATA` | `"id": 1` | `JsonObject` with `fileName, contentType, fileSize` — no BLOB |

### Lifecycle

| Action | Description |
|---|---|
| `DISCONNECT` | Client signals clean exit; server logs disconnection and releases thread |

---

## Design Pattern Justification

### 1. DAO Pattern (Data Access Object)
Applied throughout: `LocationDAO`, `TrailDAO`, `RouteStopDAO`, `TrailMediaDAO` interfaces with `JdbcXxxDAO` implementations.

**Why:** Separates database logic from business logic. The server depends on the interface, not the JDBC implementation. This means the database can be swapped (e.g. to PostgreSQL) without touching the server or client code. It also makes unit testing possible — each DAO can be tested independently.

### 2. Generic ServerResponse\<T\>
`ServerResponse<T>` wraps every server reply with `status`, `message`, and typed `data`.

**Why:** Without a standard envelope, the client cannot reliably distinguish success from failure. `ServerResponse<T>` gives every response a consistent shape regardless of entity type or operation. It also eliminates raw types throughout the codebase.

### 3. Strategy Pattern (Predicate\<T\> filtering — F8)
`findByFilter(Predicate<T> filter)` in the DAO interface accepts a lambda as an interchangeable filtering strategy.

**Why:** Instead of writing a separate SQL query for every possible filter condition, the caller passes a lambda that is applied in-memory. This keeps the DAO interface clean and avoids SQL string concatenation.

### 4. Thread Pool (ExecutorService)
`Server.java` uses `Executors.newCachedThreadPool()` to handle each client on a separate thread.

**Why:** A single-threaded server would block on one slow client and prevent others from connecting. The thread pool allows multiple simultaneous clients without the overhead of creating a new thread object for every connection.

---

## Binary File Handling Description (F17–F20)

### Schema extension (F17)
The `trail_media` table has been extended with:
```sql
file_name     VARCHAR(255) NOT NULL DEFAULT '',
content_type  VARCHAR(100) NOT NULL DEFAULT '',
file_size     INT          NOT NULL DEFAULT 0,
file_data     LONGBLOB
```
`LONGBLOB` stores up to 4 GB — sufficient for images, audio clips, and documents. Metadata columns allow listing files without loading binary data.

### Upload flow (F18)
1. Client reads a file from disk using `Files.readAllBytes()`
2. Client Base64-encodes the bytes — JSON is text-only and cannot carry raw binary
3. Client sends `UPLOAD_FILE` request with Base64 string and metadata fields
4. Server decodes Base64 back to `byte[]`
5. Server stores bytes using `PreparedStatement.setBinaryStream()`
6. Server returns `ServerResponse<TrailMedia>` with the auto-generated ID

### Retrieval flow (F19)
1. Client sends `GET_FILE` request with the record ID
2. Server fetches BLOB using `ResultSet.getBytes()`
3. Server Base64-encodes the bytes for safe JSON transport
4. Client decodes Base64 back to `byte[]`
5. Client writes the file to disk preserving the original filename and extension

### Metadata-only query (F20)
1. Client sends `GET_METADATA` with a record ID
2. Server executes a SELECT that explicitly omits the `file_data` column
3. No binary data is loaded into memory or sent over the socket
4. Server returns `fileName`, `contentType`, and `fileSize` only

---

## Test Coverage Evidence (F23–F24)

### Test files

| File | Location | Tests | Author |
|---|---|---|---|
| `TrailMediaDaoTest` | `src/test/java/test/` | 5 (DAO baseline) | Maryna Hordiienko |
| `ServerIntegrationTest` | `src/test/java/test/` | 8 (server scenarios) | Maryna Hordiienko |
| `TrailMediaTest` | `src/test/java/tables/` | 2 | Aleksy Cieslak |
| `LocationTest` | `src/test/java/tables/` | 2 | Aleksy Cieslak |
| `RouteStopTest` | `src/test/java/tables/` | 1 | Aleksy Cieslak |
| `TrailTest` | `src/test/java/tables/` | 2 | Aleksy Cieslak |
| `JsonUtilTest` | `src/test/java/utils/` | 2 | Aleksy Cieslak |

### Coverage results (F24)
IntelliJ IDEA coverage runner — full suite results:

| Package | Line % |
|---|---|
| DAO | 93% |
| tables | 73% |
| test | 100% |

Screenshot committed to `/reports/coverage.png`.

### Test categories covered (F23)
- DAO read methods: `displayAll`, `displayById` ✅
- Insert with auto-generated ID verified ✅
- Update ✅
- Delete — success and not-found cases ✅
- Filter with Predicate ✅
- JSON serialisation/deserialisation round-trip ✅
- JSON list round-trip ✅
- Binary file upload and retrieval — bytes verified exactly ✅
- Metadata-only query without BLOB ✅
- Base64 encode/decode round-trip ✅
- Server request/response scenario ✅
- Validation edge cases (zero ID, null BLOB) ✅

---

## Unit Testing — Stage 3 Summary (F22)

Test class: `TrailMediaDaoTest` — author: Maryna Hordiienko

| Test | Category |
|---|---|
| `displayAll_returnsNonEmptyList_whenSeedDataExists` | DAO read |
| `insert_returnsEntityWithGeneratedId_whenValidMediaProvided` | Insert + ID |
| `displayById_returnsCorrectEntity_whenIdExists` | DAO read |
| `displayById_returnsErrorResponse_whenIdDoesNotExist` | Error handling |
| `trailMedia_jsonRoundTrip_preservesAllFields` | JSON round-trip |

All 5 tests pass. `@BeforeEach` used for test independence. Tests clean up inserted rows after each test.

---

## Binary File Handling — Stage 3 Summary (F17–F20)

See **Binary File Handling Description** section above for full detail.

---

## Contribution Matrix

| Feature | Primary Author | Reviewer | Effort (hrs) | Notes |
|---|---|---|---|---|
| F1 — Entity & DB Setup | Aleksy Cieslak | Maryna Hordiienko | 4 | Location, RouteStop, Trail by Aleksy; TrailMedia by Maryna |
| F2 — DAO Interface & JDBC | Aleksy Cieslak | Maryna Hordiienko | 5 | Generic DAO interface by Aleksy |
| F3 — Get All | Aleksy Cieslak | Maryna Hordiienko | 2 | |
| F4 — Get by ID | Aleksy Cieslak | Maryna Hordiienko | 2 | |
| F5 — Delete by ID | Aleksy Cieslak | Maryna Hordiienko | 1 | |
| F6 — Insert Entity | Aleksy Cieslak | Maryna Hordiienko | 2 | |
| F7 — Update Entity | Aleksy Cieslak | Maryna Hordiienko | 2 | |
| F8 — Filter with Predicate | Aleksy Cieslak | Maryna Hordiienko | 1 | Lambda-based filtering |
| F9 — JSON Conversion | Aleksy Cieslak | Maryna Hordiienko | 2 | JsonUtil helper |
| F10 — Multithreaded Server | Maryna Hordiienko | Aleksy Cieslak | 3 | ExecutorService, Server.java |
| F11 — ServerResponse\<T\> | Aleksy Cieslak | Maryna Hordiienko | 2 | Generic wrapper |
| F12 — Display All/By ID | Maryna Hordiienko | Aleksy Cieslak | 2 | ClientHandler dispatch |
| F13 — Add Entity | Maryna Hordiienko | Aleksy Cieslak | 2 | |
| F14 — Delete Entity | Maryna Hordiienko | Aleksy Cieslak | 1 | |
| F15 — Update Entity | Maryna Hordiienko | Aleksy Cieslak | 1 | |
| F16 — Error Handling | Maryna Hordiienko | Aleksy Cieslak | 2 | Try-catch in dispatch |
| F17 — Binary Schema | Maryna Hordiienko | Aleksy Cieslak | 3 | MEDIUMBLOB + metadata columns |
| F18 — Binary Upload | Maryna Hordiienko | Aleksy Cieslak | 4 | Base64 + setBinaryStream |
| F19 — Binary Retrieval | Maryna Hordiienko | Aleksy Cieslak | 3 | getBytes + Base64 decode |
| F20 — Metadata Query | Maryna Hordiienko | Aleksy Cieslak | 2 | No BLOB in SELECT |
| F21 — Disconnect | Aleksy Cieslak | Maryna Hordiienko | 1 | DISCONNECT case |
| F22 — Core Unit Tests | Maryna Hordiienko | Aleksy Cieslak | 3 | TrailMediaDaoTest — 5 tests |
| F23 — Extended Tests | Maryna Hordiienko | Aleksy Cieslak | 5 | 15 + 7 tests, BLOB round-trip |
| F24 — Coverage | Maryna Hordiienko | Aleksy Cieslak | 1 | 93% DAO line coverage |
| Architecture Diagram | Aleksy Cieslak | Maryna Hordiienko | 2 | |
| README | Maryna Hordiienko | Aleksy Cieslak | 2 | |
| Screencast | Both | — | 3 | |

---

## Harvard References

Bauer, C. and King, G. (2005) *Hibernate in Action*. Greenwich: Manning Publications.

Bloch, J. (2018) *Effective Java*. 3rd edn. Boston: Addison-Wesley.

Gamma, E., Helm, R., Johnson, R. and Vlissides, J. (1994) *Design Patterns: Elements of Reusable Object-Oriented Software*. Boston: Addison-Wesley.

Google (2024) *Gson User Guide*. Available at: https://github.com/google/gson/blob/main/UserGuide.md (Accessed: 8 May 2026).

Oracle (2024) *JDBC API Documentation*. Available at: https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/package-summary.html (Accessed: 8 May 2026).

Oracle (2024) *ExecutorService (Java SE 21)*. Available at: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html (Accessed: 8 May 2026).

JUnit Team (2024) *JUnit 5 User Guide*. Available at: https://junit.org/junit5/docs/current/user-guide/ (Accessed: 8 May 2026).

MySQL (2024) *MySQL 8.0 Reference Manual — BLOB and TEXT Types*. Available at: https://dev.mysql.com/doc/refman/8.0/en/blob.html (Accessed: 8 May 2026).

RFC 4648 (2006) *The Base16, Base32, and Base64 Data Encodings*. Available at: https://www.rfc-editor.org/rfc/rfc4648 (Accessed: 8 May 2026).

Anthropic (2025) *Claude AI* [Large language model]. Available at: https://claude.ai (Accessed: 23 April 2026). Used for: code generation assistance, debugging guidance, and English drafting of documentation.
