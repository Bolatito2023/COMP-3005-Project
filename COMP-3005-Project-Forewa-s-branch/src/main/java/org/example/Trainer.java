package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Trainer{
    //This will tell if the password entered during log in is verified or not
    private static boolean read = false;
    //These are made so that when trainer wants to set availabilty after logging in
    //the database will automatically put its info and just ask for day of the week and time period
    private static String trainerFnName = "";
    private static String trainerLnName = "";
    private static String trainerEmail = "";
    private static String trainerSpeciality = "";



    /**
     * Add a trainer to table "trainer" with specified information.
     * @param fn represents the member's first name.
     * @param ln represents the member's last name.
     * @param email represents the member's email address, this is unique.
     * @param password_hash represents the staff's password.
     */
    public static void trainerRegister(String fn, String ln, String email,String speciality,String password_hash) {
        String query = "INSERT INTO trainer (firstname, lastname, email,speciality,  password_hash) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, fn);
            ps.setString(2, ln);
            ps.setString(3, email);
            ps.setString(4,speciality);
            ps.setString(5, BCrypt.hashpw(password_hash, BCrypt.gensalt()));
            ps.executeUpdate();
            System.out.println("New Trainer registered.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will help user to log into the Fitness App
     */
    /**
     * Log in as a staff, email and password required.
     * @return the staff's id
     */
    public static Integer trainerLogin(String email, String password) {
        String storedHashedPassword = null;

        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement ps = connect.prepareStatement("SELECT * FROM trainer WHERE email = ?");

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                trainerFnName = rs.getString("firstname");
                trainerLnName = rs.getString("lastname");
                trainerEmail = rs.getString("email");
                trainerSpeciality=  rs.getString("speciality");
                storedHashedPassword = rs.getString("password_hash");
            }

            if (storedHashedPassword != null) {
                //This will tell if the password entered during log in is verified or not
                boolean read = BCrypt.checkpw(password, storedHashedPassword);
                if (read) {
                    System.out.println("Logged in successfully.");
                    return (rs.getInt("trainer_id"));
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
    // should search for members in another table( registered with particular trainer) not members table should
    // search for people who are in their session
    public static List<MemberProfile> memberSearch(String first_name, String last_name){
        List<MemberProfile> members = new ArrayList<>();
        String query = "SELECT * FROM members WHERE first_name = ? OR last_name = ?";
        try {
            Connection connection = FitnessApp.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, first_name);
            ps.setString(2, last_name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Map the retrieved data to MemberProfile objects
                int memberId = rs.getInt("member_id");
                String memberFirstName = rs.getString("first_name");
                String memberLastName = rs.getString("last_name");
                String email = rs.getString("email");
                // Create a MemberProfile object and add it to the list
                members.add(new MemberProfile(memberId, memberFirstName, memberLastName, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Should be in a different table not trainers the members should look at that table to book a session
     * @param firstname
     * @param lastname
     * @param specialty
     * @param dayOfWeek
     */

    public static void scheduleAvailabilty( String firstname, String lastname,String specialty,
                                     String dayOfWeek,String timePeriod) {
        String query = "INSERT INTO trainerSchedule (firstname, lastname, speciality, dayOfWeek," +
                "timePeriod) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, firstname);
            ps.setString(2, lastname);
            ps.setString(3, specialty);
            ps.setString(4, dayOfWeek);
            ps.setString(5, timePeriod);
            ps.executeUpdate();
            System.out.println("Successfully set Availability");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error setting availability.");
        }
    }

    /**
     * get trainers first name
     * @return String
     */

    public static String getFnName(){
        return trainerFnName;
    }

    /**
     * get trainers last name
     * @return String
     */

    public static String getLnName(){
        return trainerLnName;
    }

    /**
     * get trainers speciality
     * @return String
     */
    public static String getSpeciality(){
        return trainerSpeciality;
    }








}
