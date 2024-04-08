package org.example;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.time.LocalDate;

import java.util.Objects;

import java.time.format.DateTimeParseException;

import org.mindrot.jbcrypt.BCrypt;
public class Member {

    private static String memberName = "";

    private static int routineId = 0;
    private static String routineName = " ";
    private static int durationMinutes = 0;
    private static int intensityLevel = 0;
    private static int member_id =0;

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
                member_id = rs.getInt("member_id");

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





    /**
     * This method returns the trainers schedule
     */

    public static void getTrainerSchedule(){

        try {
            Connection connect = FitnessApp.getConnection();

            Statement st = connect.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM trainerschedule"); // Process the result
            while(rs.next()){
                int trainer_id = rs.getInt("trainer_id");

                String dayOfWeek= rs.getString("dayOfWeek");
                Time startTime = rs.getTime("starting_time");

                Time endTime= rs.getTime("end_time");


                System.out.println("Trainer ID: " + trainer_id + ", Day Available: " + dayOfWeek + ", Start Time: "+ startTime + ", End Time: " + endTime
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

    /**
     * Method to make payment for a bill  Member should pay then staff should look at what member paid then subtract
     * If the amount paid subtracted from the amount in database is 0 then status set to paid.
     * @param amount The amount to be paid.
     * @return true if the payment is successful, false otherwise.
     */
    public static boolean makePayment(double amount, int member_id) {
        try {
            Connection connect = FitnessApp.getConnection();

            // Check current amount
            String selectQuery = "SELECT amount FROM bills WHERE member_id = ?";
            PreparedStatement selectPs = connect.prepareStatement(selectQuery);
            selectPs.setInt(1, member_id);
            ResultSet rs = selectPs.executeQuery();

            if (rs.next()) {
                double currentAmount = rs.getDouble("amount");
                if (currentAmount == amount) {
                    // Update amount to zero
                    String updateQuery = "UPDATE bills SET amount = 0 WHERE member_id = ?";
                    PreparedStatement updatePs = connect.prepareStatement(updateQuery);
                    updatePs.setInt(1, member_id);
                    updatePs.executeUpdate();

                    System.out.println("Payment processed successfully. Amount set to zero.");
                    return true;
                } else {
                    System.out.println("Failed to process payment. The entered amount does not match the current amount.");
                }
            } else {
                System.out.println("Failed to process payment. No matching unpaid bill found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This function helps the member view their billing history.
     * @param id
     */
    public static void viewBillHistory(int id){
        try {
            Connection connect = FitnessApp.getConnection();

            PreparedStatement ps = connect.prepareStatement("SELECT * FROM bills WHERE member_id = ?");

            //ps.setInt(1, member_id);
            ResultSet rs = ps.executeQuery();

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


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function parses the date the user enters to return a sql date
     * @param date
     * @return
     */
    public static java.sql.Date parseDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = sdf.parse(date);
            return new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    /** This function sets the members fitness goals
     * Deadline should be in format yyyy-MM-dd
     * @param member_id
     * @param goal_type
     * @param goal_value
     * @param deadline
     */
    public static void setFitnessGoal(int member_id, String goal_type, String goal_value, String deadline) {
        String query = "INSERT INTO goals (member_id, goal_type, goal_value, deadline) VALUES (?, ?, ?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);
            java.util.Date deadlineUtilDate = parseDate(deadline); // Call the parseDate method
            java.sql.Date deadlineSqlDate = new java.sql.Date(deadlineUtilDate.getTime()); // Convert java.util.Date to java.sql.Date
            ps.setInt(1, member_id);
            ps.setString(2, goal_type);
            ps.setString(3, goal_value);
            ps.setDate(4, deadlineSqlDate);
            ps.executeUpdate();
            System.out.println("Fitness goal set successfully for user " + getMemberName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * This function updates the members fitness goals
     * write date as yyyy-MM-dd
     * @param goal_value
     * @param deadline
     * @param deadline
     */
    public static void updateFitnessGoal(int member_id, String goal_value, String deadline, String status) {
        try {
            Connection connection = FitnessApp.getConnection();

            PreparedStatement statement = connection.prepareStatement("UPDATE goals SET goal_value = ?, deadline = ?, status = ? WHERE member_id = ?");
            statement.setString(1, goal_value);
            statement.setDate(2, parseDate(deadline));
            statement.setString(3, status);
            statement.setInt(4, member_id);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Fitness Goal updated successfully.");
            } else {
                System.out.println("Failed to update Fitness Goal.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function records the Members health metrics
     * @param member_id
     * @param weight
     * @param height
     * @param blood_pressure
     * @param record_date
     */
    public static void recordHealthMetrics(int member_id, double weight, double height, double blood_pressure,Date record_date) {
        String query = "INSERT INTO healthMetrics (member_id, weight, height, blood_pressure, record_date) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, member_id);
            ps.setDouble(2, weight);
            ps.setDouble(3, height);
            ps.setDouble(4, blood_pressure);
            ps.setDate(5, record_date); // Set the record date at index 5
            ps.executeUpdate();
            System.out.println("Health Metrics successfully set ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * This function updates the Members health metrics
     * @param member_id
     * @param weight
     * @param height
     * @param blood_pressure
     * @param record_date
     */
    public static void updateHealthMetrics(int member_id, double weight, double height, double blood_pressure, Date record_date) {
        try {
            Connection connection = FitnessApp.getConnection();


            PreparedStatement statement = connection.prepareStatement("UPDATE healthMetrics SET weight = ?, height = ?, blood_pressure = ?," +
                    "record_date = ? WHERE member_id = ?");
            statement.setDouble(1, weight);
            statement.setDouble(2, height);
            statement.setDouble(3, blood_pressure);
            statement.setDate(4, record_date);
            statement.setInt(5, member_id);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Fitness Goal updated successfully.");
            } else {
                System.out.println("Failed to Fitness Goal");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * This function searches for particular exercises based on the category
     * The user is prompted a question that asks if they want to add the exercise
     * to their list
     * @param category Category of the type of exercise
     */
    public static void searchUpExercise(String category){

        Scanner scanner = new Scanner(System.in);
        System.out.println("Search and select exercises in category '" + category + "':");

        try (Connection connection = FitnessApp.getConnection()) {
            String query = "SELECT * FROM exercise_routine WHERE category = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            System.out.println("Available exercises:");

            while (rs.next()) {
                routineId = rs.getInt("routine_id");
                routineName = rs.getString("routine_name");
                durationMinutes = rs.getInt("duration_minutes");
                intensityLevel = rs.getInt("intensity_level");
                System.out.println("Routine ID: " + routineId + ", Routine Name: " + routineName + ", Duration (minutes): " + durationMinutes + ", Intensity Level: " + intensityLevel);



            }System.out.println("Enter the routine ID of the exercise to add (or 0 to finish):");
            int input = scanner.nextInt();
            while (input != 0) {
                if (routineIdExists(input, category)) {
                    System.out.println("Do you want to add this exercise to your exercise list? (yes/no)");
                    String response = scanner.next();
                    if (response.equalsIgnoreCase("yes")) {
                        add(member_id,input);
                    } else {
                        System.out.println("Exercise not added.");
                    }
                } else {
                    System.out.println("Invalid routine ID. Please enter a valid routine ID.");
                }
                System.out.println("Enter the routine ID of the exercise to add (or 0 to finish):");
                input = scanner.nextInt();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void add(int member_id, int routine_id) {
        try (Connection connection = FitnessApp.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement("SELECT routine_name, category, duration_minutes, intensity_level FROM exercise_routine WHERE routine_id = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO my_exercise_routine (member_id, routine_name, category, duration_minutes, intensity_level) VALUES (?, ?, ?, ?, ?)")) {

            selectStatement.setInt(1, routine_id);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    String routineName = resultSet.getString("routine_name");
                    String category = resultSet.getString("category");
                    int durationMinutes = resultSet.getInt("duration_minutes");
                    int intensityLevel = resultSet.getInt("intensity_level");

                    insertStatement.setInt(1, member_id);
                    insertStatement.setString(2, routineName);
                    insertStatement.setString(3, category);
                    insertStatement.setInt(4, durationMinutes);
                    insertStatement.setInt(5, intensityLevel);
                    insertStatement.executeUpdate();

                    System.out.println("Exercise routine added successfully for member with ID " + member_id);
                } else {
                    System.out.println("No exercise routine found with the provided ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean routineIdExists(int routineId, String category) {
        try (Connection connection = FitnessApp.getConnection()) {
            String query = "SELECT routine_id FROM exercise_routine WHERE routine_id = ? AND category = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, routineId);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // If rs.next() returns true, it means the routine ID exists for the given category.
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // If an exception occurs or if rs.next() returns false, routine ID doesn't exist.
    }



    public static void displayExerciseRoutines(int memberId, String category) {
        try (Connection connection = FitnessApp.getConnection()) {
            String query = "SELECT * FROM my_exercise_routine WHERE member_id = ? AND category = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, memberId);
            ps.setString(2,category);

            ResultSet rs = ps.executeQuery();

            System.out.println("----------------------------------------------------------------");
            while (rs.next()) {
               String  routineName = rs.getString("routine_name");
               int durationMinutes = rs.getInt("duration_minutes");
               int  intensityLevel = rs.getInt("intensity_level");
               System.out.println("Routine Name: " + routineName + ", Duration (minutes): " + durationMinutes + ", Intensity Level: " + intensityLevel);


            }
            System.out.println("----------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    public static void displayFitnessAchievements(int memberId) {

            String query = "SELECT * FROM goals WHERE member_id = ? AND status = 'achieved'";
            try (Connection connection = FitnessApp.getConnection();
                 PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, member_id);
                ResultSet rs = ps.executeQuery();

                System.out.println("\n");
                System.out.println("----------------------------------------------------------------");
                while (rs.next()) {
                    // Retrieve and display goal information
                    int goalId = rs.getInt("goal_id");
                    int member_id = rs.getInt("member_id");
                    String goalType = rs.getString("goal_type");
                    String goalValue = rs.getString("goal_value");
                    String deadline = rs.getString("deadline");
                    String status = rs.getString("status");
                    // Display other goal attributes as needed
                    System.out.println("Goal ID: " + goalId + " Member ID: " + member_id + ", Type: " + goalType + ", Value: " + goalValue + ", Deadline: " + deadline +
                            ", Status: " + status);
                }

                System.out.println("----------------------------------------------------------------");
            } catch (SQLException e) {
                e.printStackTrace();
            }

    }

    public static void displayHealthStatistics(int member_id) {
        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement("SELECT * FROM healthMetrics WHERE member_id = ?");
            ps.setInt(1, member_id);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n");
            System.out.println("----------------------------------------------------------------");
            while (rs.next()) {
                double weight = rs.getDouble("weight");
                double height = rs.getDouble("height");
                double blood_pressure = rs.getDouble("blood_pressure");
                Date record_date = rs.getDate("record_date");

                System.out.println("Weight: " + weight + "Kg, Height: " + height + "cm, Blood Pressure: " + blood_pressure + "mmHg, Record Date: " + record_date);
            }
            System.out.println("----------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Add a session to sessions table, according to member's settings
     * It will ask member the type of sessions he wishes to have, if there are any, display them.
     * If the member does not interested in those, create a custom one.
     * If there are no available rooms, it will create a new one
     * @param memberId represents the member's ID
     */
    public static void requestSession(int memberId) {
        Scanner scanner = new Scanner(System.in);
        String category;

        while (true) {
            System.out.println("""
                What type of session are you interested?
                1) STRENGTH
                2) CARDIO
                3) WATER-BASED
                4) MINDBODY
                0) Return""");

            String option = scanner.nextLine();

            switch(option) {
                case "1": category = "STRENGTH"; break;
                case "2": category = "CARDIO"; break;
                case "3": category = "WATER-BASED"; break;
                case "4": category = "MINDBODY"; break;
                case "0": return;
                default:
                    System.out.println("Invalid input, please try again");
                    continue;
            }
            break;
        }
        String query = "SELECT s.session_id, " +
                "CONCAT(t.firstname, ' ', t.lastname) AS trainer_name, " +
                "r.room_id AS room, " +
                "s.date, " +
                "s.starting_time, " +
                "s.end_time, " +
                "COUNT(sm.member_id) AS member_count " +
                "FROM sessions s " +
                "JOIN trainers t ON s.trainer_id = t.trainer_id " +
                "JOIN rooms r ON s.room_id = r.room_id " +
                "LEFT JOIN session_members sm ON s.session_id = sm.session_id " +
                "WHERE s.type = 'PUBLIC' AND r.equipment_type = '" + category + "' " +
                "GROUP BY s.session_id, t.firstname, t.lastname, r.room_id " +
                "ORDER BY s.date, s.starting_time;";

        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            if (rs.isBeforeFirst()) {

                List<Integer> availableSessions = new ArrayList<>();

                System.out.println("Available " + category + " sessions:");
                System.out.println("----------------------------------------------------------------");
                while (rs.next()) {
                    int sessionId = rs.getInt("session_id");
                    String trainerName = rs.getString("trainer_name");
                    int room = rs.getInt("room");
                    Date date = rs.getDate("date");
                    Time startingTime = rs.getTime("starting_time");
                    Time endTime = rs.getTime("end_time");
                    int memberCount = rs.getInt("member_count");
                    System.out.println("Session ID: " + sessionId +
                            ", Trainer Name: " + trainerName +
                            ", Room: " + room +
                            ", Date: " + date +
                            ", Starting Time: " + startingTime +
                            ", End Time: " + endTime +
                            ", Number of People: " + memberCount);
                    availableSessions.add(sessionId);
                }
                System.out.println("----------------------------------------------------------------");
                System.out.println("Would you like to join any of them? If yes, enter the session ID(only one), enter NO to request a new session.");
                String response = scanner.next();
                Integer sessionId = null;

                while(!Objects.equals(response, "no") && !availableSessions.contains(sessionId)) {
                    try {
                        sessionId = Integer.parseInt(response);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input, please try again");
                        response = scanner.next();
                    }
                }

                if(!response.equalsIgnoreCase("no")) {
                    String verifyQuery = "SELECT COUNT(*) FROM sessions WHERE session_id = ?";
                    String checkEnrollmentQuery = "SELECT COUNT(*) FROM session_members WHERE session_id = ? AND member_id = ?";

                    PreparedStatement verifyStmt = connect.prepareStatement(verifyQuery);
                    PreparedStatement checkEnrollmentStmt = connect.prepareStatement(checkEnrollmentQuery);

                    //Check if the session exists
                    verifyStmt.setInt(1, sessionId);
                    ResultSet verifyRs = verifyStmt.executeQuery();
                    if (verifyRs.next() && verifyRs.getInt(1) == 0) {
                        System.out.println("Session does not exist.");
                        return;
                    }
                    //Check if member already enrolled
                    checkEnrollmentStmt.setInt(1, sessionId);
                    checkEnrollmentStmt.setInt(2, memberId);
                    ResultSet checkEnrollmentRs = checkEnrollmentStmt.executeQuery();
                    if (checkEnrollmentRs.next() && checkEnrollmentRs.getInt(1) > 0) {
                        System.out.println("You have already enrolled in the session.");
                        return;
                    }
                    //Insert into session_members table
                    String enrollQuery = "INSERT INTO session_members (session_id, member_id) VALUES (?, ?)";
                    PreparedStatement enrollStmt = connect.prepareStatement(enrollQuery);
                    enrollStmt.setInt(1, sessionId);
                    enrollStmt.setInt(2, memberId);
                    enrollStmt.executeUpdate();
                    System.out.println("Enrolled successfully.");
                }
                else {
                    //Get date
                    LocalDate date = null;
                    while (true) {
                        System.out.println("Enter the date of the session (YYYY-MM-DD):");
                        String dateInput = scanner.next();

                        try {
                            date = LocalDate.parse(dateInput);
                            if (date.isBefore(LocalDate.now())) {
                                System.out.println("The date entered has already passed, please try again.");
                                continue;
                            }
                            break;
                        } catch(DateTimeParseException e) {
                            System.out.println("Invalid input, please try again.");
                        }
                    }

                    //Get day of week
                    String dayOfWeek = String.valueOf(date.getDayOfWeek());

                    //Get trainer
                    int trainer;
                    List<Integer> trainers = new ArrayList<>();

                    String trainerQuery = "SELECT DISTINCT t.trainer_id, t.firstname, t.lastname FROM trainers t " +
                            "JOIN trainerSchedule ts ON t.trainer_id = ts.trainer_id " +
                            "WHERE t.speciality = '" + category + "' AND ts.dayOfWeek = '" + dayOfWeek + "' ";

                    PreparedStatement trainerPs = connect.prepareStatement(trainerQuery);

                    ResultSet trainerRs = trainerPs.executeQuery();
                    if(!trainerRs.isBeforeFirst()) {
                        System.out.println("Sorry, no available trainers at this time.");
                        return;
                    }
                    System.out.println("----------------------------------------------------------------");
                    while (trainerRs.next()) {
                        int id = trainerRs.getInt("trainer_id");
                        String name = trainerRs.getString("firstname") + " " + trainerRs.getString("lastname");
                        trainers.add(id);
                        System.out.println("Trainer ID: " + id + ", Name: " + name);
                    }
                    System.out.println("----------------------------------------------------------------");
                    scanner.nextLine();
                    while(true) {
                        System.out.println("Enter the trainer's id for your session:");
                        String trainerId = scanner.nextLine();
                        if(!trainers.contains(Integer.parseInt(trainerId))) {
                            System.out.println("Invalid input, please try again.");
                        }
                        else {
                            trainer = Integer.parseInt(trainerId);
                            break;
                        }
                    }

                    //Get time
                    Time startTime = null;
                    Time endTime = null;

                    String scheduleQuery = "SELECT * FROM trainerSchedule WHERE trainer_id = ? AND dayOfWeek = ?";
                    PreparedStatement schedulePs = connect.prepareStatement(scheduleQuery);
                    schedulePs.setInt(1,trainer);
                    schedulePs.setString(2, dayOfWeek);
                    ResultSet scheduleRs = schedulePs.executeQuery();

                    System.out.println("----------------------------------------------------------------");
                    while (scheduleRs.next()) {

                        Time startingTime = scheduleRs.getTime("starting_time");
                        Time endingTime = scheduleRs.getTime("end_time");
                        System.out.println("Available Time - Start: " + startingTime + ", End: " + endingTime);
                    }
                    System.out.println("----------------------------------------------------------------");
                    boolean validPeriod = false;
                    while(!validPeriod) {
                        System.out.println("Enter your desired starting time (HH:MM): ");
                        String startInput = scanner.nextLine() + ":00";
                        try {
                            startTime = Time.valueOf(startInput);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid time format, please try again.");
                            continue;
                        }

                        System.out.println("Enter your desired ending time (HH:MM): ");
                        String endInput = scanner.nextLine() + ":00";
                        try {
                            endTime = Time.valueOf(endInput);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid time format, please try again.");
                            continue;
                        }

                        if(!checkTimeConflict(trainer, dayOfWeek, startTime, endTime)) {
                            validPeriod = true;
                        }
                        else {
                            System.out.println("The selected period conflicts with an existing schedule, please try again.");
                        }
                    }

                    //Get room
                    int room = -1;
                    String roomQuery = "SELECT r.room_id " +
                            "FROM rooms r " +
                            "LEFT JOIN sessions s ON r.room_id = s.room_id AND s.date = ? " +
                            "                    AND NOT(s.end_time <= '" + startTime + "' OR s.starting_time >= '" + endTime + "') " +
                            "WHERE r.equipment_type = '" + category + "' AND r.need_maintenance = FALSE " +
                            "      AND s.session_id IS NULL " +
                            "GROUP BY r.room_id " +
                            "HAVING COUNT(s.session_id) = 0 " +
                            "LIMIT 1;";
                    PreparedStatement roomPs = connect.prepareStatement(roomQuery);
                    roomPs.setDate(1, Date.valueOf(date));
                    ResultSet roomRs = roomPs.executeQuery();
                    if(roomRs.next()) {
                        room = roomRs.getInt("room_id");
                    }
                    else {
                        int staffId = -1;
                        String randomQuery = "SELECT staff_id FROM staff ORDER BY RANDOM() LIMIT 1";
                        String newRoomQuery = "INSERT INTO rooms (equipment_type, need_maintenance, staff_id) VALUES ('" + category + "', false, ?)";
                        PreparedStatement randomPs = connect.prepareStatement(randomQuery);
                        PreparedStatement newRoomPs = connect.prepareStatement(newRoomQuery);
                        ResultSet randomRs = randomPs.executeQuery();

                        while(randomRs.next()) {
                            staffId = randomRs.getInt("staff_id");
                        }

                        newRoomPs.setInt(1, staffId);
                        newRoomPs.executeUpdate();

                        roomPs = connect.prepareStatement(roomQuery);
                        roomPs.setDate(1, Date.valueOf(date));
                        roomRs = roomPs.executeQuery();
                        while(roomRs.next()) {
                            room = roomRs.getInt("room_id");
                            System.out.println("Created room " + room + " for you.");
                        }
                    }

                    //Get type
                    String sessionType = null;
                    while(sessionType == null) {
                        System.out.println("Would you like to make the session public or private?\n" +
                                "1) PUBLIC\n" +
                                "2) PRIVATE");

                        String option = scanner.nextLine();
                        sessionType = switch(option) {
                            case "1" -> "PUBLIC";
                            case "2" -> "PRIVATE";
                            default -> null;
                        };
                    }

                    Session.addSession(room, trainer, date, startTime, endTime, sessionType, memberId);
                }
            }
            else {
                //Get date
                LocalDate date = null;
                while (true) {
                    System.out.println("Enter the date of the session (YYYY-MM-DD):");
                    String dateInput = scanner.next();

                    try {
                        date = LocalDate.parse(dateInput);
                        if (date.isBefore(LocalDate.now())) {
                            System.out.println("The date entered has already passed, please try again.");
                            continue;
                        }
                        break;
                    } catch(DateTimeParseException e) {
                        System.out.println("Invalid input, please try again.");
                    }
                }

                //Get day of week
                String dayOfWeek = String.valueOf(date.getDayOfWeek());

                //Get trainer
                int trainer;
                List<Integer> trainers = new ArrayList<>();
                System.out.println(category);
                System.out.println(dayOfWeek);
                String trainerQuery = "SELECT DISTINCT t.trainer_id, t.firstname, t.lastname FROM trainers t " +
                        "JOIN trainerSchedule ts ON t.trainer_id = ts.trainer_id " +
                        "WHERE t.speciality = '" + category + "' AND ts.dayOfWeek = '" + dayOfWeek + "' ";
                System.out.println(trainerQuery);
                PreparedStatement trainerPs = connect.prepareStatement(trainerQuery);
                //trainerPs.setString(1,"'" + category + "'");
                //trainerPs.setString(2, "'" + dayOfWeek + "'");
                ResultSet trainerRs = trainerPs.executeQuery();
                if(!trainerRs.isBeforeFirst()) {
                    System.out.println("Sorry, no available trainers at this time.");
                    return;
                }
                System.out.println("----------------------------------------------------------------");
                while (trainerRs.next()) {
                    int id = trainerRs.getInt("trainer_id");
                    String name = trainerRs.getString("firstname") + " " + trainerRs.getString("lastname");
                    trainers.add(id);
                    System.out.println("Trainer ID: " + id + ", Name: " + name);
                }
                System.out.println("----------------------------------------------------------------");
                scanner.nextLine();
                while(true) {
                    System.out.println("Enter the trainer's id for your session:");
                    String trainerId = scanner.nextLine();
                    if(!trainers.contains(Integer.parseInt(trainerId))) {
                        System.out.println("Invalid input, please try again.");
                    }
                    else {
                        trainer = Integer.parseInt(trainerId);
                        break;
                    }
                }

                //Get time
                Time startTime = null;
                Time endTime = null;

                String scheduleQuery = "SELECT * FROM trainerSchedule WHERE trainer_id = ? AND dayOfWeek = ?";
                PreparedStatement schedulePs = connect.prepareStatement(scheduleQuery);
                schedulePs.setInt(1,trainer);
                schedulePs.setString(2, dayOfWeek);
                ResultSet scheduleRs = schedulePs.executeQuery();

                System.out.println("----------------------------------------------------------------");
                while (scheduleRs.next()) {

                    Time startingTime = scheduleRs.getTime("starting_time");
                    Time endingTime = scheduleRs.getTime("end_time");
                    System.out.println("Available Time - Start: " + startingTime + ", End: " + endingTime);
                }
                System.out.println("----------------------------------------------------------------");
                boolean validPeriod = false;
                while(!validPeriod) {
                    System.out.println("Enter your desired starting time (HH:MM): ");
                    String startInput = scanner.nextLine() + ":00";
                    try {
                        startTime = Time.valueOf(startInput);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid time format, please try again.");
                        continue;
                    }

                    System.out.println("Enter your desired ending time (HH:MM): ");
                    String endInput = scanner.nextLine() + ":00";
                    try {
                        endTime = Time.valueOf(endInput);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid time format, please try again.");
                        continue;
                    }

                    if(!checkTimeConflict(trainer, dayOfWeek, startTime, endTime)) {
                        validPeriod = true;
                    }
                    else {
                        System.out.println("The selected period conflicts with an existing schedule, please try again.");
                    }
                }

                //Get room
                int room = -1;
                String roomQuery = "SELECT r.room_id " +
                        "FROM rooms r " +
                        "LEFT JOIN sessions s ON r.room_id = s.room_id AND s.date = ? " +
                        "                    AND NOT(s.end_time <= '" + startTime + "' OR s.starting_time >= '" + endTime + "') " +
                        "WHERE r.equipment_type = '" + category + "' AND r.need_maintenance = FALSE " +
                        "      AND s.session_id IS NULL " +
                        "GROUP BY r.room_id " +
                        "HAVING COUNT(s.session_id) = 0 " +
                        "LIMIT 1;";
                PreparedStatement roomPs = connect.prepareStatement(roomQuery);
                roomPs.setDate(1, Date.valueOf(date));
                ResultSet roomRs = roomPs.executeQuery();
                if(roomRs.next()) {
                    room = roomRs.getInt("room_id");
                }
                else {
                    int staffId = -1;
                    String randomQuery = "SELECT staff_id FROM staff ORDER BY RANDOM() LIMIT 1";
                    String newRoomQuery = "INSERT INTO rooms (equipment_type, need_maintenance, staff_id) VALUES ('" + category + "', false, ?)";
                    PreparedStatement randomPs = connect.prepareStatement(randomQuery);
                    PreparedStatement newRoomPs = connect.prepareStatement(newRoomQuery);
                    ResultSet randomRs = randomPs.executeQuery();

                    while(randomRs.next()) {
                        staffId = randomRs.getInt("staff_id");
                    }

                    newRoomPs.setInt(1, staffId);
                    newRoomPs.executeUpdate();

                    roomPs = connect.prepareStatement(roomQuery);
                    roomPs.setDate(1, Date.valueOf(date));
                    roomRs = roomPs.executeQuery();
                    while(roomRs.next()) {
                        room = roomRs.getInt("room_id");
                        System.out.println("Created room " + room + " for you.");
                    }
                }

                //Get type
                String sessionType = null;
                while(sessionType == null) {
                    System.out.println("Would you like to make the session public or private?\n" +
                            "1) PUBLIC\n" +
                            "2) PRIVATE");

                    String option = scanner.nextLine();
                    sessionType = switch(option) {
                        case "1" -> "PUBLIC";
                        case "2" -> "PRIVATE";
                        default -> null;
                    };
                }

                Session.addSession(room, trainer, date, startTime, endTime, sessionType, memberId);
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }
    private static boolean checkTimeConflict(int trainerId, String dayOfWeek, Time startTime, Time endTime) {
        String query = "SELECT COUNT(*) FROM trainerSchedule WHERE trainer_id = ? AND dayOfWeek = '" + dayOfWeek + "' AND (end_time <= '" + startTime + "' OR starting_time >= '" + endTime + "')";
        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0; }

        } catch (SQLException e) { e.printStackTrace(); }

        return false;
    }
}





