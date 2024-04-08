package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Scanner;

//Private enum

public class FitnessApp {
    private static String username;

    private static String password;
    private static int portNumber;

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getPortNumber() { return portNumber; }

    private void setUsername(String name) { username = name; }
    private void setPassword(String pass) { password = pass; }
    private void setPortNumber(int port) { portNumber = port; }

    public FitnessApp() {
        username = "postgres";
        password = "M99996";
        portNumber = 3005;
    }

    /**
     * Connect to Postgresql with specific url, user and password.
     * @return A Connection object that represent the connection between JDBC drive and the database.
     * @throws SQLException for any database or drive related errors.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        String url = "jdbc:postgresql://localhost:" + portNumber + "/HealthAndFitnessClub";

        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args){
        FitnessApp app = new FitnessApp();
        Scanner scanner = new Scanner(System.in);
        Integer tempId;
        Integer currentId = null;


        System.out.println("Current DataBase Configuration:");
        System.out.println("Username:    " + app.getUsername()   + "\n" +
                           "Password:    " + app.getPassword()   + "\n" +
                           "Port Number: " + app.getPortNumber() + "\n");

        label:
        while (true) {
            System.out.println("""
                           Do you want to change the DataBase config?
                           1) YES
                           2) NO""");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("Enter new username:");
                    app.setUsername(scanner.nextLine().trim());

                    System.out.println("Enter new password:");
                    app.setPassword(scanner.nextLine().trim());

                    while (true) {
                        System.out.println("Enter new port number:");
                        if (scanner.hasNextInt()) {
                            app.setPortNumber(scanner.nextInt());
                            break;
                        } else {
                            scanner.next();
                            System.out.println("Invalid input. Please enter a number.");
                        }
                    }

                    System.out.println("Updated Database Configuration:");
                    System.out.println("Username:    " + app.getUsername() + "\n" +
                                       "Password:    " + app.getPassword() + "\n" +
                                       "Port Number: " + app.getPortNumber() + "\n");
                    break label;
                case "2":
                    break label;
                default:
                    System.out.println("Invalid input, please enter again.");
                    break;
            }
        }

        while (true) {
            System.out.println("""
                    Welcome to Health and Fitness Club!
                    1) Registration
                    2) Trainer Login
                    3) Staff Login
                    4) Member Login
                    0) Exit""");
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    handleRegistration();
                    break;
                case "2":

                case "3":
                    System.out.println("Enter your Email");
                    String staffLogin_email = scanner.nextLine();
                    System.out.println("Enter your password:");
                    String staffLogin_password = scanner.nextLine();
                    tempId = Staff.staffLogin(staffLogin_email,staffLogin_password);
                    if(tempId != null) { currentId = tempId; }
                    else { continue; }
                    staffInterface(currentId);
                    break;
                case "4":
                    System.out.println("Enter your Email:");
                    String memberLogin_email = scanner.nextLine();
                    System.out.println("Enter your password:");
                    String memberLogin_password = scanner.nextLine();
                    tempId = Member.memberLogin(memberLogin_email,memberLogin_password);
                    if(tempId != null) { currentId = tempId; }
                    else { continue; }
                    break;
                case "0":
                    System.out.println("Exiting program.");
                    return; // Exit the program
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    /**
     *Handle registration
     */
    private static void handleRegistration() {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("1) Member Registration\n" +
                               "2) Trainer Registration\n" +
                               "3) Staff Registration\n" +
                               "0) Return");
            String reg_option = scanner.nextLine();

            if(Objects.equals(reg_option, "0")) return;

            if (Objects.equals(reg_option, "1") || Objects.equals(reg_option, "2") || Objects.equals(reg_option, "3")) {
                System.out.println("Enter your First name:");
                String reg_fn = scanner.nextLine();
                System.out.println("Enter your Last name:");
                String reg_ln = scanner.nextLine();
                System.out.println("Enter your Email:");
                String reg_email = scanner.nextLine();
                System.out.println("Enter your password:");
                String reg_password = scanner.nextLine();

                switch(reg_option) {
                    case "1":
                        Member.memberRegister(reg_fn,reg_ln,reg_email, Date.valueOf(LocalDate.now()) ,reg_password);
                        return;
                    case "2":
                        System.out.println("What is your speciality?\n" +
                                           "1) STRENGTH\n" +
                                           "2) CARDIO\n" +
                                           "3) WATER-BASED\n" +
                                           "4) MINDBODY");
                        String reg_speciality = scanner.nextLine();
                        switch(reg_speciality) {
                            case "1":
                                reg_speciality = "STRENGTH";
                                break;
                            case "2":
                                reg_speciality = "CARDIO";
                                break;
                            case "3":
                                reg_speciality = "WATER-BASED";
                                break;
                            case "4":
                                reg_speciality = "MINDBODY";
                                break;
                            default:
                                System.out.println("Invalid option, please try again.");
                        }
                        Trainer.trainerRegister(reg_fn,reg_ln,reg_email, reg_speciality, reg_password);
                        return;
                    case "3":
                        Staff.staffRegister(reg_fn, reg_ln, reg_email, reg_password);
                        return;
                    default:
                        System.out.println("Invalid option, please try again.");
                }
            }
            else {System.out.println("Invalid option, please try again.");}
        }
    }

    /**
     * Display the interface when user log in as a staff
     * @param staffId represents the staff's ID
     */
    private static void staffInterface(int staffId) {
        String query = "SELECT firstname, lastname FROM members WHERE staff_id = ?";

        try {
            Connection connect = getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();

            while(true) {
                System.out.println("\nCurrent staff: " + rs.getString("firstname") + " " + rs.getString("lastname"));
                System.out.println("""
                    1) Maintenance status
                    2) Display requests
                    3) Logout
                    """);

                Scanner scanner = new Scanner(System.in);
                String option = scanner.nextLine();

                switch(option) {
                    case "1":
                        Staff.CheckMaintenance(staffId);
                        break;
                    case"2":
                        Staff.displaySessionRequests();
                        break;
                    case"3": return;
                    default: System.out.println("Invalid option, please try again.");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}