# Trail Tracker — Stage 2 Protocol Documentation

## JSON Communication Protocol

All client-server communication uses newline-delimited JSON.  
Every request is a single JSON object sent by the client.  
Every response is a `ServerResponse<T>` JSON object sent by the server.

### ServerResponse shape

```json
{
  "fStatus":  "OK" | "ERROR",
  "fMessage": "human-readable message",
  "fData":    <typed payload "or" null>
}
```

`fStatus` is always present. `fData` is `null` for error responses and for
delete/disconnect responses where no entity is returned.

---

## Location Requests

| Action | Request JSON | Response `fData` type |
|---|---|---|
| `GET_ALL_LOCATIONS` | `{"action":"GET_ALL_LOCATIONS"}` | `ArrayList<Location>` |
| `GET_LOCATION_BY_ID` | `{"action":"GET_LOCATION_BY_ID","id":1}` | `Location` |
| `ADD_LOCATION` | `{"action":"ADD_LOCATION","data":{...Location fields...}}` | `Location` (with generated id) |
| `UPDATE_LOCATION` | `{"action":"UPDATE_LOCATION","data":{...Location fields including id...}}` | `Location` |
| `DELETE_LOCATION` | `{"action":"DELETE_LOCATION","id":1}` | `null` |

### ADD_LOCATION example
Request:
```json
{"action":"ADD_LOCATION","data":{"latitude":53.35,"longitude":-6.26,"full_address":"Dublin, Ireland","created_at":"2026-03-24T10:00:00"}}
```
Success response:
```json
{"fStatus":"OK","fMessage":"Location added","fData":{"id":11,"latitude":53.35,"longitude":-6.26,"full_address":"Dublin, Ireland","created_at":"2026-03-24T10:00:00"}}
```
Error response:
```json
{"fStatus":"ERROR","fMessage":"Server error: ...","fData":null}
```

---

## Trail Requests

| Action | Request JSON | Response `fData` type |
|---|---|---|
| `GET_ALL_TRAILS` | `{"action":"GET_ALL_TRAILS"}` | `ArrayList<Trail>` |
| `GET_TRAIL_BY_ID` | `{"action":"GET_TRAIL_BY_ID","id":1}` | `Trail` |
| `ADD_TRAIL` | `{"action":"ADD_TRAIL","data":{...Trail fields...}}` | `Trail` (with generated id) |
| `UPDATE_TRAIL` | `{"action":"UPDATE_TRAIL","data":{...Trail fields including id...}}` | `Trail` |
| `DELETE_TRAIL` | `{"action":"DELETE_TRAIL","id":1}` | `null` |

---

## RouteStop Requests

| Action | Request JSON | Response `fData` type |
|---|---|---|
| `GET_ALL_ROUTESTOPS` | `{"action":"GET_ALL_ROUTESTOPS"}` | `ArrayList<RouteStop>` |
| `GET_ROUTESTOP_BY_ID` | `{"action":"GET_ROUTESTOP_BY_ID","id":1}` | `RouteStop` |
| `ADD_ROUTESTOP` | `{"action":"ADD_ROUTESTOP","data":{...RouteStop fields...}}` | `RouteStop` (with generated id) |
| `UPDATE_ROUTESTOP` | `{"action":"UPDATE_ROUTESTOP","data":{...RouteStop fields including id...}}` | `RouteStop` |
| `DELETE_ROUTESTOP` | `{"action":"DELETE_ROUTESTOP","id":1}` | `null` |

---

## TrailMedia Requests

| Action | Request JSON | Response `fData` type |
|---|---|---|
| `GET_ALL_TRAILMEDIA` | `{"action":"GET_ALL_TRAILMEDIA"}` | `ArrayList<TrailMedia>` |
| `GET_TRAILMEDIA_BY_ID` | `{"action":"GET_TRAILMEDIA_BY_ID","id":1}` | `TrailMedia` |
| `ADD_TRAILMEDIA` | `{"action":"ADD_TRAILMEDIA","data":{...TrailMedia fields...}}` | `TrailMedia` (with generated id) |
| `UPDATE_TRAILMEDIA` | `{"action":"UPDATE_TRAILMEDIA","data":{...TrailMedia fields including id...}}` | `TrailMedia` |
| `DELETE_TRAILMEDIA` | `{"action":"DELETE_TRAILMEDIA","id":1}` | `null` |

---

## Lifecycle

| Action | Request JSON | Description |
|---|---|---|
| `DISCONNECT` | `{"action":"DISCONNECT"}` | Client signals clean exit; server logs disconnection and releases the thread |

---

## Error Handling (F16)

- All exceptions on the server are caught inside `ClientHandler.dispatch()`.
- They are **never** propagated to the client as stack traces.
- Every failure returns `{"fStatus":"ERROR","fMessage":"...","fData":null}`.
- Invalid action names return `ERROR: Unknown action: <name>`.
- Invalid or missing IDs (null, ≤ 0) are caught in the DAO before any SQL is executed.

---

## Threading (F10)

The server uses `Executors.newCachedThreadPool()`.  
Each accepted `Socket` is immediately wrapped in a `ClientHandler` and submitted
to the pool, so the accept loop is never blocked by a slow client.  
The pool shuts down gracefully (5-second timeout) when the server process exits.
