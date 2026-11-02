public class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private boolean isAdmin;

    // Standard User Constructor
    public User(int userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = false;
    }

    // Admin Constructor
    public User(int userId, String name, boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.email = "admin@system.local";
        this.phone = "N/A";
        this.isAdmin = isAdmin;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isAdmin() { return isAdmin; }
}