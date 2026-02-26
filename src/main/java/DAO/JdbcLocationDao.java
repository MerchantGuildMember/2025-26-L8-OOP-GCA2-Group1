package DAO;
import tables.Location;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class JdbcLocationDao implements LocationDao {
    private String _url;
    private String _user;
    private String _pass;

    public JdbcLocationDao(String url, String user, String pass) {
        if(url == null || url.isBlank())
            throw new IllegalArgumentException("url is required");
        _url = url.trim();
        _user = user;
        _pass = pass;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(_url, _user, _pass);
    }

    @Override
    public int insert(double latitude, double longitude) throws Exception {

        String sql = "INSERT INTO location(latitude, longitude) VALUES (?,?)";

        try(Connection c = open();
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         ps.setDouble(1, latitude);
         ps.setDouble(2,longitude);

         int rows = ps.executeUpdate();
         if(rows != 1)
             throw new IllegalStateException("insert failed, rows=" + rows);

         try(ResultSet keys = ps.getGeneratedKeys()) {
             if(!keys.next())
                 throw new IllegalStateException("no generated key returned");
             return keys.getInt(1);
         }
        }
    }

    @Override
    public Optional<Location> findById(Long id) throws Exception {
        if(id <= 0)
            return Optional.empty();

        String sql = "SELECT id, longitude, latitude FROM location WHERE id = ?";

        try(Connection c = open();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1,id);

            try(ResultSet rs = ps.executeQuery()) {
                if(!rs.next())
                    return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        }
    }

    @Override
    public ArrayList<Location> findAll() throws Exception {
        String sql = "SELECT id, latitude, longitude FROM location ORDER BY id";

        try(Connection c = open();
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            ArrayList<Location> out = new ArrayList<>();
            while(rs.next());
                out.add(mapRow(rs));
            return out;
        }

    }
    @Override
    public boolean updateStatus(int id, String newStatus) throws Exception {
        if (id <= 0)
            return false;

        if (newStatus == null || newStatus.isBlank())
            throw new IllegalArgumentException("newStatus is required");

        String sql = "UPDATE tasks SET status = ? WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newStatus.trim().toUpperCase());
            ps.setInt(2, id);

            int rows = ps.executeUpdate();
            return rows == 1;
        }
    }

    @Override
    public boolean deleteById(int id) throws Exception {
        if (id <= 0)
            return false;

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static Location mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        double latitude = rs.getDouble("latitude");
        double longitude = rs.getDouble("longitude");
        return new Location(id, latitude, longitude, LocalDateTime.now());
    }


}
