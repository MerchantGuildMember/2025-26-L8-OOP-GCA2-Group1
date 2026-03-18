package DAO;

import tables.RouteStop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface RouteStopDAO {
    // F3 Get All Entities
    ArrayList<RouteStop> findAll() throws Exception;

    // F4 Get by ID
    Optional<RouteStop> findById(Long id) throws Exception;

    // F5 Delete By ID
    boolean deleteById(Long id) throws Exception;

    // F6 Insert Entity
    RouteStop insert(RouteStop location) throws Exception;

    // F7 Update Entity
    RouteStop update(RouteStop location) throws Exception;

    // F8 Filter with predicate
    List<RouteStop> findByFilter(java.util.function.Predicate<RouteStop> filter) throws Exception;

    // F9 JSON conversion

    String routeStopToJson(RouteStop routeStop) throws Exception;

    RouteStop routeStopFromJson(String json)throws Exception;

    String routeStopListToJson(List<RouteStop> routeStops)throws Exception;


}

