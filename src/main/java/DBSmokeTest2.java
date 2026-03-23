import DAO.DAO;
import DAO.JdbcLocationDAO;
import DAO.LocationDAO;
import server.MultiClientServer;
import shared.ServerResponse;
import tables.Location;

public class DBSmokeTest2 {
    public static void main(String[] args) throws Exception {

        // declaring user details and the link to the database
        String url = "jdbc:mysql://localhost:3306/oop_gca2?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "oop_gca2_user";
        String pass = "one";

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
