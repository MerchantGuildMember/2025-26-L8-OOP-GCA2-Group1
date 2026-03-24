import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

//        welcomeMessage();

        displayMenu();


    }
    static void welcomeMessage() throws Exception {
        System.out.println("Welcome to Tour & Trail planning system!");
        for(int i = 2; i > 0; i--) {
            System.out.println("Moving on in "+i+" seconds...");
            Thread.sleep(1000);
        }
        clearScreen();
    }

    static void displayMenu() {
        System.out.println("_________Menu_________");
        System.out.println("\t1. Go to CRUD");
        System.out.println("\t2. Exit");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        do{
            System.out.println("Try again");
            choice = input.nextInt();
        } while(choice < 1 || choice > 2);
        route(choice);
    }

    static void route(int choice) {
        switch (choice) {
            case 1:
                displayCRUD();
                break;
            case 2:
                System.exit(0);
                break;
            case 3:
                createMenu();
                break;
            case 4:
                readMenu();
                break;
            case 5:
                updateMenu();
                break;
            case 6:
                deleteMenu();
                break;
            case 7:
                addNewLocation();
                break;
            case 8:
                addNewRouteStop();
                break;
            case 9:
                addNewTrail();
                break;
            case 10:
                addNewTrailMedia();
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    static void createMenu() {
        System.out.println("_______________________");
        System.out.println("1. Add new Location");
        System.out.println("2. Add new RouteStop");
        System.out.println("3. Add new Trail");
        System.out.println("4. Add new TrailMedia");
        System.out.println("5. Exit");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        do{
            System.out.println("Try again");
            choice = input.nextInt();
        } while(choice < 1 || choice > 5);

        if(choice == 5) System.exit(0);
        route(choice+6);

    }

    static void readMenu() {
        System.out.println("_______________________");

        System.out.println("_______________________");
    }

    static void updateMenu() {
        System.out.println("_______________________");
        System.out.println("1. Update Location");
        System.out.println("2. Update RouteStop");
        System.out.println("3. Update Trail");
        System.out.println("4. Update TrailMedia");
        System.out.println("5. Exit");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        do{
            System.out.println("Try again");
            choice = input.nextInt();
        }  while(choice < 1 || choice > 5);
        if(choice == 5) System.exit(0);


//        route(choice+6);
    }


    static void displayCRUD() {
        System.out.println("_______________________");
        System.out.println("1. Create");
        System.out.println("2. Read");
        System.out.println("3. Update");
        System.out.println("4. Delete");
        System.out.println("5. Exit");
        System.out.println("_______________________");
        System.out.print("Input: ");
        Scanner input = new Scanner(System.in);
        int choice = input.nextInt();
        do{
            System.out.println("Try again");
            choice = input.nextInt();
        } while(choice < 1 || choice > 5);

        if(choice == 5) System.exit(0);
        route(choice+2);

    }

    static void clearScreen() {
        for(int i = 0; i < 5; i++) {
            System.out.println("\n");
        }
    }
}

