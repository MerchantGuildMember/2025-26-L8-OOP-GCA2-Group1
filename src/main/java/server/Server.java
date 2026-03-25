package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Multithreaded TCP server for the Trail Tracker application.
 *
 * <p>Uses an {@link ExecutorService} so every connected client is handled on
 * its own thread without blocking the accept loop. The server never touches
 * the {@link ClientHandler} logic directly — it only creates handlers and
 * submits them to the pool.</p>
 *
 * @author Maryna Hordiienko
 */
public class Server {

    // === Fields ===
    private static final int PORT = 8080;
    private static final String DB_URL = System.getenv("URL");
    private static final String DB_USER = System.getenv("USER");
    private static final String DB_PASS = System.getenv("PASS");

    // === Public API ===
    // Creates: the server entry point — starts the accept loop
    public static void main(String[] args) {

        // Creates: a thread pool that grows on demand for each new client
        try (ExecutorService pool = Executors.newCachedThreadPool()) {

            System.out.println("Server starting on port " + PORT + " ...");

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                System.out.println("Server ready. Waiting for clients...\n");

                // Runs: continuously until the process is stopped
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client: " + clientSocket.getInetAddress()
                            + " — submitting to thread pool");

                    // Submits: handler to pool so the accept loop is never blocked
                    pool.submit(new ClientHandler(clientSocket, DB_URL, DB_USER, DB_PASS));
                }

            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            } finally {
                // Shuts down: pool gracefully, waiting up to 5 seconds for tasks to finish
                System.out.println("Shutting down thread pool...");
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(5, TimeUnit.SECONDS))
                        pool.shutdownNow();
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
