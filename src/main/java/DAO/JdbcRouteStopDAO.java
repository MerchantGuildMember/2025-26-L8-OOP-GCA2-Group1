package DAO;
import tables.Location;
import tables.RouteStop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import utils.JsonUtil;


public class JdbcRouteStopDAO implements RouteStopDAO {
    private String _url;
    private String _user;
    private String _pass;

    public JdbcRouteStopDAO(String url, String user, String pass) {
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("url is required");
        _url = url.trim();
        _user = user;
        _pass = pass;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(_url, _user, _pass);
    }


    @Override
    public ArrayList<RouteStop> findAll() throws Exception {
        String sql = "SELECT rs.id, rs.route_name, rs.created_at, " +
                "l.id AS loc_id, l.latitude, l.longitude, l.full_address, l.created_at AS loc_created_at " +
                "FROM route_stop rs " +
                "JOIN location l ON rs.location_id = l.id " +
                "ORDER BY rs.id";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ArrayList<RouteStop> out = new ArrayList<>();
            while (rs.next())
                out.add(mapRow(rs));
            return out;
        }

    }

    @Override
    public Optional<RouteStop> findById(Long id) throws Exception {
        if (id == null || id <= 0)
            return Optional.empty();

        String sql = "SELECT rs.id, rs.route_name, rs.created_at, " +
                "l.id AS loc_id, l.latitude, l.longitude, l.full_address, l.created_at AS loc_created_at " +
                "FROM route_stop rs " +
                "JOIN location l ON rs.location_id = l.id " +
                "WHERE rs.id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        }
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        if (id == null || id <= 0)
            return false;

        String sql = "DELETE FROM route_stop WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public RouteStop insert(RouteStop route_stop) throws Exception {

        String sql = "INSERT INTO route_stop (route_name, location_id, created_at) VALUES (?, ?, ?)";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, route_stop.getRoute_name());
            ps.setLong(2, route_stop.getLocation().getId());
            ps.setTimestamp(3, Timestamp.valueOf(route_stop.getCreated_at()));

            int rows = ps.executeUpdate();
            if (rows != 1)
                throw new IllegalStateException("insert failed, rows=" + rows);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next())
                    throw new IllegalStateException("no generated key returned");
                route_stop.setId(keys.getLong(1));
                return route_stop;
            }
        }
    }


    @Override
    public RouteStop update(RouteStop route_stop) throws Exception {
        String sql = "UPDATE route_stop SET route_name = ?, location_id = ? WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, route_stop.getRoute_name());
            ps.setLong(2, route_stop.getLocation().getId());
            ps.setLong(3, route_stop.getId());

            int rows = ps.executeUpdate();
            if(rows == 0) {
                throw new IllegalStateException("update failed, rows=" + rows);
            }
            return route_stop;
        }
    }

    @Override
    public List<RouteStop> findByFilter(Predicate<RouteStop> filter) throws Exception {
        return findAll().stream().filter(filter).collect(Collectors.toList());
    }

    private RouteStop mapRow(ResultSet rs) throws SQLException {
        Location location = new Location(
                rs.getLong("loc_id"),
                rs.getBigDecimal("latitude"),
                rs.getBigDecimal("longitude"),
                rs.getString("full_address"),
                rs.getTimestamp("loc_created_at").toLocalDateTime()
        );

        return new RouteStop(
                rs.getLong("id"),
                rs.getString("route_name"),
                location,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @Override
    public String routeStopToJson(RouteStop routeStop) {
        return JsonUtil.toJson(routeStop);
    }

    @Override
    public RouteStop routeStopFromJson(String json) {
        return JsonUtil.fromJson(json, RouteStop.class);
    }

    @Override
    public String routeStopListToJson(List<RouteStop> routeStops) {
        return JsonUtil.listToJson(routeStops);
    }


}
