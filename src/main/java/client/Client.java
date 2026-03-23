package client;

import java.io.*;
import java.net.Socket;

/**
 * Client Multithreaded Server
 *
 * @author Maryna Hordiienko
 */

public class Client {

    // === Fields ===
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    // === Public API ===

    // Creates: the client entry point and connects to the server
    // Sends: a series of test commands and prints server responses
    public static void main(String[] args) {
        System.out.println("Connecting to " + HOST + ":" + PORT + "...");

        // Opens: socket connection and communication streams
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected!\n");

            // Sends: predefined commands to test server functionality
            sendCommand(out, in, "GET_ALL_LOCATIONS");
            sendCommand(out, in, "GET_LOCATION:1");
            sendCommand(out, in, "GET_ALL_TRAILS");
            sendCommand(out, in, "GET_TRAIL:1");
            sendCommand(out, in, "UNKNOWN_COMMAND");

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    // === Helpers ===

    // Sends: one command to the server and prints the full response
    // Reads: server response until the "END" marker is received
    private static void sendCommand(PrintWriter out,
                                    BufferedReader in,
                                    String command) throws IOException {
        System.out.println(">>> Sending: " + command);

        // Sends: command to the server
        out.println(command);

        // Reads: response lines until the END marker is reached
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.equals("END")) {
            response.append(line).append("\n");
        }

        // Prints: formatted server response
        System.out.println("<<< Response:\n" + response);
        System.out.println("-------------------\n");
    }
}