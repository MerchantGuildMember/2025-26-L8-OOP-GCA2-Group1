package DAO;

import tables.Trail;

import java.util.ArrayList;
import java.util.Optional;

public interface TrailDAO {
    int insert(double latitude, double longitude) throws Exception;

    Optional<Trail> findById(Long id) throws Exception;
    ArrayList<Trail> findAll() throws Exception;


    boolean updateStatus(int id, String newStatus) throws Exception;

    boolean deleteById(int id) throws Exception;
}

