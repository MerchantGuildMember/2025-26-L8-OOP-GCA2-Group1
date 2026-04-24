package DAO;

import shared.ServerResponse;
import tables.TrailMedia;

public interface TrailMediaDAO extends DAO<TrailMedia> {

    // Gets: metadata for one record without loading the BLOB column (F20)
    ServerResponse<TrailMedia> getMetadataById(Long id) throws Exception;
}
