package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Multithreaded Server
 *
 * @author Maryna Hordiienko
 */

public class Server {

    // === Fields ===
    private static final int PORT = 8080;
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/oop_gca2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "oop_gca2";
    private static final String DB_PASS = "one";

    // === Public API ===

    // Creates: the server entry point and starts listening for client connections
    // Uses: ExecutorService to handle each client on a separate thread
    public static void main(String[] args) {

        // Creates: a thread pool that grows dynamically for client connections
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println("Starting server on port " + PORT + "...");

        // Opens: server socket and listens for incoming connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server started. Waiting for clients...\n");

            // Runs: continuously accepts new client connections
            while (true) {

                // Accepts: a new client connection (blocks until a client connects)
                Socket clientSocket = serverSocket.accept();

                System.out.println("New client: " + clientSocket.getInetAddress()
                        + " — submitting to thread pool");

                // Creates: a handler for the connected client
                ClientHandler handler = new ClientHandler(
                        clientSocket, DB_URL, DB_USER, DB_PASS
                );

                // Submits: the handler to the thread pool for concurrent execution
                // Allows: the server to continue accepting new clients without blocking
                pool.submit(handler);
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {

            // Shuts down: the thread pool gracefully
            System.out.println("Shutting down thread pool...");
            pool.shutdown();

            try {
                // Waits: for running tasks to finish before forcing shutdown
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                // Handles: interruption during shutdown
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}