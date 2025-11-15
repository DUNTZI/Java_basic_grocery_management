
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;

public class supplier_e extends JPanel {

    private JTextField nameField, contactField, emailField, addressField, itemField;
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private static final String FILE_NAME = "TXT/suppliers.txt";
    private int currentId = 3001;

    public supplier_e() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 450));

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Supplier Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.add(createInputPanel());
        mainPanel.add(createTablePanel());
        add(mainPanel, BorderLayout.CENTER);

        loadSuppliers();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidContactNumber(String contact) {
        return contact != null && contact.matches("^\\d{10,11}$");
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setPreferredSize(new Dimension(280, 180));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Supplier Info"));

        nameField = new JTextField(15);

        // Contact Number Field with strict validation
        contactField = new JTextField(15);
        contactField.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) {
                    return;
                }

                String digitsOnly = str.replaceAll("[^0-9]", "");
                if (digitsOnly.isEmpty()) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                if ((getLength() + digitsOnly.length()) > 11) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                super.insertString(offset, digitsOnly, attr);
            }
        });

        // Email Field with validation
        emailField = new JTextField(15);
        emailField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                String text = ((JTextField) input).getText().trim();
                if (!text.isEmpty() && !isValidEmail(text)) {
                    JOptionPane.showMessageDialog(input,
                            "Invalid email format!\nExample: name@example.com",
                            "Invalid Email",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                return true;
            }
        });

        addressField = new JTextField(15);
        itemField = new JTextField(15);

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Contact Number (10-11 digits):"));
        inputPanel.add(contactField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Address:"));
        inputPanel.add(addressField);
        inputPanel.add(new JLabel("Supply Category:"));
        inputPanel.add(itemField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addSupplier());
        inputPanel.add(addButton);

        return inputPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(750, 220));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Supplier List"));

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact No.", "Email", "Address", "Supply Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSupplier());
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSupplier());

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private void addSupplier() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String item = itemField.getText().trim();

        // Validate all required fields
        if (name.isEmpty() || contact.isEmpty() || email.isEmpty() || address.isEmpty() || item.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields marked with * are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate contact number format
        if (!isValidContactNumber(contact)) {
            JOptionPane.showMessageDialog(this,
                    "Contact number must be 10-11 digits only\nNo spaces or special characters allowed",
                    "Invalid Contact",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid email format!\nMust contain @ and valid domain\nExample: name@example.com",
                    "Invalid Email",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add to table if all validations pass
        tableModel.addRow(new Object[]{currentId++, name, contact, email, address, item});
        saveSuppliers();
        clearInputFields();
    }

    private void deleteSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this supplier?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
                saveSuppliers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow != -1) {
            String name = tableModel.getValueAt(selectedRow, 1).toString();
            String contact = tableModel.getValueAt(selectedRow, 2).toString();
            String email = tableModel.getValueAt(selectedRow, 3).toString();
            String address = tableModel.getValueAt(selectedRow, 4).toString();
            String item = tableModel.getValueAt(selectedRow, 5).toString();

            // Create edit dialog with current values
            supplier_v dialog = new supplier_v((JFrame) SwingUtilities.getWindowAncestor(this),
                    name, contact, email, address, item);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                String[] newData = dialog.getEditedData();

                // Validate edited data before saving
                if (!isValidContactNumber(newData[1])) {
                    JOptionPane.showMessageDialog(this,
                            "Contact number must be 10-11 digits",
                            "Invalid Contact",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!isValidEmail(newData[2])) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid email format",
                            "Invalid Email",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update table if validations pass
                tableModel.setValueAt(newData[0], selectedRow, 1);
                tableModel.setValueAt(newData[1], selectedRow, 2);
                tableModel.setValueAt(newData[2], selectedRow, 3);
                tableModel.setValueAt(newData[3], selectedRow, 4);
                tableModel.setValueAt(newData[4], selectedRow, 5);
                saveSuppliers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputFields() {
        nameField.setText("");
        contactField.setText("");
        emailField.setText("");
        addressField.setText("");
        itemField.setText("");
    }

    private void saveSuppliers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(
                        tableModel.getValueAt(i, 0) + "|"
                        + tableModel.getValueAt(i, 1) + "|"
                        + tableModel.getValueAt(i, 2) + "|"
                        + tableModel.getValueAt(i, 3) + "|"
                        + tableModel.getValueAt(i, 4) + "|"
                        + tableModel.getValueAt(i, 5));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving suppliers: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSuppliers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error creating suppliers file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        tableModel.addRow(parts);
                        currentId = Math.max(currentId, id + 1);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid ID format in suppliers file: " + parts[0]);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading suppliers: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
