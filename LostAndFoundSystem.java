import java.sql.*;
import java.util.ArrayList;

public class LostAndFoundSystem {
    
    private static final String URL = "jdbc:mysql://localhost:3306/lost_and_found_db";
    private static final String USER = "root";
    private static final String PASSWORD = "4207"; // UPDATE THIS

    public LostAndFoundSystem() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {} 
        catch (SQLException e) { System.out.println("Database connection failed: " + e.getMessage()); }
    }

    // Helper method for 3NF Normalization
    private int getOrCreateCategoryId(Connection conn, String categoryName) throws SQLException {
        String selectSql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        }

        String insertSql = "INSERT INTO categories (category_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categoryName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (user_id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.executeUpdate();
            System.out.println("\n[DBMS FEATURE: USER AUTH] Registered into 'users' table.");
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public User loginUser(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("name"), 
                                rs.getString("email"), rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    public void addLostItem(Lost item) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int categoryId = getOrCreateCategoryId(conn, item.getCategory());
            System.out.println("\n[DBMS FEATURE: NORMALIZATION (3NF)] Mapped category '" + item.getCategory() 
                               + "' to category_id = " + categoryId + " in 'categories' table.");

            String sql = "INSERT INTO lost_items (item_name, category, description, location, date_reported, contact, reporter_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item.getItemName());
                pstmt.setString(2, item.getCategory());
                pstmt.setString(3, item.getDescription());
                pstmt.setString(4, item.getLocation());
                pstmt.setString(5, item.getDate());
                pstmt.setString(6, item.getContact());
                pstmt.setInt(7, item.getReporterId());
                pstmt.setInt(8, categoryId);
                pstmt.executeUpdate();
            }

            // Trigger Verification
            String triggerQuery = "SELECT * FROM audit_log ORDER BY log_id DESC LIMIT 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(triggerQuery)) {
                if (rs.next()) {
                    System.out.println("[DBMS FEATURE: TRIGGER ACTIVATED] Fired 'after_lost_item_insert'.");
                    System.out.println("   └─ Audit Entry -> Action: " + rs.getString("action_type") 
                                       + " | Item: " + rs.getString("item_name") 
                                       + " | Timestamp: " + rs.getTimestamp("action_timestamp"));
                }
            }
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    public void addFoundItem(Found item) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int categoryId = getOrCreateCategoryId(conn, item.getCategory());
            System.out.println("\n[DBMS FEATURE: NORMALIZATION (3NF)] Mapped category '" + item.getCategory() 
                               + "' to category_id = " + categoryId + " in 'categories' table.");

            String sql = "INSERT INTO found_items (item_name, category, description, location, date_reported, contact, reporter_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item.getItemName());
                pstmt.setString(2, item.getCategory());
                pstmt.setString(3, item.getDescription());
                pstmt.setString(4, item.getLocation());
                pstmt.setString(5, item.getDate());
                pstmt.setString(6, item.getContact());
                pstmt.setInt(7, item.getReporterId());
                pstmt.setInt(8, categoryId);
                pstmt.executeUpdate();
                System.out.println("Found item reported successfully into database!");
            }
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    public ArrayList<Lost> getLostItemsFromView() {
        ArrayList<Lost> lostItems = new ArrayList<>();
        String sql = "SELECT * FROM vw_public_lost_items";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lostItems.add(new Lost(
                    rs.getInt("id"),
                    rs.getString("item_name"), 
                    rs.getString("category_name"),
                    rs.getString("description"), 
                    rs.getString("location"),
                    rs.getString("date_reported"), 
                    "[HIDDEN BY SECURE VIEW]", 
                    0
                ));
            }
        } catch (SQLException e) { System.out.println("Error reading lost view: " + e.getMessage()); }
        return lostItems;
    }

    public ArrayList<Found> getFoundItemsFromView() {
        ArrayList<Found> foundItems = new ArrayList<>();
        String sql = "SELECT * FROM vw_public_found_items";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                foundItems.add(new Found(
                    rs.getInt("id"),
                    rs.getString("item_name"), 
                    rs.getString("category_name"),
                    rs.getString("description"), 
                    rs.getString("location"),
                    rs.getString("date_reported"), 
                    "[HIDDEN BY SECURE VIEW]", 
                    0
                ));
            }
        } catch (SQLException e) { System.out.println("Error reading found view: " + e.getMessage()); }
        return foundItems;
    }

    public void runEODBatchProcess() {
        System.out.println("\n=======================================================");
        System.out.println("[DBMS FEATURE: STORED PROCEDURE (EOD BATCH PROCESSING)]");
        System.out.println("Executing server-side procedure 'EOD_Archive_Old_Items(30)'...");
        String sql = "{call EOD_Archive_Old_Items(?)}";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, 30);
            cstmt.execute();
            System.out.println("   └─ Success: MySQL Server archived old records server-side!");
            System.out.println("=======================================================");
        } catch (SQLException e) { 
            System.out.println("Batch Process Failed: " + e.getMessage()); 
        }
    }

    // ACID TRANSACTION: Removes item from BOTH found_items AND lost_items if listed in both!
    public void claimItemWithACID(int itemId, String itemName, int claimerUserId) {
        System.out.println("\n=======================================================");
        System.out.println("[DBMS FEATURE: DUAL-TABLE ACID TRANSACTION DEMO]");
        String deleteFoundSql = "DELETE FROM found_items WHERE id = ?";
        String deleteLostSql = "DELETE FROM lost_items WHERE LOWER(item_name) = LOWER(?)";
        String logSql = "INSERT INTO audit_log (action_type, item_name) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); 
            System.out.println("1. setAutoCommit(false) -> Multi-table transaction started.");

            try (PreparedStatement deleteFoundStmt = conn.prepareStatement(deleteFoundSql);
                 PreparedStatement deleteLostStmt = conn.prepareStatement(deleteLostSql);
                 PreparedStatement logStmt = conn.prepareStatement(logSql)) {
                 
                // STEP 1: Delete from found_items
                deleteFoundStmt.setInt(1, itemId);
                int foundRows = deleteFoundStmt.executeUpdate();

                if (foundRows == 0) {
                    System.out.println("X. Found Item ID " + itemId + " not found! Rolling back transaction.");
                    conn.rollback();
                    return;
                }
                System.out.println("2. Step 1 Execute: Removed Item ID " + itemId + " ('" + itemName + "') from 'found_items'.");

                // STEP 2: Delete matching item from lost_items if it exists on both lists
                deleteLostStmt.setString(1, itemName);
                int lostRows = deleteLostStmt.executeUpdate();
                if (lostRows > 0) {
                    System.out.println("3. Step 2 Execute: Matching report found in 'lost_items'! Removed " + lostRows + " record(s).");
                } else {
                    System.out.println("3. Step 2 Execute: No active matching listing found in 'lost_items'.");
                }

                // STEP 3: Write Audit Log
                logStmt.setString(1, "CLAIMED & REMOVED FROM BOTH LISTS BY USER " + claimerUserId);
                logStmt.setString(2, itemName);
                logStmt.executeUpdate();
                System.out.println("4. Step 3 Execute: Logged claim event into 'audit_log'.");

                // COMMIT TRANSACTION
                conn.commit(); 
                System.out.println("5. conn.commit() -> All 3 operations committed atomically across found_items, lost_items, and audit_log!");
                System.out.println("=======================================================");
                
            } catch (SQLException ex) {
                conn.rollback(); 
                System.out.println("X. ERROR DETECTED! conn.rollback() -> All changes reverted across all tables.");
                System.out.println("=======================================================");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { System.out.println("Connection Error: " + e.getMessage()); }
    }

    public void displayAllItems() {
        System.out.println("\n=======================================================");
        System.out.println("[DBMS FEATURE: SQL VIEWS EXECUTED]");
        System.out.println("   └─ Querying 'vw_public_lost_items' and 'vw_public_found_items'");
        System.out.println("   └─ Private phone numbers and emails are automatically masked.");
        System.out.println("=======================================================");

        System.out.println("\n===== LOST ITEMS =====");
        ArrayList<Lost> lItems = getLostItemsFromView();
        if (lItems.isEmpty()) System.out.println("No lost items reported.");
        for (Lost item : lItems) { item.display(); System.out.println("----------------------"); }

        System.out.println("\n===== FOUND ITEMS =====");
        ArrayList<Found> fItems = getFoundItemsFromView();
        if (fItems.isEmpty()) System.out.println("No found items reported.");
        for (Found item : fItems) { item.display(); System.out.println("----------------------"); }
    }
}