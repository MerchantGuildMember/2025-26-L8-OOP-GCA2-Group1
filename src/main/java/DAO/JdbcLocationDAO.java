package DAO;

import shared.ServerResponse;
import tables.Location;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import utils.JsonUtil;

/**
 * JDBC implementation of {@link LocationDAO} for the Trail Tracker application.
 *
 * <p>Provides CRUD operations for {@link tables.Location} records backed by a
 * MySQL database. All queries use {@link java.sql.PreparedStatement} to prevent
 * SQL injection.</p>
 *
 * @author Aleksy Cieslak
 */
public class JdbcLocationDAO implements LocationDAO {
    private String _url;
    private String _user;
    private String _pass;

    public JdbcLocationDAO(String url, String user, String pass) {
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("URL is required");
        _url = url;
        _user = user;
        _pass = pass;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(_url, _user, _pass);
    }


    @Override
    public ServerResponse<ArrayList<Location>> displayAll() throws Exception {
        String sql = "SELECT id, latitude, longitude, full_address, created_at FROM location ORDER BY id";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ArrayList<Location> out = new ArrayList<>();
            while (rs.next())
                out.add(mapRow(rs));
            return ServerResponse.ok("Retrieved locations", out);
        }

    }

    @Override
    public ServerResponse<Location> displayById(Long id) throws Exception {
        if (id == null || id <= 0)
            return ServerResponse.error("ID Error " + id);


        String sql = "SELECT id, latitude, longitude, full_address, created_at FROM location WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return ServerResponse.error("Location not found: " + id);
                return ServerResponse.ok("Found location", mapRow(rs));
            }
        }
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        if (id == null || id <= 0)
            return false;

        String sql = "DELETE FROM location WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public Location insert(Location location) throws Exception {

        String sql = "INSERT INTO location(latitude, longitude, full_address, created_at) VALUES (?,?,?,?)";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, location.getLatitude());
            ps.setBigDecimal(2, location.getLongitude());
            ps.setString(3, location.getFullAddress());
            ps.setTimestamp(4, Timestamp.valueOf(location.getCreationTime()));

            int rows = ps.executeUpdate();
            if (rows != 1)
                throw new IllegalStateException("insert failed, rows=" + rows);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next())
                    throw new IllegalStateException("no generated key returned");
                location.setId(keys.getLong(1));
                return location;
            }
        }
    }


    @Override
    public Location update(Location location) throws Exception {
        String sql = "UPDATE location SET latitude=?, longitude=?, full_address=?, created_at=? WHERE id=?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBigDecimal(1, location.getLatitude());
            ps.setBigDecimal(2, location.getLongitude());
            ps.setString(3, location.getFullAddress());
            ps.setTimestamp(4, Timestamp.valueOf(location.getCreationTime()));
            ps.setLong(5, location.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("update failed, rows=" + rows);
            }
            return location;
        }
    }

    @Override
    public List<Location> findByFilter(Predicate<Location> filter) throws Exception {
        return displayAll().getData().stream().filter(filter).collect(Collectors.toList());
    }

    private static Location mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        BigDecimal latitude = rs.getBigDecimal("latitude");
        BigDecimal longitude = rs.getBigDecimal("longitude");
        String fullAddress = rs.getString("full_address");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return new Location(id, latitude, longitude, fullAddress, createdAt);
    }

    @Override
    public Location entFromJson(String json) {
        return JsonUtil.fromJson(json, Location.class);
    }

}
