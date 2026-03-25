package shared;

/**
 * Generic wrapper for all server responses.
 * Carries a status ("OK" or "ERROR"), a human-readable message,
 * and a typed data payload.
 *
 * <p>Every reply sent from the server to a client must be an instance of
 * this class serialised to JSON — raw strings and raw types are not permitted.</p>
 *
 * @param <T> the type of the data payload
 * @author Aleksy Cieslak
 */
public class ServerResponse<T> {

    // === Fields ===
    private String fStatus;
    private String fMessage;
    private T fData;

    // === Constructors ===
    // Creates: a ServerResponse with all three fields explicitly set
    public ServerResponse(String status, String message, T data) {
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status is required");
        fStatus = status;
        fMessage = (message == null) ? "" : message;
        fData = data;
    }

    // === Public API ===
    // Gets: the status string — "OK" or "ERROR"
    public String getStatus() {
        return fStatus;
    }

    // Gets: the human-readable message
    public String getMessage() {
        return fMessage;
    }

    // Gets: the typed data payload; may be null for error responses
    public T getData() {
        return fData;
    }

    // Checks: whether this response represents a successful operation
    public boolean isOk() {
        return "OK".equals(fStatus);
    }

    // Creates: a successful ServerResponse carrying a data payload
    public static <T> ServerResponse<T> ok(String message, T data) {
        return new ServerResponse<>("OK", message, data);
    }

    // Creates: an error ServerResponse with no data payload
    public static <T> ServerResponse<T> error(String message) {
        return new ServerResponse<>("ERROR", message, null);
    }

    // === Overrides ===
    @Override
    public int hashCode() {
        return java.util.Objects.hash(fStatus, fMessage);
    }

    @Override
    public String toString() {
        return "ServerResponse{status='" + fStatus
                + "', message='" + fMessage
                + "', data=" + fData + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerResponse)) return false;
        ServerResponse<?> other = (ServerResponse<?>) o;
        return java.util.Objects.equals(fStatus, other.fStatus)
                && java.util.Objects.equals(fMessage, other.fMessage);
    }
}
