package DAO;

import tables.TrailMedia;

import java.util.ArrayList;
import java.util.Optional;

public interface TrailMediaDAO {
    int insert(double latitude, double longitude) throws Exception;

    Optional<TrailMedia> findById(Long id) throws Exception;
    ArrayList<TrailMedia> findAll() throws Exception;


    boolean updateStatus(int id, String newStatus) throws Exception;

    boolean deleteById(int id) throws Exception;
}

