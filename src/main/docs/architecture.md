# Architecture Diagram

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'fontSize': '16px'}}}%%
graph TD
    Client["<b>Client</b><br/>(Web / Mobile App)"] -->|"<b>1. HTTP Request</b><br/>JSON payload"| Server["<b>Server</b><br/>(Java Application)"]

    subgraph Server_Internal [<b>Server Internals</b>]
        direction TB
        Controller["<b>Controller / Handler</b><br/>- Receives request<br/>- Parses JSON<br/>- Calls service"] --> Service["<b>Service Layer</b><br/>- Business logic<br/>- Validation<br/>- Orchestration"]
        Service --> DAO["<b>DAO Interface</b><br/>(e.g., LocationDAO)<br/>- Contract for data access"]
        DAO <--|implements| JDBC["<b>JDBC Implementation</b><br/>(JdbcLocationDAO)<br/>- Uses PreparedStatement<br/>- No SQL concatenation<br/>- Maps ResultSet to DTOs"]
    end

    JDBC -->|"<b>2. SQL over JDBC</b><br/>SELECT/INSERT/UPDATE/DELETE"| Database[(" <b>MySQL Database</b><br/>- Tables defined in mysqlSetup.sql<br/>- Contains seed data")]
    Database -->|"ResultSet"| JDBC
    JDBC -->|"DTO objects"| Service
    Service -->|"DTO / List<DTO>"| Controller
    Controller -->|"<b>3. HTTP Response</b><br/>JSON"| Client

    %% Additional annotations using node styles
    style Client fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    style Server fill:#fff3e0,stroke:#e65100,stroke-width:2px
    style Controller fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    style Service fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    style DAO fill:#fff9c4,stroke:#fbc02d,stroke-width:2px
    style JDBC fill:#ffccbc,stroke:#bf360c,stroke-width:2px
    style Database fill:#e1d5e7,stroke:#4a148c,stroke-width:2px
```