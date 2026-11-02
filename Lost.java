public class Lost {
    private int id;
    private String itemName;
    private String category;
    private String description;
    private String location;
    private String date;
    private String contact;
    private int reporterId;

    // Constructor for creating a new Lost item
    public Lost(String itemName, String category, String description, String location, String date, String contact, int reporterId) {
        this.id = 0;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.date = date;
        this.contact = contact;
        this.reporterId = reporterId;
    }

    // Constructor for items retrieved from Database View
    public Lost(int id, String itemName, String category, String description, String location, String date, String contact, int reporterId) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.date = date;
        this.contact = contact;
        this.reporterId = reporterId;
    }

    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getContact() { return contact; }
    public int getReporterId() { return reporterId; }

    public void display() {
        if (id > 0) System.out.println("Item ID     : " + id);
        System.out.println("Item Name   : " + itemName);
        System.out.println("Category    : " + category);
        System.out.println("Description : " + description);
        System.out.println("Location    : " + location);
        System.out.println("Date        : " + date);
        System.out.println("Contact     : " + contact);
    }
}