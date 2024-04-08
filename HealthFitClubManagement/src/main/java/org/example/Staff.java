package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Staff {
    /**
     * Add a staff to table "staff" with specified information.
     * @param fn represents the member's first name.
     * @param ln represents the member's last name.
     * @param email represents the member's email address, this is unique.
     * @param password_hash represents the staff's password.
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
                        System.out.printf("Room ID: %d, Equipment Type: %s\n",
                                rs.getInt("room_id"), rs.getString("equipment_type"));
                    }

                    System.out.println("----------------------------------------------------------------");
                    System.out.println("Enter room IDs that you have maintained, separated by commas.");

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

            if (success > 0) System.out.println("Room with IDm" + roomId + " has been marked as maintained.");
            else System.out.println("Room with ID" + roomId + " not found.");

        } catch (SQLException e) { e.printStackTrace(); }
    }

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
}