package server;

import DAO.DAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import shared.ClientRequest;
import shared.ServerResponse;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Optional;

public class ClientHandler<T> implements Runnable {

    // === Fields ===
    private Socket _socket;
    private DAO<T> _dao;
    private ObjectMapper _mapper;

    // === Constructors ===
    // Creates: a handler for the given socket, using the provided DAO
    public ClientHandler(Socket socket, DAO<T> dao) {
        if (socket == null)
            throw new IllegalArgumentException("socket is required");
        if (dao == null)
            throw new IllegalArgumentException("dao is required");

        _socket = socket;
        _dao    = dao;
        _mapper = new ObjectMapper();
    }

    // === Public API ===
    // Runs: the client session — reads JSON requests and writes JSON responses
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
             PrintWriter out  = new PrintWriter(_socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                String response = handle(line);
                out.println(response);
            }
        }
        catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
        finally {
            try {
                _socket.close();
            }
            catch (IOException ignored) {
            }
        }
    }

    // === Helpers ===
    // Handles: a single raw JSON request line — returns a JSON response string
    private String handle(String rawJson) {
        try {
            ClientRequest req = _mapper.readValue(rawJson, ClientRequest.class);
            ServerResponse<?> response = dispatch(req);
            return _mapper.writeValueAsString(response);
        }
        catch (Exception e) {
            return toErrorJson("malformed request: " + e.getMessage());
        }
    }

    // Dispatches: the request to the correct DAO method based on requestType
    private ServerResponse<?> dispatch(ClientRequest req) throws Exception {
        String type = req.getRequestType();

        if ("GET_ALL".equals(type)) {
            List<?> list = _dao.findAll();
            return ServerResponse.ok("retrieved " + list.size() + " entities", list);
        }

        if ("GET_BY_ID".equals(type)) {
            long id = req.getInt("id");
            Optional<?> task = _dao.findById(id);
            if (task.isEmpty())
                return ServerResponse.error("no task with id=" + id);
            return ServerResponse.ok("task found", task.get());
        }

        if ("INSERT".equals(type)) {
            String entityJson = req.getString("entity");  // client sends the full entity as JSON
            T entity = _dao.entFromJson(entityJson);
            T inserted = _dao.insert(entity);
            return ServerResponse.ok("entity created", inserted);
        }

        if ("DELETE".equals(type)) {
            long id = req.getInt("id");
            boolean deleted = _dao.deleteById(id);
            if (!deleted)
                return ServerResponse.error("no task with id=" + id);
            return ServerResponse.ok("task deleted", null);
        }

        if ("DISCONNECT".equals(type)) {
            return ServerResponse.ok("goodbye", null);
        }

        return ServerResponse.error("unknown request type: " + type);
    }

    // Converts: an error message to a raw JSON error response string (fallback)
    private String toErrorJson(String message) {
        return "{\"status\":\"ERROR\",\"message\":\"" + message + "\",\"data\":null}";
    }
}