package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;


public class FitnessApp {
    private static String username;

    private static String password;
    private static int portNumber;


    private static Trainer trainer;



    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getPortNumber() { return portNumber; }

    private void setUsername(String name) { username = name; }
    private void setPassword(String pass) { password = pass; }
    private void setPortNumber(int port) { portNumber = port; }

    /**
     * Constructor for FitnessApp
     */
    public FitnessApp() {
        username = "postgres";
        password = "gbemisola23$";
        portNumber = 2244;


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
            String option = scanner.nextLine();
            switch (option) {
                case "1":
                    handleRegistration();
                    break;
                case "2":
                    System.out.println("Enter your Email name:");
                    String tEmail_login = scanner.nextLine();

                    System.out.println("Enter your password:");
                    String tPassword_login = scanner.nextLine();
                    tempId = Trainer.trainerLogin(tEmail_login,tPassword_login);
                    if(tempId != null) { currentId = tempId;
                    }
                    else { continue; }
                    trainerFunction();

                    break;

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
                    System.out.println("Enter your Email name:");
                    String memberLogin_email = scanner.nextLine();
                    System.out.println("Enter your password:");
                    String memberLogin_password = scanner.nextLine();
                    tempId = Member.memberLogin(memberLogin_email,memberLogin_password);
                    if(tempId != null) { currentId = tempId;  }
                    else { continue; }
                    memberFunction(currentId);

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

        while(true){
            System.out.println("Welcome " + trainer.getFnName() +"\n"+ """
                    1) Set Availability
                    2) View Member Profile
                    0) Exit""");

            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    System.out.println("Enter your day of the week");
                    String dayOfWeek = scanner.nextLine();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                    System.out.println("Enter your start time (Note it is in 24hrs)");
                    System.out.println("e.g., 1:00pm write 13:00");
                    String startInput = scanner.nextLine();
                    LocalTime startTime = LocalTime.parse(startInput, formatter);
                    Time startTimeSql = Time.valueOf(startTime);

                    System.out.println("Enter your end time (Note it is in 24hrs)");
                    String endInput = scanner.nextLine();
                    LocalTime endTime = LocalTime.parse(endInput, formatter);
                    Time endTimeSql = Time.valueOf(endTime);


                    Trainer.scheduleAvailability(Trainer.getTrainer_id(),dayOfWeek,startTimeSql,endTimeSql);
                    break;
                case "2":
                    System.out.println("Member's first name:");
                    String fn = scanner.nextLine();
                    System.out.println("Member's last name:");
                    String ln = scanner.nextLine();
                    Trainer.memberSearch(fn, ln);
                    break;
                case "0":
                    return; // Exit the trainer function and return to the main menu
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }


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
     * This method is called when the member successfully logs in
     * More options will be added such as viewing fitness goals e.t.c
     */
    public static void memberFunction(Integer id){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n");
            System.out.println("Welcome " + Member.getMemberName() + "\n" +
                    "1) View Trainer Schedule\n" +
                    "2) Update Personal Information\n" +
                    "3) Set Fitness Goals\n" +
                    "4) Update Fitness Goals\n" +
                    "5) Record Health Metrics\n" +
                    "6) Update Health Metrics\n" +
                    "7) View Bill\n" +
                    "8) Pay Bill\n" +
                    "9) Search Up Exercises \n" +
                    "10) Display DashBoard \n"+
                    "11) Request a session\n" +
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
                case "3":
                    System.out.println("Enter your fitness goal type (e.g., lose weight, gain muscle, etc.):");
                    String goal_type = scanner.nextLine();

                    System.out.println("Enter your fitness goal value (e.g., 5 kg, run 10 km, etc.):");
                    String goal_value = scanner.nextLine();

                    System.out.println("Enter your goal deadline (format: yyyy-MM-dd):");
                    String deadline = scanner.nextLine();

                    Member.setFitnessGoal(id,goal_type,goal_value,deadline);
                    break;
                case "4":
                    System.out.println("If achieved goal, enter 'achieved'. If not yet achieved, enter 'not yet achieved':");
                    String achieved = scanner.nextLine();

                    Member.updateFitnessGoal(id,achieved);
                    break;
                case "5":
                    System.out.println("Enter your weight:");
                    double weight = scanner.nextDouble();
                    scanner.nextLine(); // Consume the newline character

                    System.out.println("Enter your height:");
                    double height = scanner.nextDouble();
                    scanner.nextLine(); // Consume the newline character

                    System.out.println("Enter your blood pressure:");
                    double blood_pressure = scanner.nextDouble();
                    scanner.nextLine(); // Consume the newline character

                    Member.recordHealthMetrics(id,weight,height,blood_pressure,Date.valueOf(LocalDate.now()));
                    break;
                case "6":
                    System.out.println("Enter your weight:");
                    double new_weight = scanner.nextDouble();
                    scanner.nextLine(); // Consume the newline character

                    System.out.println("Enter your height:");
                    double new_height = scanner.nextDouble();
                    scanner.nextLine(); // Consume the newline character

                    System.out.println("Enter your blood pressure:");
                    double new_blood_pressure = scanner.nextDouble();
                    scanner.nextLine(); // Consume the newline character

                    Member.updateHealthMetrics(id, new_weight,new_height,new_blood_pressure,Date.valueOf(LocalDate.now()));
                    break;
                case "7":
                    Member.viewBillHistory(id);
                    break;
                case "8":
                    System.out.println("Enter your amount:");
                    double amount = scanner.nextDouble();
                    Member.makePayment(amount,id);
                    break;
                case "9":
                    System.out.println("Your Category Options:");
                    System.out.println("1) CARDIO");
                    System.out.println("2) STRENGTH");
                    System.out.println("3) WATER-BASED");
                    System.out.println("4) MINDBODY");
                    System.out.print("Enter the number corresponding to your choice: ");

                    String exerciseCategory = "";
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character
                    switch (choice) {
                        case 1:
                            exerciseCategory = "CARDIO";
                            break;
                        case 2:
                            exerciseCategory = "STRENGTH";
                            break;
                        case 3:
                            exerciseCategory = "WATER-BASED";
                            break;
                        case 4:
                            exerciseCategory = "MINDBODY";
                            break;
                        default:
                            System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                            return; // Exit method if choice is invalid
                    }
                    //scanner.nextLine(); // Consume the newline character
                    Member.searchUpExercise(exerciseCategory);
                    break;
                case "10":
                    Member.displayHealthStatistics(id);
                    Member.displayFitnessAchievements(id);
                    System.out.println("Choose a category:");
                    System.out.println("1) CARDIO");
                    System.out.println("2) STRENGTH");
                    System.out.println("3) WATER-BASED");
                    System.out.println("4) MINDBODY");
                    System.out.print("Enter the number corresponding to your choice: ");

                    String category;
                    int categoryChoice = scanner.nextInt();
                    switch (categoryChoice) {
                        case 1:
                            category = "CARDIO";
                            break;
                        case 2:
                            category = "STRENGTH";
                            break;
                        case 3:
                            category = "WATER-BASED";
                            break;
                        case 4:
                            category = "MINDBODY";
                            break;
                        default:
                            System.out.println("Invalid choice. Please select a number between 1 and 4.");
                            return; // Exit the method or handle the invalid input accordingly
                    }
                    scanner.nextLine(); // Consume the newline character
                    Member.displayExerciseRoutines(id, category);
                    break;
                case "11":
                    Member.requestSession(id);
                    break;
                case "0":
                    return; // Exit the member function and return to the main menu
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }
    /**
     * Display the interface when user log in as a staff
     * @param staffId represents the staff's ID
     */
    private static void staffInterface(int staffId) {
        String query = "SELECT firstname, lastname FROM staff WHERE staff_id = ?";

        try {
            Connection connect = getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();

            while(true) {

                String fn = null, ln = null;
                while(rs.next()) {
                    fn = rs.getString("firstname");
                    ln = rs.getString("lastname");
                }
                System.out.println("\nCurrent staff: " + fn + " " + ln);
                System.out.println("""
                    1) Maintenance status
                    2) Display requests
                    3) Display bill history
                    4) Display finished sessions
                    0) Logout
                    """);

                Scanner scanner = new Scanner(System.in);
                String option = scanner.nextLine();

                switch(option) {
                    case "1":
                        Staff.CheckMaintenance(staffId);
                        break;
                    case "2":
                        Staff.displaySessionRequests();
                        break;
                    case "3":
                        Staff.viewMemberBillHistory();
                        break;
                    case "4":
                        Staff.handleFinishedSessions();
                        break;
                    case "0": return;
                    default: System.out.println("Invalid option, please try again.");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }



}