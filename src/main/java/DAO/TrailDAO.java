package DAO;

import tables.Trail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TrailDAO {
    // F3 Get All Entities
    ArrayList<Trail> findAll() throws Exception;

    // F4 Get by ID
    Optional<Trail> findById(Long id) throws Exception;

    // F5 Delete By ID
    boolean deleteById(Long id) throws Exception;

    // F6 Insert Entity
    Trail insert(Trail location) throws Exception;

    // F7 Update Entity
    Trail update(Trail location) throws Exception;

    // F8 Filter with predicate
    List<Trail> findByFilter(java.util.function.Predicate<Trail> filter) throws Exception;

    // F9 JSON conversion

    String trailToJson(Trail trail) throws Exception;

    Trail trailFromJson(String json) throws Exception;

    String trailListToJson(List<Trail> trails) throws Exception;
}




