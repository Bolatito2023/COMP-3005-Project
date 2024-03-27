package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Scanner;
import java.sql.Date;

public class FitnessApp {
    private static String username;

    private static String password;
    private static int portNumber;

    private static Member member;
    private static Staff staff;

    private static Trainer trainer;



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
        staff = new Staff();
        trainer = new Trainer();
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
                    System.out.println("\n");
                    System.out.println("1) Member Registration\n" +
                            "2) Trainer Registration\n" +
                            "3) Staff Registration\n" +
                            "0) Return");
                    String reg_option = scanner.nextLine();
                    if(Objects.equals(reg_option, "0")) break;
                    System.out.println("Enter your First name:");
                    String reg_fn = scanner.nextLine();
                    System.out.println("Enter your Last name:");
                    String reg_ln = scanner.nextLine();
                    System.out.println("Enter your Email name:");
                    String reg_email = scanner.nextLine();
                    String reg_speciality = "";
                    if(Objects.equals(reg_option, "2")) {
                        System.out.println("Enter your Speciality:");// This is only asked if the user is a trainer
                        reg_speciality = scanner.nextLine();
                    }
                    System.out.println("Enter your password:");
                    String reg_password = scanner.nextLine();
                    switch(reg_option) {
                        case "1":
                            Member.memberRegister(reg_fn,reg_ln,reg_email, Date.valueOf(LocalDate.now()) ,reg_password);
                            break;
                        case "2":
                            Trainer.trainerRegister(reg_fn, reg_ln, reg_email,reg_speciality, reg_password);
                            break;
                        case "3":
                            Staff.staffRegister(reg_fn, reg_ln, reg_email, reg_password);
                        default:
                            System.out.println("Invalid option, please try again.");
                    }


                    break;
                case "2":
                    System.out.println("\n");
                    System.out.println("Enter your Email name:");
                    String tEmail_login = scanner.nextLine();

                    System.out.println("Enter your password:");
                    String tPassword_login = scanner.nextLine();
                    tempId = Trainer.trainerLogin(tEmail_login,tPassword_login);
                    if(tempId != null) { currentId = tempId; }
                    trainerFunction();
                    //Staff.CheckMaintenance(currentId); //Just to test the function
                    break;

                case "3":
                    System.out.println("\n");
                    System.out.println("Enter your Email name:");
                    String staffLogin_email = scanner.nextLine();
                    System.out.println("Enter your password:");
                    String staffLogin_password = scanner.nextLine();
                    tempId = Staff.staffLogin(staffLogin_email,staffLogin_password);
                    if(tempId != null) { currentId = tempId; }
                    //Staff.CheckMaintenance(currentId); //Just to test the function
                    break;
                case "4":
                    System.out.println("Enter your Email name:");
                    String memberLogin_email = scanner.nextLine();
                    System.out.println("Enter your password:");
                    String memberLogin_password = scanner.nextLine();
                    tempId = Member.memberLogin(memberLogin_email,memberLogin_password);
                    if(tempId != null) { currentId = tempId; memberFunction(currentId); }

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
     * This function will be called once the trainer has logged in
     * They have the option to set availability or view
     */

    public static void trainerFunction(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n");
        System.out.println("Welcome " + trainer.getFnName() +"\n"+ """
                    1) Set Availability
                    2) View Member Profile
                    0) Exit""");

        String option = scanner.nextLine().trim();
        switch (option) {
            case "1":
                System.out.println("Enter your day of the week");
                String dayOfWeek = scanner.nextLine();

                System.out.println("Enter your time period");
                System.out.println("e.g 11:30am - 1:00pm");
                String timePeriod = scanner.nextLine();
                Trainer.scheduleAvailabilty(Trainer.getFnName(),Trainer.getLnName(),Trainer.getSpeciality(),dayOfWeek,timePeriod);
                break;
            case "2":
                break;
                //Trainer.memberSearch();
            case "0":
                return; // Exit the trainer function and return to the main menu
            default:
                System.out.println("Invalid option, please try again.");
        }
    }

    /**
     * This method is called when the member successfully logs in
     * More options will be added such as viewing fitness goals e.t.c
     */
    public static void memberFunction(Integer id){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n");
            System.out.println("Welcome " + Member.getMemberName() + "\n" +
                    "1) View Trainer Schedule\n" +
                    "2) Update Information\n" +
                    "0) Exit");


            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    Member.getTrainerSchedule();
                    break;
                case "2":
                    System.out.println("Enter your new First name:");
                    String new_fn = scanner.nextLine();
                    System.out.println("Enter your new Last name:");
                    String new_ln = scanner.nextLine();
                    System.out.println("Enter your new Email name:");
                    String new_email = scanner.nextLine();
                    System.out.println("Enter your new password:");
                    String new_password = scanner.nextLine();

                    Member.updatePersonalInformation(new_fn,new_ln,new_email,new_password,id);
                    break;
                case "0":
                    return; // Exit the member function and return to the main menu
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

}