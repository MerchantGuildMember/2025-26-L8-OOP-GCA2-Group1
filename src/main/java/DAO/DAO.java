package DAO;

import utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface DAO<T> {

    // F3 Get All Entities
    ArrayList<T> findAll() throws Exception;

    // F4 Get by ID
    Optional<T> findById(Long id) throws Exception;

    // F5 Delete By ID
    boolean deleteById(Long id) throws Exception;

    // F6 Insert Entity
    T insert(T ent) throws Exception;

    // F7 Update Entity
    T update(T ent) throws Exception;

    // F8 Filter with predicate
    List<T> findByFilter(java.util.function.Predicate<T> filter) throws Exception;

    // F9 JSON conversion

    default String entToJson(T ent) {
        return JsonUtil.toJson(ent);
    }

    T entFromJson(String json) throws Exception;

    default String listToJson(List<T> entities) {
        return JsonUtil.listToJson(entities);
    }

}