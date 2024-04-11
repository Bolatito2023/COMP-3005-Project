
package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Staff {


    /**
     * Member will be charged $50.0 for a private session per hour
     */
    private static final double PRIVATE_SESSION_RATE = 50.0;
    /**
     * Member will be charged $30.0 for a private session per hour
     */
    private static final double PUBLIC_SESSION_RATE = 30.0;
    public enum SessionType {PRIVATE, PUBLIC}
    /**
     * Add a staff to table "staff" with specified information.
     * @param fn represents the member's first name.
     * @param ln represents the member's last name.
     * @param email represents the member's email address, this is unique.
     * @param password_hash represents the staff's password.
     *
     */
    public static void staffRegister(String fn, String ln, String email,String password_hash) {
        String query = "INSERT INTO staff (firstname, lastname, email,  password_hash) VALUES (?, ?, ?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, fn);
            ps.setString(2, ln);
            ps.setString(3, email);
            ps.setString(4, BCrypt.hashpw(password_hash, BCrypt.gensalt()));
            ps.executeUpdate();
            System.out.println("New staff registered.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log in as a staff, email and password required.
     * @return the staff's id
     */
    public static Integer staffLogin(String email, String password) {
        String storedHashedPassword = null;

        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement ps = connect.prepareStatement("SELECT * FROM staff WHERE email = ?");

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                storedHashedPassword = rs.getString("password_hash");
            }

            if (storedHashedPassword != null) {
                //This will tell if the password entered during log in is verified or not
                boolean read = BCrypt.checkpw(password, storedHashedPassword);
                if (read) {
                    System.out.println("Logged in successfully.");
                    return (rs.getInt("staff_id"));
                } else {
                    System.out.println("Incorrect password.");
                    return null;
                }
            } else {
                System.out.println("No matching email found.");
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Display all rooms managed by a specific staff with need_maintenance set to true, then ask user which of those
     * rooms have been maintained.
     * For each user input, if that room exists, set its need_maintenance to false.
     * If there are still any unmaintained rooms after user input, display them and ask again for user input.
     * @param staffId represents the staff's id
     */
    public static void CheckMaintenance(int staffId) {
        String query = "SELECT * FROM ROOMS WHERE need_maintenance = TRUE AND staff_id = ?";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            boolean allMaintained;
            do{
                ps.setInt(1, staffId);
                allMaintained = true;

                ResultSet rs = ps.executeQuery();

                if (!rs.isBeforeFirst()) {
                    System.out.println("No more rooms need maintenance.\n");
                    return;
                }
                else {
                    System.out.println("----------------------------------------------------------------");
                    while (rs.next()) {
                        allMaintained = false;
                        System.out.printf("Room ID: %d, Equipment Type: %s, Capacity: %d\n",
                                rs.getInt("room_id"), rs.getString("equipment_type"),
                                rs.getInt("capacity"));
                    }

                    System.out.println("----------------------------------------------------------------");
                    System.out.println("Enter room IDs that you have maintained, separated by commas");

                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();

                    if (input.isEmpty()) {
                        return;
                    }
                    else {
                        String[] roomIds = input.replace(" ", "").split(",");
                        for (String roomId : roomIds) {
                            MarkMaintained(Integer.parseInt(roomId));
                        }
                    }
                }
            } while(!allMaintained);
        } catch (SQLException e) { e.printStackTrace(); }

    }

    /**
     * Set the need_maintenance value of a room to false
     * @param roomId represents the room that being maintained
     */
    private static void MarkMaintained(int roomId) {
        String query = "UPDATE rooms SET need_maintenance = FALSE WHERE room_id = ?";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, roomId);

            int success = ps.executeUpdate();

            if (success > 0) System.out.println("Room with ID" + roomId + " has been marked as maintained.");
            else System.out.println("Room with ID" + roomId + " not found.");

        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * This method adds a bill to a member depending on their id, session type and hours spent
     * @param memberId

     */
    public static void addMemberBill(int memberId, int sessionId) {

        // Implement logic to add a bill to the member's account in the database
        try {
            Connection connect = FitnessApp.getConnection();
            String query = "SELECT type FROM sessions WHERE session_id = ?";
            PreparedStatement ps = connect.prepareStatement(query);
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String sessionTypeString = rs.getString("type");
                SessionType sessionType = SessionType.valueOf(sessionTypeString.toUpperCase()); // Convert string to enum
                double ratePerHour = (sessionType == SessionType.PRIVATE) ? PRIVATE_SESSION_RATE : PUBLIC_SESSION_RATE;
                double amount = calculateBillAmount(Session.getSessionLength(sessionId), ratePerHour);
                boolean successful = insertAmountToBill(memberId, amount);
                if (successful) {
                    System.out.println("Bill successfully added.");
                } else {
                    System.out.println("Failed to add Bill");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void processPayment(int bill_id) {
        try {
            Connection connection = FitnessApp.getConnection();

            // Check if the amount is 0
            PreparedStatement checkStatement = connection.prepareStatement("SELECT amount FROM bills WHERE bill_id = ?");
            checkStatement.setInt(1, bill_id);
            ResultSet rs = checkStatement.executeQuery();

            if (rs.next()) {
                double amount = rs.getDouble("amount");
                if (amount == 0) {
                    // Update status to 'paid'
                    PreparedStatement updateStatement = connection.prepareStatement("UPDATE bills SET status = 'paid' WHERE bill_id = ?");
                    updateStatement.setInt(1, bill_id);
                    int rowsUpdated = updateStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Payment has been processed");
                    } else {
                        System.out.println("Unable to process Payment");
                    }
                } else {
                    System.out.println("Payment cannot be processed because the amount is not zero.");
                }
            } else {
                System.out.println("No bill found with the provided bill ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method helps to view the members bill history
     */
    public static void viewMemberBillHistory(){
        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement ps = connect.prepareStatement("SELECT * FROM bills");

            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("Bill history is empty");
            }
            else {
                System.out.println("\n");
                System.out.println("----------------------------------------------------------------");
                while (rs.next()) {
                    int bill_id = rs.getInt("bill_id");
                    int member_id = rs.getInt("member_id");
                    double amount = rs.getDouble("amount");
                    String status = rs.getString("status");

                    System.out.println("Bill ID: "+ bill_id + ", Member ID: " +member_id + ", Amount: $" +amount + ", Status: "+ status);
                }
                System.out.println("----------------------------------------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method inserts a bill to a members account
     * @param memberId id of the member
     * @param amount the amount to be paid
     * @return true if inserted false otherwise
     */
    private static boolean insertAmountToBill(int memberId, double amount) {
        try {
            Connection connect = FitnessApp.getConnection();
            String query = "INSERT INTO bills (member_id, amount, status) VALUES (?, ?, 'unpaid')";
            PreparedStatement ps = connect.prepareStatement(query);
            ps.setInt(1, memberId);
            ps.setDouble(2, amount);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method will calculate the members bill based on type hours spent e.t.c
     * @param hoursSpent
     * @param ratePerHour
     * @return
     */
    private static double calculateBillAmount(float hoursSpent, double ratePerHour) {

        return ratePerHour*hoursSpent;
    }

    /**
     * This method displays session requests made by members
     */
    public static void displaySessionRequests() {
        String query = "SELECT s.session_id, s.room_id, s.trainer_id, s.date, s.starting_time, s.end_time, s.type, COUNT(sm.member_id) AS member_count " +
                "FROM sessions s LEFT JOIN session_members sm ON s.session_id = sm.session_id " +
                "WHERE s.status = 'trainer_confirmed' " +
                "GROUP BY s.session_id " +
                "HAVING COUNT(sm.member_id) > 0";

        Scanner scanner = new Scanner(System.in);

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            boolean allDecided;
            do {
                allDecided = true;

                ResultSet rs = ps.executeQuery();

                List<Integer> pending1 = new ArrayList<>();
                List<Integer> pending2 = new ArrayList<>();

                if (!rs.isBeforeFirst()) {
                    System.out.println("No more session requests.\n");
                    return;
                }
                else {
                    System.out.println("----------------------------------------------------------------");
                    while (rs.next()) {
                        System.out.println("Session ID: " + rs.getInt("session_id") +
                                ", Room ID: " + rs.getInt("room_id") +
                                ", Trainer ID: " + rs.getInt("trainer_id") +
                                ", Date: " + rs.getDate("date") +
                                ", Starting Time: " + rs.getTime("starting_time") +
                                ", End Time: " + rs.getTime("end_time") +
                                ", Type: " + rs.getString("type") +
                                ", Number of People: " + rs.getInt("member_count"));
                        pending1.add(rs.getInt("session_id"));
                    }
                    System.out.println("----------------------------------------------------------------");
                    System.out.println("Enter session IDs that you wish to accept, separated by commas, invalid inputs will be skipped.");


                    String input = scanner.nextLine();

                    if(!input.isEmpty()) {
                        String[] sessionIds = input.replace(" ", "").split(",");
                        for (String sessionId : sessionIds) {
                            if (pending1.contains(Integer.parseInt(sessionId))) {
                                Session.updateSessionStatus(sessionId, "staff_confirmed");
                            }
                        }
                        System.out.println("Sessions accepted");
                    }

                }
                ResultSet rs2 = ps.executeQuery();

                if(rs2.isBeforeFirst()) {
                    System.out.println("----------------------------------------------------------------");
                    while (rs2.next()) {
                        System.out.println("Session ID: " + rs2.getInt("session_id") +
                                ", Room ID: " + rs2.getInt("room_id") +
                                ", Trainer ID: " + rs2.getInt("trainer_id") +
                                ", Date: " + rs2.getDate("date") +
                                ", Starting Time: " + rs2.getTime("starting_time") +
                                ", End Time: " + rs2.getTime("end_time") +
                                ", Number of People: " + rs2.getInt("member_count"));
                        pending2.add(rs2.getInt("session_id"));
                    }
                    System.out.println("----------------------------------------------------------------");
                    System.out.println("Enter session IDs that you wish to deny, separated by commas, invalid inputs will be skipped.");

                    String input = scanner.nextLine();

                    if(input.isEmpty()) { return; }
                    else {
                        String[] sessionIds = input.replace(" ", "").split(",");
                        for (String sessionId : sessionIds) {
                            if (pending2.contains(Integer.parseInt(sessionId))) {
                                Session.updateSessionStatus(sessionId, "denied");
                            }
                        }
                        System.out.println("Sessions denied");
                    }
                }

            }while(!allDecided);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void handleFinishedSessions() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();


        String deleteQuery = "DELETE FROM sessions WHERE (date < ? OR (date = ? AND end_time < '" + currentTime + "')) AND status != 'staff_confirmed'";
        String selectQuery = "SELECT * FROM sessions WHERE (date < ? OR (date = ? AND end_time < '" + currentTime + "')) AND status = 'staff_confirmed'";

        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement deletePs = connect.prepareStatement(deleteQuery);
            PreparedStatement selectPs = connect.prepareStatement(selectQuery);

            deletePs.setDate(1, Date.valueOf(currentDate));
            deletePs.setDate(2, Date.valueOf(currentDate));
            deletePs.executeUpdate();

            selectPs.setDate(1, Date.valueOf(currentDate));
            selectPs.setDate(2, Date.valueOf(currentDate));
            ResultSet selectRs = selectPs.executeQuery();

            List<Integer> sessionIds = new ArrayList<>();

            System.out.println("----------------------------------------------------------------");
            while (selectRs.next()) {
                int sessionId = selectRs.getInt("session_id");
                Date date = selectRs.getDate("date");
                Time startingTime = selectRs.getTime("starting_time");
                Time endTime = selectRs.getTime("end_time");

                sessionIds.add(sessionId);
                System.out.println("Session ID: " + sessionId + ", Date: " + date + ", Starting Time: " + startingTime + ", End Time: " + endTime);
            }
            System.out.println("----------------------------------------------------------------");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the session IDs you wish to terminate, separated by commas, or type 'ALL' to terminate all, invalid inputs will be skipped:");
            String input = scanner.nextLine();

            if ("ALL".equalsIgnoreCase(input)) {
                for (int id : sessionIds) {
                    terminateSession(id);
                }
            }
            else {
                List<String> deleteList = Arrays.asList(input.split("\\s*,\\s*"));

                for (String idStr : deleteList) {
                    int sessionId = Integer.parseInt(idStr);

                    if (sessionIds.contains(sessionId)) {
                        terminateSession(sessionId);
                    }
                }
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * This method terminates sessions
     * @param sessionId id of the session
     */

    private static void terminateSession(int sessionId) {
        String memberQuery = "SELECT member_id FROM session_members WHERE session_id = ?";
        String deleteQuery = "DELETE FROM sessions WHERE session_id = ?";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement memberPs = connect.prepareStatement(memberQuery);
            PreparedStatement deletePs = connect.prepareStatement(deleteQuery);

            memberPs.setInt(1, sessionId);
            ResultSet memberRs = memberPs.executeQuery();

            while (memberRs.next()) {
                int memberId = memberRs.getInt("member_id");
                addMemberBill(memberId, sessionId);
            }

            deletePs.setInt(1, sessionId);
            deletePs.executeUpdate();

        } catch (SQLException e) { e.printStackTrace(); }

    }
}
