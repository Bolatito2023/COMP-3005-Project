package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeParseException;

public class Member {

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

