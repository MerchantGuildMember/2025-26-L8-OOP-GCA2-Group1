package shared;

public class ServerResponse<T> {

    private String _status;
    private String _message;
    private T _data;

    public ServerResponse(String status, String message, T data) {
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status is required");
        _status  = status;
        _message = message;
        _data    = data;
    }

    // === Public API ===
    // Gets: the status string — "OK" or "ERROR"
    public String getStatus() {
        return _status;
    }

    // Gets: the human-readable message
    public String getMessage() {
        return _message;
    }

    // Gets: the data payload, which may be null for error responses
    public T getData() {
        return _data;
    }

    // Creates: a successful response carrying data
    public static <T> ServerResponse<T> ok(String message, T data) {
        return new ServerResponse<>("OK", message, data);
    }

    // Creates: an error response with no data
    public static <T> ServerResponse<T> error(String message) {
        return new ServerResponse<>("ERROR", message, null);
    }

    // Checks: whether this response represents a successful operation
    public boolean isOk() {
        return "OK".equals(_status);
    }
}