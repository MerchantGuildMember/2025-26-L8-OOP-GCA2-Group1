package server;

import DAO.DAO;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiClientServer<T> {

    // === Fields ===
    private int _port;
    private ExecutorService _pool;
    private DAO<T> _dao;

    // === Constructors ===
    // Creates: a multi-client server using a cached thread pool
    public MultiClientServer(int port, DAO<T> dao) {
        if (port < 1_024 || port > 65_535)
            throw new IllegalArgumentException("port must be 1024–65535");
        _port = port;
        _pool = Executors.newCachedThreadPool();
        _dao = dao;
    }

    // === Public API ===
    // Starts: the server accept loop — runs until the process is stopped
    public void start() throws IOException {
        System.out.println("Server starting on port " + _port);

        try (ServerSocket serverSocket = new ServerSocket(_port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();    // block until a client arrives
                System.out.println("Accepted: " + clientSocket.getInetAddress());
                _pool.submit(new ClientHandler<T>(clientSocket, _dao)); // hand off to pool
            }
        }
    }
}