package client;

import java.io.*;
import java.net.Socket;

public class ServerTest {

    // === Fields ===
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private static int _passed = 0;
    private static int _failed = 0;

    // === Public API ===
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║       SERVER TEST — STARTING     ║");
        System.out.println("╚══════════════════════════════════╝\n");

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✔️ Connected to server " + HOST + ":" + PORT + "\n");

            // --- Test 1: GET_ALL_LOCATIONS ---
            testCommand(
                    out, in,
                    "GET_ALL_LOCATIONS",
                    "[", // response should start with [ (JSON array)
                    "GET_ALL_LOCATIONS returns a JSON array"
            );

            // --- Test 2: GET_LOCATION with existing ID ---
            testCommand(
                    out, in,
                    "GET_LOCATION:1",
                    "\"id\"", // response should contain id field
                    "GET_LOCATION:1 returns a JSON object"
            );

            // --- Test 3: GET_LOCATION with non-existing ID ---
            testCommand(
                    out, in,
                    "GET_LOCATION:99999",
                    "NOT_FOUND", // should return NOT_FOUND
                    "GET_LOCATION:99999 returns NOT_FOUND"
            );

            // --- Test 4: GET_ALL_TRAILS ---
            testCommand(
                    out, in,
                    "GET_ALL_TRAILS",
                    "[", // JSON array expected
                    "GET_ALL_TRAILS returns a JSON array"
            );

            // --- Test 5: GET_TRAIL with existing ID ---
            testCommand(
                    out, in,
                    "GET_TRAIL:1",
                    "\"id\"", // JSON object with id
                    "GET_TRAIL:1 returns a JSON object"
            );

            // --- Test 6: GET_TRAIL with non-existing ID ---
            testCommand(
                    out, in,
                    "GET_TRAIL:99999",
                    "NOT_FOUND",
                    "GET_TRAIL:99999 returns NOT_FOUND"
            );

            // --- Test 7: unknown command ---
            testCommand(
                    out, in,
                    "INVALID_COMMAND",
                    "ERROR", // should return ERROR
                    "Unknown command returns ERROR"
            );

            // --- Test 8: invalid ID format ---
            testCommand(
                    out, in,
                    "GET_LOCATION:abc",
                    "ERROR", // should return ERROR because abc is not a number
                    "Invalid ID format returns ERROR"
            );

        } catch (IOException e) {
            System.out.println("✘ Could not connect to server: " + e.getMessage());
            System.out.println("  Make sure Server.java is running on port " + PORT);
            return;
        }

        // --- Summary ---
        printSummary();
    }

    // === Helpers ===

    // Sends: one command, reads the response, checks if it contains expected string
    private static void testCommand(PrintWriter out,
                                    BufferedReader in,
                                    String command,
                                    String expectedContains,
                                    String testName) throws IOException {

        System.out.println("┌─ Test: " + testName);
        System.out.println("│  >>> " + command);

        // Sends: command to the server
        out.println(command);

        // Reads: response until END marker is received
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.equals("END")) {
            response.append(line).append("\n");
        }

        String result = response.toString().trim();

        // Prints: shortened response if too long
        System.out.println("│  <<< " + (result.length() > 80
                ? result.substring(0, 80) + "..."
                : result));

        // Checks: if response contains expected value
        if (result.contains(expectedContains)) {
            System.out.println("│  ✔️ PASSED — response contains: \"" + expectedContains + "\"");
            _passed++;
        } else {
            System.out.println("│  ✘ FAILED — expected: \"" + expectedContains
                    + "\", got: \"" + result + "\"");
            _failed++;
        }

        System.out.println("└─────────────────────────────────\n");
    }

    // Prints: final test summary
    private static void printSummary() {
        int total = _passed + _failed;

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║             RESULTS              ║");
        System.out.println("╠══════════════════════════════════╣");

        System.out.printf("║  Total tests:   %-15d ║%n", total);
        System.out.printf("║  ✔️ Passed:      %-15d ║%n", _passed);
        System.out.printf("║  ✘ Failed:      %-15d ║%n", _failed);

        System.out.println("╠══════════════════════════════════╣");

        if (_failed == 0) {
            System.out.println("║       ALL TESTS PASSED ✔️         ║");
        } else {
            System.out.println("║     SOME TESTS FAILED ✘          ║");
        }

        System.out.println("╚══════════════════════════════════╝");
    }
}