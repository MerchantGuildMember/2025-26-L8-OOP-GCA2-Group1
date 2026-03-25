package shared;

import java.util.Map;

public class ClientRequest {

    // === Fields ===
    private String _requestType;
    private Map<String, Object> _payload;

    // === Constructors ===
    // Creates: an empty ClientRequest — needed by Jackson deserialisation
    public ClientRequest() {
        _requestType = "";
        _payload     = Map.of();
    }

    // === Public API ===
    // Gets: the request type string (e.g. "GET_ALL")
    public String getRequestType() {
        return _requestType;
    }

    // Sets: the request type
    public void setRequestType(String requestType) {
        _requestType = requestType;
    }

    // Gets: the payload map
    public Map<String, Object> getPayload() {
        return _payload;
    }

    // Sets: the payload map
    public void setPayload(Map<String, Object> payload) {
        _payload = payload;
    }

    // Gets: a string value from the payload by key, or null if absent
    public String getString(String key) {
        Object v = _payload.get(key);
        return v == null ? null : v.toString();
    }

    // Gets: an integer value from the payload by key, or -1 if absent/unparseable
    public int getInt(String key) {
        Object v = _payload.get(key);
        if (v == null)
            return -1;
        try {
            return Integer.parseInt(v.toString());
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }
}
