package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Date;

public class FitnessApp {
    private static String username;

    private static String password;
    private static int portNumber;

    private static Member member;

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getPortNumber() { return portNumber; }

    private void setUsername(String name) { username = name; }
    private void setPassword(String pass) { password = pass; }
    private void setPortNumber(int port) { portNumber = port; }

    public FitnessApp() {
        username = "postgres";
        password = "gbemisola23$";
        portNumber = 2244;
        member = new Member();
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
                    1) Member Registration
                    2) Trainer Login
                    3) Staff Login
                    4) Member Login
                    0) Exit""");
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    System.out.println("Enter your First name:");
                    String first_name = scanner.nextLine();
                    System.out.println("Enter your Last name:");
                    String last_name = scanner.nextLine();
                    System.out.println("Enter your Email name:");
                    String email = scanner.nextLine();
                    System.out.println("Enter your Join Date (yyyy-mm-dd):");
                    String joinDateInput = scanner.nextLine(); // Read the date input from the user
                    Date joinDate = Date.valueOf(joinDateInput); // Parse the input string into a Date object
                    System.out.println("Enter your password:");
                    String password = scanner.nextLine();

                    member.memberRegister(first_name,last_name,email,joinDate ,password);
                    System.out.println("\n");
                        break;

                case "2":

                case "3":
                    // Handle staff login
                    break;
                case "4":
                    System.out.println("Enter your Email name:");
                    String email_login = scanner.nextLine();

                    System.out.println("Enter your password:");
                    String password_login = scanner.nextLine();
                    member.memberLogin(email_login,password_login);
                    System.out.println("\n");
                    break;
                case "0":
                    System.out.println("Exiting program.");
                    return; // Exit the program
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }
}