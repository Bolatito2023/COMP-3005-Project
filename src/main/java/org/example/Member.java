package org.example;

import java.time.LocalDate;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
public class Member {

    //This will tell if the password entered during log in is verified or not
    private static boolean read = false;
    /**
     * Add a member to table "members" with specified information.
     * @param fn represents the member's first name.
     * @param ln represents the member's last name.
     * @param email represents the member's email address, this is unique.
     * @param password_hash represents the member's password.
     * Join date will be current date.
     */
    public static void memberRegister(String fn, String ln, String email,Date join_date, String password_hash) {
        String query = "INSERT INTO members (firstname, lastname, email, join_date, password_hash) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, fn);
            ps.setString(2, ln);
            ps.setString(3, email);
            ps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(5, hashPassword(password_hash));
            ps.executeUpdate();
            System.out.println("New member registered.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     *This method will help user to log into the Fitness App
     */
    public static void memberLogin(String email, String password) {
        String storedHashedPassword = null;

        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement ps = connect.prepareStatement("SELECT password_hash FROM members WHERE email = ?");

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                storedHashedPassword = rs.getString("password_hash");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (storedHashedPassword != null) {
            read = verifyPassword(password, storedHashedPassword);
            if (read) {
                System.out.println("Logged in successfully.");
            } else {
                System.out.println("Incorrect password.");
            }
        } else {
            System.out.println("No matching email found.");
        }
    }



    /**
     * This method, hashPassword, is responsible for taking a plain-text password as input,
     * hashing it
     * using the bcrypt hashing algorithm, and then returning the resulting hashed password.
     * @param password
     * @return
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * This method compares your entered password with the hashed password
     * @param password
     * @param hashedPassword
     * @return
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }


    /**
     * This method will help update members personal information
     */
    public void updatePersonalInformation(String firstname, String lastname, String email, int member_id) {
        try {
            Connection connection = FitnessApp.getConnection();

            PreparedStatement statement = connection.prepareStatement("UPDATE members SET firstname = ?, lastname = ?, email = ? WHERE member_id = ?");
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setInt(4, member_id);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Personal information updated successfully.");
            } else {
                System.out.println("Failed to update personal information.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFitnessGoal(String goalType, String value, Date deadline) {
        // Similar to updatePersonalInformation, construct SQL update statement for goals table
    }

    // Update health metrics
    public void updateHealthMetrics(String metricType, String value, Date recordDate) {
        // Similar to updatePersonalInformation, construct SQL update statement for health_metrics table
    }


}

