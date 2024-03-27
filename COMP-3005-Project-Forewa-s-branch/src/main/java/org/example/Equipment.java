package org.example;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Equipment {
    /**
     * Add an equipment to table "equipment" with specificied information
     * @param name represents the name of the equipment
     * @param type represents the type of equipment. For example, Strength Training, Cardiovascular, Miscellanious
     * @param purchaseDate
     * @param lastMaintenanceDate
     * @param maintenanceFrequency
     * @param status
     */
    public static void equipmentRegister(String name, String type, Date purchaseDate, Date lastMaintenanceDate, String maintenanceFrequency, String status){
        String query = "INSERT INTO equipment(name, type, purchase_date, last_maintenance_date, maintenance_frequency, status) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection connect = FitnessApp.getConnection();
            PreparedStatement ps = connect.prepareStatement(query);

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setDate(3, purchaseDate);
            ps.setDate(4, lastMaintenanceDate);
            ps.setString(5, maintenanceFrequency);
            ps.setString(6, status);
            ps.executeUpdate();
            System.out.println("New Equipment Registered!");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Perform maintenance on an equipment and update last maintenance date.
     *
     * @param equipmentId represents the ID of the equipment being maintained.
     * @param maintenanceDate represents the date of maintenance.
     */
    public static void performMaintenance(int equipmentId, Date maintenanceDate) {
        String query = "UPDATE equipment SET last_maintenance_date = ? WHERE equipment_id = ?";

        try (Connection connect = FitnessApp.getConnection();
             PreparedStatement ps = connect.prepareStatement(query)) {

            ps.setDate(1, new java.sql.Date(maintenanceDate.getTime()));
            ps.setInt(2, equipmentId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Maintenance performed and last maintenance date updated.");
            } else {
                System.out.println("Failed to update maintenance date.");     //error handling
            }

        } catch (SQLException e) {
            System.err.println("Error performing maintenance: " + e.getMessage());
        }
    }
}
