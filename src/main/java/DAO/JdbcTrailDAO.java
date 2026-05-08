package DAO;

import shared.ServerResponse;
import tables.Location;
import tables.RouteStop;
import tables.Trail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import utils.JsonUtil;

public class JdbcTrailDAO implements TrailDAO {
    private String _url;
    private String _user;
    private String _pass;

    public JdbcTrailDAO(String url, String user, String pass) {
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("url is required");
        _url = url;
        _user = user;
        _pass = pass;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(_url, _user, _pass);
    }


    @Override
    public ServerResponse<ArrayList<Trail>> displayAll() throws Exception {
        String sql = "SELECT id, name, description, difficulty, estimated_time FROM trail ORDER BY id";
        ArrayList<Trail> trails = new ArrayList<>();

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String difficulty = rs.getString("difficulty");
                double estimatedTime = rs.getDouble("estimated_time");

                ArrayList<RouteStop> stops = new ArrayList<>(loadStopsForTrail(id));
                if (stops.isEmpty()) {
                    continue;
                }

                Trail trail = new Trail(id, name, description, difficulty, estimatedTime, stops);
                trails.add(trail);
            }
        }


        return ServerResponse.ok("Retrieved trails", trails);
    }

    @Override
    public ServerResponse<Trail> displayById(Long id) throws Exception {
        if (id == null || id <= 0) {
            return ServerResponse.error("ID Error: " + id);
        }

        String sql = "SELECT id, name, description, difficulty, estimated_time FROM trail WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return ServerResponse.error("Trail not found: " + id);

                }

                String name = rs.getString("name");
                String description = rs.getString("description");
                String difficulty = rs.getString("difficulty");
                double estimatedTime = rs.getDouble("estimated_time");

                ArrayList<RouteStop> stops = new ArrayList<>(loadStopsForTrail(id));

                Trail trail = new Trail(id, name, description, difficulty, estimatedTime, stops);
                return ServerResponse.ok("Retrieved trail", trail);
            }
        }
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        if (id == null || id <= 0) return false;
        try (Connection c = open()) {
            String sqlStop = "DELETE FROM trail_stop WHERE trail_id = ?";
            try (PreparedStatement psStop = c.prepareStatement(sqlStop)) {
                psStop.setLong(1, id);
                psStop.executeUpdate();
            }
            String sqlTrail = "DELETE FROM trail WHERE id = ?";
            try (PreparedStatement psTrail = c.prepareStatement(sqlTrail)) {
                psTrail.setLong(1, id);
                return psTrail.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            System.err.println("Database error during trail delete: " + e.getMessage());
            throw new RuntimeException("Error deleting trail", e);
        }
    }

    @Override
    public Trail insert(Trail trail) throws Exception {
        try (Connection c = open();
             PreparedStatement psTrail = c.prepareStatement(
                     "INSERT INTO trail (name, description, difficulty, estimated_time) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            c.setAutoCommit(false);

            psTrail.setString(1, trail.getName());
            psTrail.setString(2, trail.getDescription());
            psTrail.setString(3, trail.getDifficulty());
            psTrail.setDouble(4, trail.getEstimated_time());

            int rows = psTrail.executeUpdate();
            if (rows != 1) throw new IllegalStateException("insert failed, rows=" + rows);

            try (ResultSet keys = psTrail.getGeneratedKeys()) {
                if (!keys.next()) throw new IllegalStateException("no generated key returned");
                trail.setId(keys.getLong(1));
            }

            List<RouteStop> stops = trail.getStops();
            if (stops != null && !stops.isEmpty()) {
                try (PreparedStatement psStop = c.prepareStatement(
                        "INSERT INTO trail_stop (trail_id, stop_id, stop_order) VALUES (?, ?, ?)")) {
                    int order = 1;

                    for (RouteStop stop : stops) {
                        psStop.setLong(1, trail.getId());
                        psStop.setLong(2, stop.getId());
                        psStop.setInt(3, order++);
                        psStop.addBatch();
                    }
                    psStop.executeBatch();
                }
            }

            c.commit();
            return trail;
        } catch (SQLException e) {
            System.err.println("Database error during trail insert: " + e.getMessage());
            throw new RuntimeException("Error inserting trail", e);
        }
    }

    @Override
    public Trail update(Trail trail) throws Exception {
        try (Connection c = open()) {
            String sqlTrail = "UPDATE trail SET name=?, description=?, difficulty=?, estimated_time=? WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sqlTrail)) {
                ps.setString(1, trail.getName());
                ps.setString(2, trail.getDescription());
                ps.setString(3, trail.getDifficulty());
                ps.setDouble(4, trail.getEstimated_time());
                ps.setLong(5, trail.getId());

                int rows = ps.executeUpdate();
                if (rows == 0) throw new IllegalStateException("update failed, trail not found");
            }

            String sql_del = "DELETE FROM trail_stop WHERE trail_id = ?";
            try (PreparedStatement psDel = c.prepareStatement(sql_del)) {
                psDel.setLong(1, trail.getId());
                psDel.executeUpdate();
            }

            List<RouteStop> stops = trail.getStops();
            if (stops != null && !stops.isEmpty()) {
                String sqlIns = "INSERT INTO trail_stop (trail_id, stop_id, stop_order) VALUES (?, ?, ?)";
                try (PreparedStatement psIns = c.prepareStatement(sqlIns)) {
                    int order = 1;
                    for (RouteStop stop : stops) {
                        psIns.setLong(1, trail.getId());
                        psIns.setLong(2, stop.getId());
                        psIns.setInt(3, order++);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
        }
        return trail;
    }

    @Override
    public List<Trail> findByFilter(Predicate<Trail> filter) throws Exception {
        return displayAll().getData().stream().filter(filter).collect(Collectors.toList());
    }

    private Trail mapTrailRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String difficulty = rs.getString("difficulty");
        double estimated_time = rs.getDouble("estimated_time");
        ArrayList<RouteStop> stops = new ArrayList<>();

        return new Trail(id, name, description, difficulty, estimated_time, stops);
    }

    private List<RouteStop> loadStopsForTrail(Long trailId) throws SQLException {
        String sql = "SELECT rs.id AS stop_id, rs.route_name, rs.created_at AS stop_created_at, " +
                "l.id AS loc_id, l.latitude, l.longitude, l.full_address, l.created_at AS loc_created_at " +
                "FROM trail_stop ts " +
                "JOIN route_stop rs ON ts.stop_id = rs.id " +
                "JOIN location l ON rs.location_id = l.id " +
                "WHERE ts.trail_id = ? " +
                "ORDER BY ts.stop_order";

        List<RouteStop> stops = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, trailId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Location loc = new Location(
                            rs.getLong("loc_id"),
                            rs.getBigDecimal("latitude"),
                            rs.getBigDecimal("longitude"),
                            rs.getString("full_address"),
                            rs.getTimestamp("loc_created_at").toLocalDateTime()
                    );
                    RouteStop stop = new RouteStop(
                            rs.getLong("stop_id"),
                            rs.getString("route_name"),
                            loc,
                            rs.getTimestamp("stop_created_at").toLocalDateTime()
                    );
                    stops.add(stop);
                }
            }
        }
        return stops;
    }

    @Override
    public Trail entFromJson(String json) {
        return JsonUtil.fromJson(json, Trail.class);
    }
}