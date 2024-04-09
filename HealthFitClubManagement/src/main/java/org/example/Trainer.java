package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.util.Scanner;


public class Trainer{
    //This will tell if the password entered during log in is verified or not
    private static boolean read = false;

    //These are made so that when trainer wants to set availabilty after logging in
    //the database will automatically put its info and just ask for day of the week and time period
    private static String trainerFnName = "";
    private static String trainerLnName = "";
    private static String trainerEmail = "";
    private static String trainerSpeciality = "";
    private static int trainer_id =0;



    /**
     * Add a trainer to table "trainer" with specified information.
     * @param fn represents the member's first name.
     * @param ln represents the member's last name.
     * @param email represents the member's email address, this is unique.
     * @param password_hash represents the staff's password.
     */
    public static void trainerRegister(String fn, String ln, String email,String speciality,String password_hash) {
        String query = "INSERT INTO trainers (firstname, lastname, email,speciality,  password_hash) VALUES (?, ?, ?, ?, ?)";

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

            PreparedStatement ps = connect.prepareStatement("SELECT * FROM trainers WHERE email = ?");

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                trainerFnName = rs.getString("firstname");
                trainerLnName = rs.getString("lastname");
                trainerEmail = rs.getString("email");
                trainerSpeciality=  rs.getString("speciality");
                trainer_id = rs.getInt("trainer_id");
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
    public static void memberSearch(String first_name, String last_name){
        List<MemberProfile> members = new ArrayList<>();

        String query = """
                        SELECT DISTINCT m.member_id, m.firstname, m.lastname
                        FROM members m
                        INNER JOIN session_members sm ON m.member_id = sm.member_id
                        INNER JOIN sessions s ON sm.session_id = s.session_id AND s.trainer_id = ?
                        WHERE m.firstname = ? OR m.lastname = ?;""";
        try {
            Connection connection = FitnessApp.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, trainer_id);
            ps.setString(2, first_name);
            ps.setString(3, last_name);
            ResultSet rs = ps.executeQuery();

            if(!rs.isBeforeFirst()) {
                System.out.println("No members found");
                return;
            }
            while (rs.next()) {
                // Map the retrieved data to MemberProfile objects
                int memberId = rs.getInt("member_id");
                String memberFirstName = rs.getString("firstname");
                String memberLastName = rs.getString("lastname");
                String email = rs.getString("email");
                // Create a MemberProfile object and add it to the list
                members.add(new MemberProfile(memberId, memberFirstName, memberLastName, email));
            }

            System.out.println("----------------------------------------------------------------");
            for (MemberProfile member : members) {
                System.out.println(member);
            }
            System.out.println("----------------------------------------------------------------");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the member's id for detail(only one), hit enter only to quit:");
            int memberId;
            while(true) {
                String input = scanner.nextLine();
                if(input.isEmpty()) {
                    return;
                }
                try {
                    memberId = Integer.parseInt(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, please try again.");
                }
            }
            Member.displayHealthStatistics(memberId);
            Member.displayFitnessAchievements(memberId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Should be in a different table not trainers the members should look at that table to book a session
     * @param trainerId
     * @param startTime
     * @param endTime
     * @param dayOfWeek
     */

    public static void scheduleAvailability(int trainerId, String dayOfWeek, Time startTime, Time endTime) {
        String query = "INSERT INTO trainerSchedule (trainer_id, dayOfWeek, starting_time, end_time) VALUES (?, ?, ?, ?)";
        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, trainerId);
            ps.setString(2, dayOfWeek);
            ps.setTime(3, startTime);
            ps.setTime(4, endTime);

            ps.executeUpdate();
            System.out.println("Successfully set availability");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error setting availability");
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

    public static int getTrainer_id(){
        return trainer_id;
    }

    /**
     * get trainers speciality
     * @return String
     */
    public static String getSpeciality(){
        return trainerSpeciality;
    }








}
