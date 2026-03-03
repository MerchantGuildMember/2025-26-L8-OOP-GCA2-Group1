package DAO;

import tables.Location;

import java.util.ArrayList;
import java.util.Optional;

public interface LocationDAO {
    int insert(double latitude, double longitude) throws Exception;

    Optional<Location> findById(Long id) throws Exception;
    ArrayList<Location> findAll() throws Exception;


    boolean updateStatus(int id, String newStatus) throws Exception;

    boolean deleteById(int id) throws Exception;
}

