package org.example;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
public class Member {

    private static String memberName = "";

    /**
     * Add a member to table "members" with specified information.
     * @param fn represents the member's first name.
     * @param ln represents the member's last name.
     * @param email represents the member's email address, this is unique.
     * @param password_hash represents the member's password.
     * Join date will be current date.
     * @return
     */
    public static void memberRegister(String fn, String ln, String email,Date join_date, String password_hash) {
        String query = "INSERT INTO members (firstname, lastname, email, join_date, password_hash) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, fn);
            ps.setString(2, ln);
            ps.setString(3, email);
            ps.setDate(4, join_date);
            ps.setString(5, BCrypt.hashpw(password_hash, BCrypt.gensalt()));
            ps.executeUpdate();
            System.out.println("New member registered.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     *Log in as a member, email and password required.
     * @return the member's id
     */
    public static Integer memberLogin(String email, String password) {
        String storedHashedPassword = null;

        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement ps = connect.prepareStatement("SELECT * FROM members WHERE email = ?");

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                memberName = rs.getString("firstname");

                storedHashedPassword = rs.getString("password_hash");
            }

            if (storedHashedPassword != null) {
                //This will tell if the password entered during log in is verified or not
                boolean read = BCrypt.checkpw(password, storedHashedPassword);
                if (read) {
                    System.out.println("Logged in successfully.");
                    return (rs.getInt("member_id"));
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
     * This method will help update members personal information
     * @param firstname
     * @param lastname
     * @param email
     * @param password_hash
     * @param member_id
     */
    public static void updatePersonalInformation(String firstname, String lastname, String email,String password_hash, int member_id) {
        try {
            Connection connection = FitnessApp.getConnection();

            PreparedStatement statement = connection.prepareStatement("UPDATE members SET firstname = ?, lastname = ?, email = ? , password_hash= ? WHERE member_id = ?");
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setString(4, BCrypt.hashpw(password_hash, BCrypt.gensalt()));
            statement.setInt(5, member_id);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                memberName= firstname;
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

    /**
     * This method returns the trainers schedule
     */

    public static void getTrainerSchedule(){

        try {
            Connection connect = FitnessApp.getConnection();

            Statement st = connect.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM trainerSchedule"); // Process the result
            while(rs.next()){
               // int trainer_id = rs.getInt("trainer_id");
                String first_name = rs.getString("firstname");
                String last_name = rs.getString("lastname");
                String speciality = rs.getString("speciality");
                String dayOfWeek= rs.getString("dayOfWeek");
                String timePeriod= rs.getString("timePeriod");


                System.out.println("First Name: " + first_name + ", Last Name: " + last_name
                        + ", Speciality: " + speciality + ", Day Available: " + dayOfWeek + ", Time Period: "+ timePeriod
               );
            }
            // Close resources
            rs.close();
            st.close();

            // Close the connection (in a real scenario, do this in a finally
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    /**
     * Used in the fitness app when welcoming user.
     * @return
     */
    public static String getMemberName(){
        return memberName;
    }



}

