import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class LostAndFoundSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/lost_and_found_db";
    private static final String USER = "root";
    private static final String PASSWORD = "4207"; // UPDATE THIS PASSWORD

    public static final String[] STANDARDIZED_CATEGORIES = {
        "Electronics", "Documents", "Accessories", "Books & Stationery", "Clothing", "Personal Items", "Other"
    };

    public LostAndFoundSystem() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {} 
        catch (SQLException e) { System.out.println("Database connection failed: " + e.getMessage()); }
    }

    public static boolean isValidDate(int day, int month, int year) {
        try {
            LocalDate inputDate = LocalDate.of(year, month, day);
            return !inputDate.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private int getOrCreateCategoryId(Connection conn, String categoryName) throws SQLException {
        String selectSql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("category_id");
        }

        String insertSql = "INSERT INTO categories (category_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, categoryName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
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
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public User loginUser(int userId, String name) {
        String sql = "SELECT * FROM users WHERE user_id = ? AND LOWER(name) = LOWER(?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"));
            }
        } catch (SQLException e) {}
        return null;
    }

    public String addLostItem(Lost item) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int categoryId = getOrCreateCategoryId(conn, item.getCategory());
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

            String triggerQuery = "SELECT * FROM audit_log ORDER BY log_id DESC LIMIT 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(triggerQuery)) {
                if (rs.next()) {
                    return "[3NF] Category ID: " + categoryId + " | [SQL TRIGGER ACTIVATED] Audit Log created: " + rs.getString("action_type");
                }
            }
        } catch (SQLException e) { return "Database Error: " + e.getMessage(); }
        return "Lost item reported successfully!";
    }

    public String addFoundItem(Found item) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int categoryId = getOrCreateCategoryId(conn, item.getCategory());
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
                return "[3NF] Found item saved with Category ID: " + categoryId;
            }
        } catch (SQLException e) { return "Database Error: " + e.getMessage(); }
    }

    public ArrayList<Lost> getLostItemsFromView() {
        ArrayList<Lost> lostItems = new ArrayList<>();
        String sql = "SELECT l.id, l.item_name, COALESCE(c.category_name, l.category) AS category_name, " +
                     "l.description, l.location, l.date_reported, COALESCE(l.contact, '[HIDDEN]') AS contact " +
                     "FROM lost_items l LEFT JOIN categories c ON l.category_id = c.category_id";
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
                    rs.getString("contact"), 
                    0
                ));
            }
        } catch (SQLException e) { System.out.println("Error fetching lost items: " + e.getMessage()); }
        return lostItems;
    }

    public ArrayList<Found> getFoundItemsFromView() {
        ArrayList<Found> foundItems = new ArrayList<>();
        String sql = "SELECT f.id, f.item_name, COALESCE(c.category_name, f.category) AS category_name, " +
                     "f.description, f.location, f.date_reported, COALESCE(f.contact, '[HIDDEN]') AS contact " +
                     "FROM found_items f LEFT JOIN categories c ON f.category_id = c.category_id";
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
                    rs.getString("contact"), 
                    0
                ));
            }
        } catch (SQLException e) { System.out.println("Error fetching found items: " + e.getMessage()); }
        return foundItems;
    }

    public String runEODBatchProcess() {
        String sql = "{call EOD_Archive_Old_Items(?)}";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, 30);
            cstmt.execute();
            return "[STORED PROCEDURE EXECUTED] EOD_Archive_Old_Items(30) complete. Old items moved to archive.";
        } catch (SQLException e) { 
            return "Batch Process Failed: " + e.getMessage(); 
        }
    }

    public String claimItemWithACID(int foundItemId, int matchingLostItemId, int claimerUserId) {
        StringBuilder log = new StringBuilder();
        log.append("[ACID TRANSACTION STARTED]\n1. setAutoCommit(false)\n");

        String deleteFoundSql = "DELETE FROM found_items WHERE id = ?";
        String deleteLostSql = "DELETE FROM lost_items WHERE id = ?";
        String auditSql = "INSERT INTO audit_log (action_type, item_name) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtFound = conn.prepareStatement(deleteFoundSql);
                 PreparedStatement stmtLost = conn.prepareStatement(deleteLostSql);
                 PreparedStatement stmtAudit = conn.prepareStatement(auditSql)) {

                stmtFound.setInt(1, foundItemId);
                int foundRows = stmtFound.executeUpdate();
                if (foundRows == 0) {
                    conn.rollback();
                    return "X. Transaction Failed: Found Item ID " + foundItemId + " does not exist. Rolled back.";
                }
                log.append("2. Removed Found Item ID ").append(foundItemId).append(" from 'found_items'\n");

                if (matchingLostItemId > 0) {
                    stmtLost.setInt(1, matchingLostItemId);
                    int lostRows = stmtLost.executeUpdate();
                    if (lostRows > 0) {
                        log.append("3. Removed matching Lost Item ID ").append(matchingLostItemId).append(" from 'lost_items'\n");
                    }
                }

                stmtAudit.setString(1, "CLAIMED BY USER ID " + claimerUserId);
                stmtAudit.setString(2, "Found ID: " + foundItemId + " | Lost ID: " + matchingLostItemId);
                stmtAudit.executeUpdate();
                log.append("4. Logged claim audit event\n");

                conn.commit();
                log.append("5. conn.commit() -> Transaction committed atomically!");
                return log.toString();

            } catch (SQLException ex) {
                conn.rollback();
                return "X. Transaction Error: " + ex.getMessage() + "\nconn.rollback() executed.";
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { return "Connection Error: " + e.getMessage(); }
    }
}