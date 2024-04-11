
package org.example;
import java.sql.*;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.sql.Time;
public class Session {


    /**
     *Add a session to the sessions table, given the room, trainer, date, time and whom requested
     *Also add corresponding info to session_members relation
     */
    public static void addSession(int room_id, int trainer_id, LocalDate date, Time starting_time, Time end_time, String type, int member_id) {
        if (date.isBefore(LocalDate.now())) {
            System.err.println("Error: The date is already passed.");
            return;
        }

        String query = "INSERT INTO sessions (room_id, trainer_id, date, starting_time, end_time, status, type)" +
                "VALUES (?, ?, ?, ?, ?, 'member_requested', '" + type + "')";

        String trigger = "INSERT INTO session_members (session_id, member_id) VALUES (?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connect.prepareStatement(trigger);

            ps.setInt(1, room_id);
            ps.setInt(2, trainer_id);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, starting_time);
            ps.setTime(5, end_time);
            ps.executeUpdate();

            System.out.println("Session added successfully.");

            ResultSet rs = ps.getGeneratedKeys();

            int sessionId = -1;
            while(rs.next()) {
                sessionId = rs.getInt("session_id");
            }
            ps2.setInt(1, sessionId);
            ps2.setInt(2, member_id);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the duration of a session
     * @param sessionId represent the session's ID
     * @return the duration as a float
     */
    public static float getSessionLength(int sessionId) {
        String query = "SELECT starting_time, end_time FROM sessions WHERE session_id = ?";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Time startTime = rs.getTime("starting_time");
                    Time endTime = rs.getTime("end_time");

                    long ms = endTime.getTime() - startTime.getTime();

                    return ms / 3600000.0f;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Session not found.");
        return -1;
    }

    /**
     * Update the Session status
     * Status could be denial or staff confirmed
     * @param sessionId
     * @param newStatus
     */
    public static void updateSessionStatus(String sessionId, String newStatus) {
        String query = "UPDATE sessions SET status = ? WHERE session_id = ?";
        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, newStatus);
            ps.setInt(2, Integer.parseInt(sessionId));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
