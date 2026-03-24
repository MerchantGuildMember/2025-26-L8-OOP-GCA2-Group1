package server;

import DAO.JdbcLocationDAO;
import DAO.JdbcTrailDAO;
import DAO.LocationDAO;
import DAO.TrailDAO;
import tables.Location;
import tables.Trail;
import utils.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;


/**
 * Client handler for the server.
 *
 * @author Maryna Hordiienko
 */

// Implements Runnable so this handler can be submitted to an ExecutorService
public class ClientHandler implements Runnable {

    // === Fields ===
    private final Socket _clientSocket;
    private final LocationDAO _locationDAO;
    private final TrailDAO _trailDAO;

    // === Constructors ===
    // Creates: a handler for one connected client
    public ClientHandler(Socket clientSocket, String dbUrl,
                         String dbUser, String dbPass) {
        if (clientSocket == null)
            throw new IllegalArgumentException("clientSocket is required");

        _clientSocket = clientSocket;
        _locationDAO = new JdbcLocationDAO(dbUrl, dbUser, dbPass);
        _trailDAO = new JdbcTrailDAO(dbUrl, dbUser, dbPass);
    }

    // === Public API ===
    // Runs: the client session — reads commands and sends JSON responses
    // Called by the thread pool when a thread becomes available
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Client connected: "
                + _clientSocket.getInetAddress());

        // Uses try-with-resources so the socket and streams are closed automatically
        try (Socket socket = _clientSocket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true)) {

            String command;

            // Reads commands until the client disconnects
            while ((command = in.readLine()) != null) {
                System.out.println("[" + threadName + "] Command: " + command);

                String response = processCommand(command);
                out.println(response);
                out.println("END"); // Marks the end of the response
            }

        } catch (IOException e) {
            System.out.println("[" + threadName + "] IO error: " + e.getMessage());
        }

        System.out.println("[" + threadName + "] Client disconnected.");
    }

    // === Helpers ===
    // Processes: a single command string and returns a JSON response
    private String processCommand(String command) {
        try {
            // Handles: request to retrieve all locations
            if (command.equals("GET_ALL_LOCATIONS")) {
                List<Location> locations = _locationDAO.findAll();
                return JsonUtil.listToJson(locations);
            }

            // Handles: request to retrieve one location by ID
            if (command.startsWith("GET_LOCATION:")) {
                Long id = Long.parseLong(command.split(":")[1].trim());
                return _locationDAO.findById(id)
                        .map(JsonUtil::toJson)
                        .orElse("NOT_FOUND");
            }

            // Handles: request to retrieve all trails
            if (command.equals("GET_ALL_TRAILS")) {
                List<Trail> trails = _trailDAO.findAll();
                System.out.println("TEST"+ trails.size());
                return JsonUtil.listToJson(trails);
            }

            // Handles: request to retrieve one trail by ID
            if (command.startsWith("GET_TRAIL:")) {
                Long id = Long.parseLong(command.split(":")[1].trim());
                return _trailDAO.findById(id)
                        .map(JsonUtil::toJson)
                        .orElse("NOT_FOUND");
            }

            return "ERROR: Unknown command: " + command;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}