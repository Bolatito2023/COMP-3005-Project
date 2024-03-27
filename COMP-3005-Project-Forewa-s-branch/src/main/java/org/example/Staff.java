package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Scanner;

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

            if (success > 0) System.out.println("Room with IDm" + roomId + " has been marked as maintained.");
            else System.out.println("Room with ID" + roomId + " not found.");

        } catch (SQLException e) { e.printStackTrace(); }
    }
}
