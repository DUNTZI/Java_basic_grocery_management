
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class user_am extends JPanel {

    private JTable userTable, userDetailsTable;
    private DefaultTableModel tableModel, detailsTableModel;
    private JButton updateButton;
    private JTextField searchField;
    private JButton searchButton;

    // --- User Info Components ---
    private JTextField usernameField, passwordField, contactField, emailField;
    private JComboBox<String> roleComboBox;
    private JLabel userIdLabel;
    private JButton saveButton;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel userListPanel, userInfoPanel;

    public user_am() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create both pages (panels)
        userListPanel = createUserListPanel();
        userInfoPanel = createUserInfoPanel();

        // Add panels to CardLayout panel
        cardPanel.add(userListPanel, "UserListPage");
        cardPanel.add(userInfoPanel, "UserInfoPage");

        // Navigation Buttons
        JPanel navigationPanel = new JPanel();
        JButton goToUserListButton = new JButton("Go to User List");
        goToUserListButton.addActionListener(e -> cardLayout.show(cardPanel, "UserListPage"));

        JButton goToUserInfoButton = new JButton("Go to Add New User");
        goToUserInfoButton.addActionListener(e -> {
            resetUserInfoForm();
            cardLayout.show(cardPanel, "UserInfoPage");
        });

        navigationPanel.add(goToUserListButton);
        navigationPanel.add(goToUserInfoButton);

        // Add the cardPanel and navigation panel to the main layout
        add(navigationPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
    }

    // ------------------- User List Panel -------------------
    private JPanel createUserListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"ID", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        userTable = new JTable(tableModel);
        userTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Actions").setCellEditor(new ButtonEditor(this));
        userTable.setRowHeight(40);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Details table (for view/update)
        String[] detailsColumnNames = {"User ID", "Username", "Role", "Contact Number", "Email"};
        detailsTableModel = new DefaultTableModel(detailsColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        userDetailsTable = new JTable(detailsTableModel);
        userDetailsTable.setRowHeight(30);
        JScrollPane detailsScrollPane = new JScrollPane(userDetailsTable);
        // Set combo box editor for the Role column in details table
        String[] roles = {"am", "sm", "fm", "im", "pm"};
        JComboBox<String> roleComboBoxEditor = new JComboBox<>(roles);
        userDetailsTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(roleComboBoxEditor));

        detailsScrollPane.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchUser());
        searchPanel.add(new JLabel("Search by User ID: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Update button
        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateUserData());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(updateButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(detailsScrollPane, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        loadUserData();
        return panel;
    }

    // ------------------- User Info Panel -------------------
    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New User"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Labels and fields
        userIdLabel = new JLabel("User ID: " + UserController.generateNextUserId());
        usernameField = new JTextField(15);
        passwordField = new JTextField(15);
        contactField = new JTextField(15);
        emailField = new JTextField(15);
        roleComboBox = new JComboBox<>(new String[]{"am", "sm", "fm", "im", "pm"});

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveNewUser());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        panel.add(userIdLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Contact No:"), gbc);
        gbc.gridx = 1;
        panel.add(contactField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(saveButton, gbc);

        return panel;
    }

    // ------------------- Helper Methods -------------------
    // Load user IDs into the user table
    private void loadUserData() {
        tableModel.setRowCount(0);
        for (user_c user : UserController.loadUsers()) {
            tableModel.addRow(new Object[]{user.getId(), "Buttons"});
        }
    }

    // Search for a user by ID
    private void searchUser() {
        String searchId = searchField.getText().trim();
        if (searchId.isEmpty()) {
            loadUserData();
            return;
        }
        tableModel.setRowCount(0);
        for (user_c user : UserController.loadUsers()) {
            if (user.getId().equals(searchId)) {
                tableModel.addRow(new Object[]{user.getId(), "Buttons"});
                break;
            }
        }
    }

    // Save a new user using data from the form
    private void saveNewUser() {
        String userId = UserController.generateNextUserId();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getSelectedItem().toString();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user_c newUser = new user_c(userId, username, password, role, contact, email);
        if (UserController.isValidUser(newUser)) {
            UserController.addUser(newUser);
            JOptionPane.showMessageDialog(null, "User added successfully!");
        } else {
            // validation already shows error message inside isValidUser()
        }
        resetUserInfoForm();
        loadUserData();
    }

    // Clear the input form and update the next user ID
    private void resetUserInfoForm() {
        userIdLabel.setText("User ID: " + UserController.generateNextUserId());
        usernameField.setText("");
        passwordField.setText("");
        contactField.setText("");
        emailField.setText("");
        roleComboBox.setSelectedIndex(0);
    }

    // Update user data based on edited details in the details table
    private void updateUserData() {
        if (detailsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No user selected to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = detailsTableModel.getValueAt(0, 0).toString();
        String updatedUsername = detailsTableModel.getValueAt(0, 1).toString();
        String updatedRole = detailsTableModel.getValueAt(0, 2).toString();
        String updatedContact = detailsTableModel.getValueAt(0, 3).toString();
        String updatedEmail = detailsTableModel.getValueAt(0, 4).toString();

        // For simplicity, we keep the original password here.
        // In a real application, you might prompt for a new password.
        String originalPassword = null;
        for (user_c user : UserController.loadUsers()) {
            if (user.getId().equals(userId)) {
                originalPassword = user.getPassword();
                break;
            }
        }

        if (originalPassword == null) {
            JOptionPane.showMessageDialog(this, "Original user data not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        user_c updatedUser = new user_c(userId, updatedUsername, originalPassword, updatedRole, updatedContact, updatedEmail);
        if (UserController.isValidUser(updatedUser)) {
            UserController.updateUser(updatedUser);
            JOptionPane.showMessageDialog(null, "User updated successfully!");
        } else {
            // Do not show success message
        }
    }

    // Display user details when "View" is clicked (populating the details table)
    public void viewUser(String userId) {
        detailsTableModel.setRowCount(0); // Clear previous details
        for (user_c user : UserController.loadUsers()) {
            if (user.getId().equals(userId)) {
                detailsTableModel.addRow(new Object[]{
                    user.getId(), user.getUsername(), user.getRole(), user.getContact(), user.getEmail()
                });
                break;
            }
        }
    }

    // ------------ Renderer/Editor Classes for the "Buttons" in the table ------------
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panel.add(new JButton("View"));
            panel.add(new JButton("Delete"));
            return panel;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private String userId;

        public ButtonEditor(user_am parent) {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");

            viewButton.addActionListener(e -> {
                fireEditingStopped();
                parent.viewUser(userId);
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                int confirm = JOptionPane.showConfirmDialog(
                        user_am.this,
                        "Delete user " + userId + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    UserController.deleteUser(userId);
                    JOptionPane.showMessageDialog(user_am.this, "User deleted successfully.");
                    detailsTableModel.setRowCount(0);
                    loadUserData();
                }
            });

            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            userId = table.getValueAt(row, 0).toString();
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Buttons";
        }
    }

    // Main method to run the panel in a frame (for testing purposes)
    public static void main(String[] args) {
        JFrame frame = new JFrame("User Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new user_am());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
