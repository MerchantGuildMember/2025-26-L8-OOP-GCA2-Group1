import DAO.JdbcLocationDAO;
import tables.Location;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DbSmokeTest {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/oop_gca2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "oop_gca2_user";
        String pass = "one";

        JdbcLocationDAO dao = new JdbcLocationDAO(url, user, pass);

        try {
            // 1. Test INSERT
            Location newLoc = new Location(null,
                    53.3497, 6.2603,
                    "Dublin, O'Connell", LocalDateTime.now());
            Location inserted = dao.insert(newLoc);
            System.out.println("Inserted location with ID: " + inserted.getId());

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

        } catch (Exception e) {
            System.out.println("helolo");
            e.printStackTrace();
        }
    }
}