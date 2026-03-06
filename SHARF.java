import java.util.*;

/* =========================
   ROOMMATE FINDER (JAVA DSA VERSION)
========================= */

class User {
    String username;
    String password;
    boolean isAdmin;
    boolean banned;

    User(String u, String p, boolean admin) {
        username = u;
        password = p;
        isAdmin = admin;
        banned = false;
    }
}

class Listing {
    int id;
    String name;
    String area;
    int rent;
    String gender;
    String postedBy;
    boolean approved;
    boolean premium;

    Listing(int id, String name, String area, int rent, String gender, String postedBy) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.rent = rent;
        this.gender = gender;
        this.postedBy = postedBy;
        this.approved = false;
        this.premium = false;
    }
}

public class SHARF {

    static Scanner sc = new Scanner(System.in);

    // DSA Structures
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Listing> listings = new ArrayList<>();
    static Queue<Listing> pendingQueue = new LinkedList<>(); // FIFO
    static Stack<Listing> deleteStack = new Stack<>(); // LIFO
    static HashMap<Integer, Listing> listingMap = new HashMap<>(); // Fast lookup

    static int idCounter = 1;
    static User currentUser = null;

    public static void main(String[] args) {

        // Default Admin
        users.add(new User("admin", "admin123", true));

        while (true) {
            if (currentUser == null) {
                authMenu();
            } else {
                if (currentUser.isAdmin)
                    adminMenu();
                else
                    userMenu();
            }
        }
    }

    /* =========================
       AUTH
    ========================= */

    static void authMenu() {
        System.out.println("\n=== S.H.A.R.F - Hyderabad Roommate Finder ===");
        System.out.println("1. Login");
        System.out.println("2. Signup");
        System.out.println("3. Exit");

        int choice = sc.nextInt();
        sc.nextLine();

        if (choice == 1) login();
        else if (choice == 2) signup();
        else System.exit(0);
    }

    static void login() {
        System.out.print("Username: ");
        String u = sc.nextLine();
        System.out.print("Password: ");
        String p = sc.nextLine();

        for (User user : users) {
            if (user.username.equals(u) && user.password.equals(p)) {
                if (user.banned) {
                    System.out.println("Account is banned!");
                    return;
                }
                currentUser = user;
                System.out.println("Login successful!");
                return;
            }
        }

        System.out.println("Invalid credentials.");
    }

    static void signup() {
        System.out.print("Choose username: ");
        String u = sc.nextLine();
        System.out.print("Choose password: ");
        String p = sc.nextLine();

        users.add(new User(u, p, false));
        System.out.println("Account created!");
    }

    /* =========================
       USER MENU
    ========================= */

    static void userMenu() {
        System.out.println("\n--- User Menu ---");
        System.out.println("1. Add Listing");
        System.out.println("2. View Approved Listings");
        System.out.println("3. Delete My Listing");
        System.out.println("4. Logout");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> addListing();
            case 2 -> viewListings();
            case 3 -> deleteListing();
            case 4 -> currentUser = null;
        }
    }

    static void addListing() {
        System.out.print("Your Name: ");
        String name = sc.nextLine();

        System.out.print("Area (Hyderabad): ");
        String area = sc.nextLine();

        System.out.print("Rent (₹): ");
        int rent = sc.nextInt();
        sc.nextLine();

        System.out.print("Looking for (Male/Female/Any): ");
        String gender = sc.nextLine();

        Listing l = new Listing(idCounter++, name, area, rent, gender, currentUser.username);

        pendingQueue.add(l); // FIFO Queue
        listingMap.put(l.id, l);

        System.out.println("Listing submitted for admin approval!");
    }

    static void viewListings() {
        System.out.println("\n--- Approved Listings ---");

        for (Listing l : listings) {
            if (l.approved) {
                System.out.println("ID: " + l.id +
                        " | " + l.name +
                        " | " + l.area +
                        " | ₹" + l.rent +
                        (l.premium ? " | ⭐ Featured" : ""));
            }
        }
    }

    static void deleteListing() {
        System.out.print("Enter listing ID to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        Listing l = listingMap.get(id);

        if (l != null && l.postedBy.equals(currentUser.username)) {
            listings.remove(l);
            deleteStack.push(l); // LIFO stack
            System.out.println("Deleted! (Can undo)");
        } else {
            System.out.println("Invalid ID.");
        }
    }

    /* =========================
       ADMIN MENU
    ========================= */

    static void adminMenu() {
        System.out.println("\n--- Admin Panel ---");
        System.out.println("1. Approve Next Listing (FIFO)");
        System.out.println("2. View All Listings");
        System.out.println("3. Feature Listing");
        System.out.println("4. Undo Last Delete (LIFO)");
        System.out.println("5. Ban User");
        System.out.println("6. Logout");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> approveListing();
            case 2 -> viewListings();
            case 3 -> featureListing();
            case 4 -> undoDelete();
            case 5 -> banUser();
            case 6 -> currentUser = null;
        }
    }

    static void approveListing() {
        if (pendingQueue.isEmpty()) {
            System.out.println("No pending listings.");
            return;
        }

        Listing l = pendingQueue.poll(); // FIFO
        l.approved = true;
        listings.add(l);

        System.out.println("Approved listing ID: " + l.id);
    }

    static void featureListing() {
        System.out.print("Enter ID to feature: ");
        int id = sc.nextInt();
        sc.nextLine();

        Listing l = listingMap.get(id);

        if (l != null && l.approved) {
            l.premium = true;
            System.out.println("Listing marked as premium.");
        } else {
            System.out.println("Invalid ID.");
        }
    }

    static void undoDelete() {
        if (deleteStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }

        Listing l = deleteStack.pop(); // LIFO
        listings.add(l);
        System.out.println("Restored listing ID: " + l.id);
    }

    static void banUser() {
        System.out.print("Enter username to ban: ");
        String uname = sc.nextLine();

        for (User u : users) {
            if (u.username.equals(uname)) {
                u.banned = true;
                System.out.println("User banned.");
                return;
            }
        }

        System.out.println("User not found.");
    }
}