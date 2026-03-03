package DAO;
import tables.TrailMedia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JdbcTrailMediaDAO implements TrailMediaDAO {
    private String _url;
    private String _user;
    private String _pass;

    public JdbcTrailMediaDAO(String url, String user, String pass) {
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
    public ArrayList<TrailMedia> findAll() throws Exception {
        String sql = "SELECT id, trail_id, stop_id, media_type, url, caption, creation_time FROM trail_media ORDER BY id";
        ArrayList<TrailMedia> list = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Optional<TrailMedia> findById(Long id) throws Exception {
        if (id == null || id <= 0) return Optional.empty();
        String sql = "SELECT id, trail_id, stop_id, media_type, url, caption, creation_time FROM trail_media WHERE id = ?";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        }
    }

    @Override
    public boolean deleteById(Long id) throws Exception {
        if (id == null || id <= 0) return false;
        String sql = "DELETE FROM trail_media WHERE id = ?";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public TrailMedia insert(TrailMedia media) throws Exception {
        String sql = "INSERT INTO trail_media (trail_id, stop_id, media_type, url, caption, creation_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, media.getTrail_id());
            if (media.getStop_id() != null) {
                ps.setLong(2, media.getStop_id());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, media.getMedia_type());
            ps.setString(4, media.getUrl());
            ps.setString(5, media.getCaption());
            ps.setTimestamp(6, Timestamp.valueOf(media.getCreation_time()));

            int rows = ps.executeUpdate();
            if (rows != 1) throw new IllegalStateException("insert failed, rows=" + rows);

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new IllegalStateException("no generated key returned");
                media.setId(keys.getLong(1));
                return media;
            }
        }
    }

    @Override
    public TrailMedia update(TrailMedia media) throws Exception {
        String sql = "UPDATE trail_media SET trail_id=?, stop_id=?, media_type=?, url=?, caption=?, creation_time=? WHERE id=?";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, media.getTrail_id());
            if (media.getStop_id() != null) {
                ps.setLong(2, media.getStop_id());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, media.getMedia_type());
            ps.setString(4, media.getUrl());
            ps.setString(5, media.getCaption());
            ps.setTimestamp(6, Timestamp.valueOf(media.getCreation_time()));
            ps.setLong(7, media.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new IllegalStateException("update failed, rows=" + rows);
            return media;
        }
    }

    @Override
    public List<TrailMedia> findByFilter(Predicate<TrailMedia> filter) throws Exception {
        return findAll().stream().filter(filter).collect(Collectors.toList());
    }

    private TrailMedia mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long trail_id = rs.getLong("trail_id");
        Long stop_id = rs.getLong("stop_id");
        String media_type = rs.getString("media_type");
        String url = rs.getString("url");
        String caption = rs.getString("caption");
        LocalDateTime creation_time = rs.getTimestamp("creation_time").toLocalDateTime();
        return new TrailMedia(id, trail_id, stop_id, media_type, url, caption, creation_time);
    }
}
