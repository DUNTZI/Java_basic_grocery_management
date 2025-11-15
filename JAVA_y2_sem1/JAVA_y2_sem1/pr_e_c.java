// pr_e_c.java: Logic and controller code (e.g. file reading/writing, data handling, button actions)
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;

public class pr_e_c {

    private static final String PR_FILE = "TXT/pr.txt";
    public final pr_e view;

    public pr_e_c(pr_e view) {
        this.view = view;
    }

    public void loadPRData() {
        view.tableModel.setRowCount(0);
        view.fullPrData.clear();
        File file = new File(PR_FILE);
        if (!file.exists()) { /* Handle file creation if needed */ return; }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                // File: pr_id|item_id|supplier_id|quantity|req_date|raised_by_id|status
                if (data.length >= 7) { // Need at least 7 fields
                    view.fullPrData.add(data);
                    view.tableModel.addRow(new Object[]{data[0], "View/Delete"}); // Add only PR ID to top table
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error reading PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Clear details table whenever main list is loaded/reloaded
        if (view.detailsTableModel != null) {
            view.detailsTableModel.setRowCount(0);
        }
    }

    public void addPR() {
        System.out.println("Attempting to add PR...");
        // Get selected item and supplier IDs from ComboBoxes
        String itemName = view.itemIDComboBox.getSelectedItem().toString();
        String supplierID = view.supplierIDComboBox.getSelectedItem().toString().trim();
        String itemID = "";
        if (view.itemNameAndSupplierToIdMap.containsKey(itemName)) {
            itemID = view.itemNameAndSupplierToIdMap.get(itemName).getOrDefault(supplierID, "");
        }
        String quantityStr = view.quantityField.getText().trim();
        String requiredDate = view.requiredDateField.getText().trim();

        // Get logged-in user info
        String raisedByID = login_c.currentUserId;
        String raisedByRole = login_c.currentRole;

        // Only allow if role is sm or am
        if (!(raisedByRole.equalsIgnoreCase("sm") || raisedByRole.equalsIgnoreCase("am"))) {
            JOptionPane.showMessageDialog(view, "Only Sales Manager or Administrator can raise a PR.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (itemID.isEmpty() || supplierID.isEmpty() || quantityStr.isEmpty() || requiredDate.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill all fields!", "Input Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Validation failed: Empty fields.");
            return;
        }
        int quantity;
        try { quantity = Integer.parseInt(quantityStr); if (quantity <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(view, "Quantity must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Validation failed: Invalid quantity.");
            return; }
        if (!requiredDate.matches("\\d{2}-\\d{2}-\\d{4}")) { JOptionPane.showMessageDialog(view, "Required Date must be DD-MM-YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Validation failed: Invalid date format.");
            return; }

        String status = "Pending";
        int prID = generatePrID();
        System.out.println("Generated PR ID: " + prID);
        if (prID < 0) { JOptionPane.showMessageDialog(view, "Could not generate PR ID.", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error generating PR ID.");
            return; }
        String formattedPrID = String.format("%04d", prID);
        String newPR = String.join("|", formattedPrID, itemID, supplierID, String.valueOf(quantity), requiredDate, raisedByID, status);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PR_FILE, true))) {
            System.out.println("Writing to file: " + newPR);
            writer.write(newPR);
            writer.newLine();
            JOptionPane.showMessageDialog(view, "Purchase Requisition (PR ID: " + formattedPrID + ") added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            view.itemIDComboBox.setSelectedIndex(-1); view.supplierIDComboBox.setSelectedIndex(-1); view.quantityField.setText("");
            view.requiredDateField.setText("");
            loadPRData();
            view.showCard(pr_e.PR_LIST_CARD);
            System.out.println("PR added successfully.");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error saving PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error saving PR: " + e.getMessage());
        }
    }

    public void searchPR() {
        String searchId = view.searchField.getText().trim();
        if (searchId.isEmpty()) {
            view.sorter.setRowFilter(null); // Clear filter if search is empty
        } else {
            try {
                // Filter PR ID column (index 0) case-insensitive
                RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)^" + Pattern.quote(searchId) + "$", 0); // Exact match
                // Or starts with: RowFilter.regexFilter("(?i)^" + Pattern.quote(searchId), 0);
                view.sorter.setRowFilter(rf);
            } catch (java.util.regex.PatternSyntaxException e) {
                JOptionPane.showMessageDialog(view, "Invalid search pattern", "Search Error", JOptionPane.ERROR_MESSAGE);
                view.sorter.setRowFilter(null);
            }
        }
        // Clear details when searching
        view.detailsTableModel.setRowCount(0);
    }

    // Called by ButtonEditor's View button
    public void viewPR(String prId) {
        view.detailsTableModel.setRowCount(0); // Clear previous details
        String[] dataToView = null;
        // Find the full data corresponding to the prId
        for (String[] data : view.fullPrData) {
            if (data.length > 0 && data[0].equals(prId)) {
                dataToView = data;
                break;
            }
        }

        if (dataToView != null && dataToView.length >= 7) {
            // Populate the details table with the single row found
            String itemName = view.itemIdToNameMap.getOrDefault(dataToView[1], dataToView[1]);
            view.detailsTableModel.addRow(new Object[]{
                    dataToView[0], // PR ID
                    itemName,      // Item Name instead of Item ID
                    dataToView[2], // Supplier ID
                    dataToView[3], // Quantity
                    dataToView[4], // Required Date
                    mapIDToRole(dataToView[5]), // Raised By ID (Map to Role Name for display)
                    dataToView[6]  // Status
            });
        } else {
            JOptionPane.showMessageDialog(view, "Could not find details for PR ID: " + prId, "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Called by ButtonEditor's Delete button
    public void deletePR(String prId) {
        int confirm = JOptionPane.showConfirmDialog(view,"Delete PR " + prId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (deleteRecordFromFile(PR_FILE, prId)) {
                view.detailsTableModel.setRowCount(0); // Clear details table
                loadPRData(); // Reload the main table data
                JOptionPane.showMessageDialog(view, "PR " + prId + " deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(view, "Error deleting PR " + prId + " from file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updatePRData() {
        if (view.prDetailsTable.isEditing()) {
            view.prDetailsTable.getCellEditor().stopCellEditing(); // Ensure edits are saved to model
        }

        if (view.detailsTableModel.getRowCount() != 1) {
            JOptionPane.showMessageDialog(view, "Please view a single PR in the details table before updating.", "Update Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get data from the details table (which should have only 1 row)
        String prId = view.detailsTableModel.getValueAt(0, 0).toString();
        String itemName = view.detailsTableModel.getValueAt(0, 1).toString();
        String supplierID = view.detailsTableModel.getValueAt(0, 2).toString();
        String updatedItemId = "";
        if (view.itemNameAndSupplierToIdMap.containsKey(itemName)) {
            updatedItemId = view.itemNameAndSupplierToIdMap.get(itemName).getOrDefault(supplierID, itemName);
        }
        String updatedSupplierId = view.detailsTableModel.getValueAt(0, 2).toString();
        String updatedQuantity = view.detailsTableModel.getValueAt(0, 3).toString();
        String updatedRequiredDate = view.detailsTableModel.getValueAt(0, 4).toString();
        String updatedRaisedByDisplay = view.detailsTableModel.getValueAt(0, 5).toString(); // e.g. "fish - 1012"
        // Extract user ID from "username - userID"
        String updatedRaisedById;
        if (updatedRaisedByDisplay.contains(" - ")) {
            updatedRaisedById = updatedRaisedByDisplay.substring(updatedRaisedByDisplay.lastIndexOf(" - ") + 3).trim();
        } else if (updatedRaisedByDisplay.startsWith("Unknown (") && updatedRaisedByDisplay.endsWith(")")) {
            // fallback for "Unknown (1012)"
            updatedRaisedById = updatedRaisedByDisplay.substring(9, updatedRaisedByDisplay.length() - 1).trim();
        } else {
            updatedRaisedById = updatedRaisedByDisplay.trim();
        }
        String updatedStatus = view.detailsTableModel.getValueAt(0, 6).toString(); // Status shouldn't change here usually

        // --- Optional: Add validation for the updated data ---
        if (updatedItemId.isEmpty() || updatedSupplierId.isEmpty() || updatedQuantity.isEmpty() || updatedRequiredDate.isEmpty() || updatedRaisedByDisplay.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Updated fields cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE); return;
        }
        try { int qty = Integer.parseInt(updatedQuantity); if(qty <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(view, "Updated quantity must be a positive number.", "Validation Error", JOptionPane.ERROR_MESSAGE); return; }
        if (!updatedRequiredDate.matches("\\d{2}-\\d{2}-\\d{4}")) { JOptionPane.showMessageDialog(view, "Updated date must be DD-MM-YYYY.", "Validation Error", JOptionPane.ERROR_MESSAGE); return; }
        // --- End Validation ---

        // Create the updated line string based on file format
        String updatedLine = String.join("|", prId, updatedItemId, updatedSupplierId, updatedQuantity, updatedRequiredDate, updatedRaisedById, updatedStatus);

        if (updateRecordInFile(PR_FILE, prId, updatedLine)) {
            JOptionPane.showMessageDialog(view, "PR " + prId + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            view.detailsTableModel.setRowCount(0); // Clear details table
            loadPRData(); // Reload main table data
        } else {
            JOptionPane.showMessageDialog(view, "Error updating PR " + prId + " in file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =================================================
    // --- Helper Methods ---
    // =================================================

    // Generic method to delete a record identified by the first field (ID)
    private boolean deleteRecordFromFile(String filename, String idToDelete) {
        File inputFile = new File(filename);
        File tempFile;
        try {
            tempFile = File.createTempFile("temp_del_", ".txt", inputFile.getParentFile());
        } catch (IOException e) {
            System.err.println("Could not create temporary file: " + e);
            return false;
        }

        boolean deleted = false;
        boolean found = false;

        if (!inputFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(idToDelete)) {
                    found = true;
                    if (data.length >= 7 && data[6].equalsIgnoreCase("Approved")) {
                        // Do not allow deletion if status is Approved
                        JOptionPane.showMessageDialog(view,
                            "Cannot delete PR ID " + idToDelete + " because it is already Approved.",
                            "Delete Not Allowed", JOptionPane.WARNING_MESSAGE);
                        writer.write(line); // Keep the original line
                        writer.newLine();
                    } else {
                        deleted = true;
                        // Do not write this line (delete)
                    }
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing file for deletion: " + e);
            if (tempFile.exists()) tempFile.delete();
            return false;
        }

        if (!found) {
            if (tempFile.exists()) tempFile.delete();
            return false;
        }

        try {
            if (!inputFile.delete()) {
                System.gc();
                Thread.sleep(100);
                if (!inputFile.delete())
                    throw new IOException("Could not delete original: " + inputFile.getName());
            }
            if (!tempFile.renameTo(inputFile)) {
                throw new IOException("Could not rename temp file");
            }
            return deleted;
        } catch (IOException | SecurityException | InterruptedException e) {
            System.err.println("Error replacing file: " + e);
            return false;
        }
    }

    // Generic method to update a record identified by the first field (ID)
    private boolean updateRecordInFile(String filename, String idToUpdate, String newLine) {
        File inputFile = new File(filename);
        File tempFile;
        try { tempFile = File.createTempFile("temp_upd_", ".txt", inputFile.getParentFile()); }
        catch (IOException e) { System.err.println("Could not create temporary file: " + e); return false; }

        boolean found = false;
        if (!inputFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(idToUpdate)) {
                    writer.write(newLine); // Write the updated line
                    writer.newLine();
                    found = true;
                } else {
                    writer.write(line); writer.newLine(); // Write other lines as they are
                }
            }
        } catch (IOException e) { System.err.println("Error processing file for update: " + e); if(tempFile.exists()) tempFile.delete(); return false; }

        if (!found) { if(tempFile.exists()) tempFile.delete(); return false; } // ID wasn't in file

        // Replace original file
        try {
            if (!inputFile.delete()) { System.gc(); Thread.sleep(100); if (!inputFile.delete()) throw new IOException("Could not delete original: " + inputFile.getName()); }
            if (!tempFile.renameTo(inputFile)) { /* Add copy fallback if needed */ throw new IOException("Could not rename temp file"); }
            return true; // Success
        } catch (IOException | SecurityException | InterruptedException e) { System.err.println("Error replacing file: " + e); return false; }
    }

    // Map role name to user ID (adjust IDs as needed)
    private String mapRoleToID(String role) {
        return switch (role) {
            case "Sales Manager" -> "1002"; // From example data
            case "Administrator" -> "1001"; // Assumption
            // Add other roles as needed
            default -> "UNKNOWN"; // Or handle error
        };
    }

    // Map user ID to "username - userID" by looking up users.txt
    private String mapIDToRole(String userID) {
        File file = new File("TXT/users.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2 && parts[0].trim().equals(userID.trim())) {
                    return parts[1].trim() + " - " + userID.trim();
                }
            }
        } catch (IOException e) {
            // Optionally log error
        }
        return "Unknown (" + userID + ")";
    }

    // Generate next PR ID (ensure it's 4 digits starting from 5001)
    private int generatePrID() {
        int maxID = 5000; // Start checking from 5000
        File file = new File(PR_FILE);
        if (!file.exists()) return 5001; // First ID

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && !data[0].trim().isEmpty()) {
                    try {
                        int currentID = Integer.parseInt(data[0].trim());
                        if (currentID > maxID) maxID = currentID;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error reading PR file for ID generation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1; // Indicate error
        }
        return maxID + 1;
    }
}