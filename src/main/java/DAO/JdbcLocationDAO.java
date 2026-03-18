package DAO;

import tables.Location;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import utils.JsonUtil;

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
    public ArrayList<Location> findAll() throws Exception {
        String sql = "SELECT id, latitude, longitude, full_address, created_at FROM location ORDER BY id";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ArrayList<Location> out = new ArrayList<>();
            while (rs.next())
                out.add(mapRow(rs));
            return out;
        }

    }

    @Override
    public Optional<Location> findById(Long id) throws Exception {
        if (id == null || id <= 0)
            return Optional.empty();

        String sql = "SELECT id, latitude, longitude, full_address, created_at FROM location WHERE id = ?";

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
        return findAll().stream().filter(filter).collect(Collectors.toList());
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
    public String locationToJson(Location location) {
        return JsonUtil.toJson(location);
    }

    @Override
    public Location locationFromJson(String json) {
        return JsonUtil.fromJson(json, Location.class);
    }

    @Override
    public String locationListToJson(List<Location> locations) {
        return JsonUtil.listToJson(locations);
    }


}
