package client;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class ServerClient {

    private final String _host;
    private final int _port;
    private final Gson _gson;

    public ServerClient(String host, int port) {
        _host = host;
        _port = port;
        _gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString()))
                .create();
    }

    public String send(String requestJson) throws IOException {
        try (Socket socket = new Socket(_host, _port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(requestJson);
            return in.readLine();
        }
    }

    public String buildAction(String action) {
        return "{\"action\":\"" + action + "\"}";
    }

    public String buildActionWithId(String action, long id) {
        return "{\"action\":\"" + action + "\",\"id\":" + id + "}";
    }

    public String buildActionWithData(String action, Object data) {
        JsonObject req = new JsonObject();
        req.addProperty("action", action);
        req.add("data", _gson.toJsonTree(data));
        return _gson.toJson(req);
    }
}