package ccrrs.database;

import ccrrs.model.Complaint;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for persisting complaints and history in MySQL.
 */
public class ComplaintDAO {

    public static boolean insertComplaint(Complaint c) {
        String sql = "INSERT INTO complaints (case_id, complainant_name, phone, email, city, category, " +
                "description, severity, financial_loss, victim_count, priority_score, status, " +
                "assigned_officer_id, deadline_days, investigation_cost, expected_resolution_value) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status), priority_score = VALUES(priority_score)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, c.getCaseId());
                stmt.setString(2, c.getComplainantName());
                stmt.setString(3, c.getPhone());
                stmt.setString(4, c.getEmail());
                stmt.setString(5, c.getCity());
                stmt.setString(6, c.getCategory());
                stmt.setString(7, c.getDescription());
                stmt.setInt(8, c.getSeverity());
                stmt.setDouble(9, c.getFinancialLoss());
                stmt.setInt(10, c.getVictimCount());
                stmt.setDouble(11, c.getPriorityScore());
                stmt.setString(12, c.getStatus());
                stmt.setString(13, c.getAssignedOfficerId().equals("UNASSIGNED") ? null : c.getAssignedOfficerId());
                stmt.setInt(14, c.getDeadlineDays());
                stmt.setInt(15, c.getInvestigationCost());
                stmt.setInt(16, c.getExpectedResolutionValue());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("  [DAO Error] " + e.getMessage());
            return false;
        }
    }

    public static List<Complaint> loadAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT * FROM complaints ORDER BY case_id ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return list;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Complaint c = new Complaint(
                            rs.getString("case_id"),
                            rs.getString("complainant_name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("city"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getString("timestamp"),
                            rs.getInt("severity"),
                            rs.getDouble("financial_loss"),
                            rs.getInt("victim_count"),
                            rs.getInt("deadline_days"),
                            rs.getInt("investigation_cost"),
                            rs.getInt("expected_resolution_value")
                    );
                    c.setPriorityScore(rs.getDouble("priority_score"));
                    c.updateStatus(rs.getString("status"), "DATABASE_LOAD", "Loaded from persistence");
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("  [DAO Error] " + e.getMessage());
        }
        return list;
    }
}
