import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LostAndFoundSystem system = new LostAndFoundSystem();
        User loggedInUser = null;

        System.out.println("======================================");
        System.out.println("  ENTERPRISE LOST & FOUND SYSTEM");
        System.out.println("======================================");

        // Authentication Loop
        while (loggedInUser == null) {
            System.out.println("\n1. Login with User ID");
            System.out.println("2. Register New User");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Please enter a valid choice number.");
                sc.nextLine();
                continue;
            }
            int authChoice = sc.nextInt();
            sc.nextLine();

            if (authChoice == 1) {
                System.out.print("Enter your User ID: ");
                if (!sc.hasNextInt()) {
                    System.out.println("User ID must be numeric.");
                    sc.nextLine();
                    continue;
                }
                int loginId = sc.nextInt();
                sc.nextLine();
                loggedInUser = system.loginUser(loginId);
                if (loggedInUser == null) {
                    System.out.println("User ID not found. Please register first.");
                } else {
                    System.out.println("\nWelcome back, " + loggedInUser.getName() + "!");
                }
            } else if (authChoice == 2) {
                System.out.print("Create User ID (Numbers only): ");
                if (!sc.hasNextInt()) {
                    System.out.println("User ID must be numeric.");
                    sc.nextLine();
                    continue;
                }
                int newId = sc.nextInt();
                sc.nextLine();
                System.out.print("Enter Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Email: ");
                String email = sc.nextLine();
                System.out.print("Enter Phone: ");
                String phone = sc.nextLine();

                User newUser = new User(newId, name, email, phone);
                if (system.registerUser(newUser)) {
                    System.out.println("Registration successful! You are logged in.");
                    loggedInUser = newUser;
                }
            } else if (authChoice == 3) {
                System.out.println("Goodbye!");
                sc.close();
                return;
            }
        }

        // Main Menu
        int choice = 0;
        do {
            System.out.println("\n======================================");
            System.out.println("              MAIN MENU");
            System.out.println("======================================");
            System.out.println("1. Report Lost Item ");
            System.out.println("2. Report Found Item");
            System.out.println("3. View All Items");
            System.out.println("4. Run EOD Batch Process");
            System.out.println("5. Claim Item ");
            System.out.println("6. View User Details");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input! Please enter a menu number (1-7).");
                sc.nextLine();
                continue;
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("\n===== REPORT LOST ITEM =====");
                    System.out.print("Enter Item Name: ");
                    String lostName = sc.nextLine();
                    System.out.print("Enter Category (e.g. Electronics, Books): ");
                    String lostCat = sc.nextLine();
                    System.out.print("Enter Description: ");
                    String lostDesc = sc.nextLine();
                    System.out.print("Enter Location: ");
                    String lostLoc = sc.nextLine();
                    System.out.print("Enter Date (DD-MM-YYYY): ");
                    String lostDate = sc.nextLine();
                    System.out.print("Enter Contact: ");
                    String lostContact = sc.nextLine();
                    
                    Lost lostItem = new Lost(lostName, lostCat, lostDesc, lostLoc, lostDate, lostContact, loggedInUser.getUserId());
                    system.addLostItem(lostItem);
                    break;

                case 2:
                    System.out.println("\n===== REPORT FOUND ITEM =====");
                    System.out.print("Enter Item Name: ");
                    String fName = sc.nextLine();
                    System.out.print("Enter Category: ");
                    String fCat = sc.nextLine();
                    System.out.print("Enter Description: ");
                    String fDesc = sc.nextLine();
                    System.out.print("Enter Location: ");
                    String fLoc = sc.nextLine();
                    System.out.print("Enter Date (DD-MM-YYYY): ");
                    String fDate = sc.nextLine();
                    System.out.print("Enter Contact: ");
                    String fContact = sc.nextLine();
                    
                    Found foundItem = new Found(fName, fCat, fDesc, fLoc, fDate, fContact, loggedInUser.getUserId());
                    system.addFoundItem(foundItem);
                    break;

                case 3:
                    system.displayAllItems();
                    break;

                case 4:
                    system.runEODBatchProcess();
                    break;

                case 5:
                    System.out.print("\nEnter Found Item ID to Claim (See ID in 'View All Items'): ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid input! Item ID must be numeric (e.g., 1, 2).");
                        sc.nextLine();
                        break;
                    }
                    int claimId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Item Name: ");
                    String claimName = sc.nextLine();
                    system.claimItemWithACID(claimId, claimName, loggedInUser.getUserId());
                    break;

                case 6:
                    System.out.println("\n===== CURRENT USER DETAILS =====");
                    loggedInUser.display();
                    break;

                case 7:
                    System.out.println("\nLogging out. Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 7);
        sc.close();
    }
}