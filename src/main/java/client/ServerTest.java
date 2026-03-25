package client;

import DAO.JdbcLocationDAO;
import DAO.JdbcRouteStopDAO;
import DAO.JdbcTrailDAO;
import DAO.JdbcTrailMediaDAO;
import DAO.LocationDAO;
import DAO.RouteStopDAO;
import DAO.TrailDAO;
import DAO.TrailMediaDAO;
import shared.ServerResponse;
import tables.Location;
import tables.RouteStop;
import tables.Trail;
import tables.TrailMedia;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Smoke test for all four DAOs using the updated ServerResponse API.
 *
 * <p>Tests displayAll() and displayById() for Location, RouteStop, Trail,
 * and TrailMedia. Also tests insert(), update(), and deleteById() for
 * Location to verify full CRUD works end-to-end.</p>
 *
 * Run this BEFORE starting the server to verify the database connection
 * and DAO layer are working correctly.
 *
 * @author smoke test
 */
public class ServerTest {

    // === Fields ===
    private static final String URL  = System.getenv("URL");
    private static final String USER = System.getenv("USER");
    private static final String PASS = System.getenv("PASS");

    private static int fPassed = 0;
    private static int fFailed = 0;

    // === Public API ===
    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         DB SERVER TEST — START        ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        // ── 1. DB Connection ────────────────────────────────────────────
        testDbConnection();

        // ── 2. Location DAO ─────────────────────────────────────────────
        testLocationDAO();

        // ── 3. RouteStop DAO ────────────────────────────────────────────
        testRouteStopDAO();

        // ── 4. Trail DAO ────────────────────────────────────────────────
        testTrailDAO();

        // ── 5. TrailMedia DAO ───────────────────────────────────────────
        testTrailMediaDAO();

        // ── Summary ─────────────────────────────────────────────────────
        printSummary();
    }

    // === Helpers ===

    // Tests: raw JDBC connection using SELECT 1 — same as the original DbSmokeTest
    private static void testDbConnection() {
        System.out.println("━━━ 1. Database Connection ━━━");
        try {
            java.sql.Connection c = java.sql.DriverManager.getConnection(URL, USER, PASS);
            java.sql.PreparedStatement ps = c.prepareStatement("SELECT 1");
            java.sql.ResultSet rs = ps.executeQuery();
            rs.next();
            int val = rs.getInt(1);
            c.close();
            pass("SELECT 1 -> " + val);
        } catch (Exception e) {
            fail("DB connection failed: " + e.getMessage());
        }
        System.out.println();
    }

    // Tests: LocationDAO — displayAll, displayById, insert, update, deleteById
    private static void testLocationDAO() throws Exception {
        System.out.println("━━━ 2. LocationDAO ━━━");
        LocationDAO dao = new JdbcLocationDAO(URL, USER, PASS);

        // displayAll
        ServerResponse<ArrayList<Location>> allResp = dao.displayAll();
        check("displayAll() status is Success",
                "Success".equals(allResp.getStatus()));
        check("displayAll() returns data",
                allResp.getData() != null && !allResp.getData().isEmpty());

        int countBefore = allResp.getData() != null ? allResp.getData().size() : 0;
        System.out.println("  Found " + countBefore + " locations in DB");

        // insert
        Location newLoc = new Location(null, 53.3497, -6.2603,
                "Smoke Test Location", LocalDateTime.now());
        Location inserted = dao.insert(newLoc);
        check("insert() returns object with generated id",
                inserted.getId() != null && inserted.getId() > 0);
        System.out.println("  Inserted location with id=" + inserted.getId());
        // displayById — found
        ServerResponse<Location> byIdResp = dao.displayById(inserted.getId());
        check("displayById() status is Success",
                "Success".equals(byIdResp.getStatus()));
        check("displayById() returns correct address",
                byIdResp.getData() != null &&
                        "Smoke Test Location".equals(byIdResp.getData().getFullAddress()));

        // displayById — not found
        ServerResponse<Location> notFoundResp = dao.displayById(999999L);
        check("displayById(999999) status is Error",
                "Error".equals(notFoundResp.getStatus()));
        check("displayById(999999) data is null",
                notFoundResp.getData() == null);

        // displayById — invalid id
        ServerResponse<Location> invalidResp = dao.displayById(-1L);
        check("displayById(-1) status is Error",
                "Error".equals(invalidResp.getStatus()));

        // update
        inserted.setFullAddress("Updated Smoke Test Location");
        Location updated = dao.update(inserted);
        check("update() returns object with updated address",
                "Updated Smoke Test Location".equals(updated.getFullAddress()));

        // deleteById
        boolean deleted = dao.deleteById(inserted.getId());
        check("deleteById() returns true", deleted);

        // confirm deleted
        ServerResponse<Location> afterDelete = dao.displayById(inserted.getId());
        check("displayById() after delete returns Error",
                "Error".equals(afterDelete.getStatus()));

        // entToJson / entFromJson
        Location sample = new Location(1L, 53.34, -6.26,
                "Test JSON Location", LocalDateTime.now());
        String json = dao.entToJson(sample);
        check("entToJson() returns non-empty string",
                json != null && !json.isBlank());
        Location fromJson = dao.entFromJson(json);
        check("entFromJson() returns object with correct address",
                fromJson != null &&
                        "Test JSON Location".equals(fromJson.getFullAddress()));

        System.out.println();
    }

    // Tests: RouteStopDAO — displayAll and displayById
    private static void testRouteStopDAO() throws Exception {
        System.out.println("━━━ 3. RouteStopDAO ━━━");
        RouteStopDAO dao = new JdbcRouteStopDAO(URL, USER, PASS);

        // displayAll
        ServerResponse<ArrayList<RouteStop>> allResp = dao.displayAll();
        check("displayAll() status is Success",
                "Success".equals(allResp.getStatus()));
        check("displayAll() returns data",
                allResp.getData() != null);

        int count = allResp.getData() != null ? allResp.getData().size() : 0;
        System.out.println("  Found " + count + " route stops in DB");

        // displayById — first existing record
        if (count > 0) {
            Long firstId = allResp.getData().get(0).getId();
            ServerResponse<RouteStop> byIdResp = dao.displayById(firstId);
            check("displayById(" + firstId + ") status is Success",
                    "Success".equals(byIdResp.getStatus()));
            check("displayById(" + firstId + ") data is not null",
                    byIdResp.getData() != null);
        }

        // displayById — not found
        ServerResponse<RouteStop> notFound = dao.displayById(999999L);
        check("displayById(999999) status is Error",
                "Error".equals(notFound.getStatus()));

        System.out.println();
    }

    // Tests: TrailDAO — displayAll and displayById
    private static void testTrailDAO() throws Exception {
        System.out.println("━━━ 4. TrailDAO ━━━");
        TrailDAO dao = new JdbcTrailDAO(URL, USER, PASS);

        // displayAll
        ServerResponse<ArrayList<Trail>> allResp = dao.displayAll();
        check("displayAll() status is Success",
                "Success".equals(allResp.getStatus()));
        check("displayAll() returns data",
                allResp.getData() != null);
        int count = allResp.getData() != null ? allResp.getData().size() : 0;
        System.out.println("  Found " + count + " trails in DB");

        // displayById — first existing record
        if (count > 0) {
            Long firstId = allResp.getData().get(0).getId();
            ServerResponse<Trail> byIdResp = dao.displayById(firstId);
            check("displayById(" + firstId + ") status is Success",
                    "Success".equals(byIdResp.getStatus()));
            check("displayById(" + firstId + ") has stops",
                    byIdResp.getData() != null &&
                            byIdResp.getData().getStops() != null &&
                            !byIdResp.getData().getStops().isEmpty());
        }

        // displayById — not found
        ServerResponse<Trail> notFound = dao.displayById(999999L);
        check("displayById(999999) status is Error",
                "Error".equals(notFound.getStatus()));

        // displayById — invalid id
        ServerResponse<Trail> invalid = dao.displayById(-5L);
        check("displayById(-5) status is Error",
                "Error".equals(invalid.getStatus()));

        System.out.println();
    }

    // Tests: TrailMediaDAO — displayAll and displayById
    private static void testTrailMediaDAO() throws Exception {
        System.out.println("━━━ 5. TrailMediaDAO ━━━");
        TrailMediaDAO dao = new JdbcTrailMediaDAO(URL, USER, PASS);

        // displayAll
        ServerResponse<ArrayList<TrailMedia>> allResp = dao.displayAll();
        check("displayAll() status is Success",
                "Success".equals(allResp.getStatus()));
        check("displayAll() returns data",
                allResp.getData() != null);

        int count = allResp.getData() != null ? allResp.getData().size() : 0;
        System.out.println("  Found " + count + " trail media records in DB");

        // displayById — first existing record
        if (count > 0) {
            Long firstId = allResp.getData().get(0).getId();
            ServerResponse<TrailMedia> byIdResp = dao.displayById(firstId);
            check("displayById(" + firstId + ") status is Success",
                    "Success".equals(byIdResp.getStatus()));
            check("displayById(" + firstId + ") data is not null",
                    byIdResp.getData() != null);
        }

        // displayById — not found
        ServerResponse<TrailMedia> notFound = dao.displayById(999999L);
        check("displayById(999999) status is Error",
                "Error".equals(notFound.getStatus()));

        System.out.println();
    }

    // Checks: a boolean condition and prints pass/fail
    private static void check(String label, boolean condition) {
        if (condition) {
            pass(label);
        } else {
            fail(label);
        }
    }

    // Prints: a PASSED result
    private static void pass(String label) {
        System.out.println("  ✔️ PASSED — " + label);
        fPassed++;
    }

    // Prints: a FAILED result
    private static void fail(String label) {
        System.out.println("  ✘ FAILED — " + label);
        fFailed++;
    }

    // Prints: the final summary
    private static void printSummary() {
        int total = fPassed + fFailed;
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║             RESULTS                  ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf( "║  Total:   %-28d ║%n", total);
        System.out.printf( "║  ✔️ Passed: %-27d ║%n", fPassed);
        System.out.printf( "║  ✘ Failed: %-27d ║%n", fFailed);
        System.out.println("╠══════════════════════════════════════╣");
        if (fFailed == 0) {
            System.out.println("║       ALL TESTS PASSED ✔️             ║");
        } else {
            System.out.println("║     SOME TESTS FAILED ✘              ║");
        }
        System.out.println("╚══════════════════════════════════════╝");
    }
}