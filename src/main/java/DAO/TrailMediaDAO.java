package DAO;

import tables.TrailMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TrailMediaDAO {
    // F3 Get All Entities
    ArrayList<TrailMedia> findAll() throws Exception;

    // F4 Get by ID
    Optional<TrailMedia> findById(Long id) throws Exception;

    // F5 Delete By ID
    boolean deleteById(Long id) throws Exception;

    // F6 Insert Entity
    TrailMedia insert(TrailMedia location) throws Exception;

    // F7 Update Entity
    TrailMedia update(TrailMedia location) throws Exception;

    // F8 Filter with predicate
    List<TrailMedia> findByFilter(java.util.function.Predicate<TrailMedia> filter) throws Exception;

    // F9 JSON conversion
    String trailMediaToJson(TrailMedia trailMedia) throws Exception;

    TrailMedia trailMediaFromJson(String json) throws Exception;

    String trailMediaListToJson(List<TrailMedia> trailMediaList) throws Exception;


}

