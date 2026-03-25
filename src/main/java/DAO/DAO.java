package DAO;

import shared.ServerResponse;
import utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Generic DAO interface defining the full CRUD contract for all entities.
 * Display methods return {@link ServerResponse} so the server can forward
 * them directly to the client without any extra wrapping.
 *
 * @param <T> the entity type managed by this DAO
 * @author Maryna Hordiienko (primary)
 */
public interface DAO<T> {

    // === Public API ===

    // Gets: all entities as a ServerResponse wrapping an ArrayList
    ServerResponse<ArrayList<T>> displayAll() throws Exception;

    // Gets: a single entity by ID, or an error ServerResponse if not found
    ServerResponse<T> displayById(Long id) throws Exception;

    // Checks: deletes entity by ID; returns true when a row was removed
    boolean deleteById(Long id) throws Exception;

    // Creates: inserts entity and returns the populated DTO including auto-generated ID
    T insert(T ent) throws Exception;

    // Updates: applies field changes and returns the updated DTO
    T update(T ent) throws Exception;

    // Gets: entities matching the given predicate — uses lambda, not raw SQL per filter
    List<T> findByFilter(Predicate<T> filter) throws Exception;

    // Converts: entity to JSON string
    default String entToJson(T ent) {
        return JsonUtil.toJson(ent);
    }

    // Converts: JSON string to entity
    T entFromJson(String json) throws Exception;

    // Converts: list of entities to JSON string
    default String listToJson(List<T> entities) {
        return JsonUtil.listToJson(entities);
    }
}
