package DAO;

import tables.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface LocationDAO {
    // F3 Get All Entities
    ArrayList<Location> findAll() throws Exception;

    // F4 Get by ID
    Optional<Location> findById(Long id) throws Exception;

    // F5 Delete By ID
    boolean deleteById(Long id) throws Exception;

    // F6 Insert Entity
    Location insert(Location location) throws Exception;

    // F7 Update Entity
    Location update(Location location) throws Exception;

    // F8 Filter with predicate
    List<Location> findByFilter(java.util.function.Predicate<Location> filter) throws Exception;

    // F9 JSON conversion


}

