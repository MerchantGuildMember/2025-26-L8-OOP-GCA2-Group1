package DAO;

import tables.RouteStop;

import java.util.ArrayList;
import java.util.Optional;

public interface RouteStopDAO {
    int insert(double latitude, double longitude) throws Exception;

    Optional<RouteStop> findById(Long id) throws Exception;
    ArrayList<RouteStop> findAll() throws Exception;


    boolean updateStatus(int id, String newStatus) throws Exception;

    boolean deleteById(int id) throws Exception;
}

