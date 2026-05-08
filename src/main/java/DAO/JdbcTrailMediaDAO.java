package DAO;

import shared.ServerResponse;
import tables.TrailMedia;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import utils.JsonUtil;

/**
 * JDBC implementation of {@link TrailMediaDAO} for the Trail Tracker application.
 *
 * <p>Provides CRUD operations for {@link tables.TrailMedia} records backed by a
 * MySQL database, including binary file upload (BLOB), metadata-only retrieval,
 * and file data retrieval. All queries use {@link java.sql.PreparedStatement}
 * to prevent SQL injection.</p>
 *
 * @author Aleksy Cieslak
 */
public class JdbcTrailMediaDAO implements TrailMediaDAO {

    private String _url;
    private String _user;
    private String _pass;

    public JdbcTrailMediaDAO(String url, String user, String pass) {
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
    public ServerResponse<ArrayList<TrailMedia>> displayAll() throws Exception {
        String sql = "SELECT id, trail_id, stop_id, media_type, url, caption, creation_time, " +
                "file_data, file_name, content_type, file_size FROM trail_media ORDER BY id";
        ArrayList<TrailMedia> list = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs, true));
            }
        }
        return ServerResponse.ok("Retrieved trail media", list);
    }

    @Override
    public ServerResponse<TrailMedia> displayById(Long id) throws Exception {
        if (id == null || id <= 0)
            return ServerResponse.error("ID Error: " + id);
        String sql = "SELECT id, trail_id, stop_id, media_type, url, caption, creation_time, " +
                "file_data, file_name, content_type, file_size FROM trail_media WHERE id = ?";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return ServerResponse.error("Trail media not found: " + id);
                return ServerResponse.ok("Retrieved trail media", mapRow(rs, true));
            }
        }
    }

    // F20 — metadata only, BLOB column is not fetched
    public ServerResponse<TrailMedia> getMetadataById(Long id) throws Exception {
        if (id == null || id <= 0)
            return ServerResponse.error("ID Error: " + id);
        String sql = "SELECT id, trail_id, stop_id, media_type, url, caption, creation_time, " +
                "file_name, content_type, file_size FROM trail_media WHERE id = ?";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return ServerResponse.error("Trail media not found: " + id);
                return ServerResponse.ok("Retrieved trail media metadata", mapRow(rs, false));
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
        String sql = "INSERT INTO trail_media " +
                "(trail_id, stop_id, media_type, url, caption, creation_time, " +
                "file_data, file_name, content_type, file_size) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, media.getTrail_id());
            if (media.getStop_id() != null) {
                ps.setLong(2, media.getStop_id());
            }
            else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, media.getMedia_type());
            ps.setString(4, media.getUrl());
            ps.setString(5, media.getCaption());
            ps.setTimestamp(6, Timestamp.valueOf(media.getCreation_time()));
            if (media.getFFileData() != null) {
                byte[] data = media.getFFileData();
                ps.setBinaryStream(7, new ByteArrayInputStream(data), data.length);
            }
            else {
                ps.setNull(7, Types.BLOB);
            }
            ps.setString(8, media.getFFileName());
            ps.setString(9, media.getFContentType());
            ps.setInt(10, media.getFFileSize());

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
        String sql = "UPDATE trail_media SET trail_id=?, stop_id=?, media_type=?, url=?, " +
                "caption=?, creation_time=?, file_data=?, file_name=?, content_type=?, file_size=? " +
                "WHERE id=?";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, media.getTrail_id());
            if (media.getStop_id() != null) {
                ps.setLong(2, media.getStop_id());
            }
            else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, media.getMedia_type());
            ps.setString(4, media.getUrl());
            ps.setString(5, media.getCaption());
            ps.setTimestamp(6, Timestamp.valueOf(media.getCreation_time()));
            if (media.getFFileData() != null) {
                byte[] data = media.getFFileData();
                ps.setBinaryStream(7, new ByteArrayInputStream(data), data.length);
            }
            else {
                ps.setNull(7, Types.BLOB);
            }
            ps.setString(8, media.getFFileName());
            ps.setString(9, media.getFContentType());
            ps.setInt(10, media.getFFileSize());
            ps.setLong(11, media.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new IllegalStateException("update failed, rows=" + rows);
            return media;
        }
    }

    @Override
    public List<TrailMedia> findByFilter(Predicate<TrailMedia> filter) throws Exception {
        return displayAll().getData().stream().filter(filter).collect(Collectors.toList());
    }

    // includeBlobData=true loads file_data; false for metadata-only queries (F20)
    private TrailMedia mapRow(ResultSet rs, boolean includeBlobData) throws SQLException {
        Long id = rs.getLong("id");
        Long trail_id = rs.getLong("trail_id");
        Long stop_id = rs.getLong("stop_id");

        // Maryna's addition
        if (rs.wasNull()) {
            stop_id = null;
        }

        String media_type = rs.getString("media_type");
        String url = rs.getString("url");
        String caption = rs.getString("caption");
        LocalDateTime creation_time = rs.getTimestamp("creation_time").toLocalDateTime();
        String file_name = rs.getString("file_name");
        String content_type = rs.getString("content_type");
        int file_size = rs.getInt("file_size");

        byte[] file_data = null;
        if (includeBlobData) {
            file_data = rs.getBytes("file_data");
        }

        return new TrailMedia(id, trail_id, stop_id, media_type, url, caption, creation_time,
                file_data, file_name, content_type, file_size);
    }

    @Override
    public TrailMedia entFromJson(String json) {
        return JsonUtil.fromJson(json, TrailMedia.class);
    }
}
