import DAO.JdbcLocationDAO;
import DAO.JdbcTrailDAO;
import DAO.LocationDAO;
import DAO.TrailDAO;
import tables.Location;
import tables.Trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static utils.JsonUtil.listToJson;

public class DbSmokeTest {

    public static void main(String[] args) throws Exception {

       /* LocationDAO dao = new JdbcLocationDAO(System.getenv("URL"), System.getenv("USER"), System.getenv("PASS"));

        Location newLoc = new Location(null,
                53.3497, 6.2603,
                "Dublin, O'Connell", LocalDateTime.now());
        Location inserted = dao.insert(newLoc);

            // 2. Test FIND BY ID
            Long id = inserted.getId();
            dao.findById(id).ifPresentOrElse(
                    location -> System.out.println("Found: " + location.getFullAddress()),
                    () -> System.out.println("Location not found!")
            );

            // 3. Test FIND ALL
            System.out.println("All locations:");
            dao.findAll().forEach(location ->
                    System.out.println(location.getId() + ": " + location.getFullAddress())
            );

            // 4. Test UPDATE
            inserted.setFullAddress("Updated Address, NY");
            Location updated = dao.update(inserted);
            System.out.println("Updated address to: " + updated.getFullAddress());

            // 5. Test DELETE
            boolean deleted = dao.deleteById(id);
            System.out.println("Deleted: " + deleted);

            // 6. Test FILTER (F8) – find locations with latitude > 40
            System.out.println("Locations with latitude > 40:");
            dao.findByFilter(location -> location.getLatitude().compareTo(new BigDecimal("40")) > 0)
                    .forEach(location -> System.out.println(location.getLatitude()));

            // 7. Test JSON Conversion (F9)
            System.out.println("JSON Conversion Test");
            String json = dao.entToJson(newLoc);
            System.out.println("Converted location to JSON: " + json);

            Location location = dao.entFromJson(json);
            System.out.println("Converted JSON to location: " + location);
            Location newLoc1 = new Location(null,
                    92.3497, 5.2603,
                    "Drogheda, New Bridge", LocalDateTime.now());

            Location newLoc2 = new Location(null,
                    33.3497, 6.2203,
                    "Dundalk, Train Station", LocalDateTime.now());

            List<Location> locations = new ArrayList<>();
            locations.add(newLoc);
            locations.add(newLoc1);
            locations.add(newLoc2);
            System.out.println("Converted List<Location> to JSON: " + listToJson(locations));

            TrailDAO trailDAO =  new JdbcTrailDAO(url, user, pass);
            List<Trail> trails = trailDAO.findAll();
            for(Trail trail : trails){
                System.out.println(trail.getId());
            }

        */
    }


}


