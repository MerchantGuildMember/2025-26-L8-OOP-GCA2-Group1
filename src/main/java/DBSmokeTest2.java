import DAO.JdbcLocationDAO;
import DAO.LocationDAO;
import server.MultiClientServer;
import tables.Location;

public class DBSmokeTest2 {
    public static void main(String[] args) throws Exception {


        LocationDAO dao = new JdbcLocationDAO(System.getenv("URL"), System.getenv("USER"), System.getenv("PASS"));

        MultiClientServer<Location> server = new MultiClientServer<Location>(3_360, dao);
        server.start();

        // 3. Test FIND ALL
        System.out.println("All locations:");
        dao.findAll().forEach(location ->
                System.out.println(location.getId() + ": " + location.getFullAddress())
        );


    }
}
