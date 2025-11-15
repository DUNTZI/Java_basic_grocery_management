
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class pr_pm extends JPanel {

    private static final String PR_FILE = "TXT/pr.txt";
    private JTable prTable, prDetailsTable;
    private DefaultTableModel tableModel, detailsTableModel;
    private JButton updateButton;
    private JTextField searchField;
    private JButton searchButton;

    public pr_pm() {
        setLayout(new BorderLayout());

        String[] columnNames = {"PR ID", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        prTable = new JTable(tableModel);
        prTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        prTable.getColumn("Actions").setCellEditor(new ButtonEditor());
        prTable.setRowHeight(40);

        JScrollPane scrollPane = new JScrollPane(prTable);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);

        String[] detailsColumnNames = {"PR ID", "Item ID", "Supplier ID", "Quantity Requested", "Required Date", "Raised By", "Status"};

        detailsTableModel = new DefaultTableModel(detailsColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow only "Status" column (index 4) to be editable
                return column == 6;
            }
        };

        prDetailsTable = new JTable(detailsTableModel);
        prDetailsTable.setRowHeight(30);
        JScrollPane detailsScrollPane = new JScrollPane(prDetailsTable);
        detailsScrollPane.setBorder(new EmptyBorder(10, 20, 10, 20));

        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Approved", "Rejected"});
        prDetailsTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(statusComboBox));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchPR());

        searchPanel.add(new JLabel("Search by PR ID: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updatePRData());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(updateButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(detailsScrollPane, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        loadPRData();
    }

    private void loadPRData() {
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(PR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0) {
                    tableModel.addRow(new Object[]{data[0], "View/Delete"});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPR() {
        String searchId = searchField.getText().trim();
        if (searchId.isEmpty()) {
            loadPRData();
            return;
        }

        tableModel.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader(PR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(searchId)) {
                    tableModel.addRow(new Object[]{data[0], "View/Delete"});
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewPR(String prId) {
        detailsTableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(PR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 7 && data[0].equals(prId)) {
                    detailsTableModel.addRow(new Object[]{
                        data[0], // PR ID
                        data[1], // Item ID (was at index 4)
                        data[2], // Supplier ID (was at index 1)
                        data[3], // Quantity Requested (was at index 2)
                        data[4], // Required Date (was at index 3)
                        data[5], // Raised By (was at index 5)
                        data[6] // Status (was at index 6)
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePRData() {
        if (detailsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No PR selected to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String prId = detailsTableModel.getValueAt(0, 0).toString();
        String updatedItemId = detailsTableModel.getValueAt(0, 1).toString();
        String updatedSupplierId = detailsTableModel.getValueAt(0, 2).toString();
        String updatedQuantity = detailsTableModel.getValueAt(0, 3).toString();
        String updatedRequiredDate = detailsTableModel.getValueAt(0, 4).toString();
        String updatedRaisedBy = detailsTableModel.getValueAt(0, 5).toString();
        String updatedStatus = detailsTableModel.getValueAt(0, 6).toString();

        File inputFile = new File(PR_FILE);
        StringBuilder updatedContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data[0].equals(prId)) {
                    updatedContent.append(prId).append("|")
                            .append(updatedItemId).append("|") // Item ID comes second now
                            .append(updatedSupplierId).append("|")
                            .append(updatedQuantity).append("|")
                            .append(updatedRequiredDate).append("|")
                            .append(updatedRaisedBy).append("|")
                            .append(updatedStatus).append("\n");
                } else {
                    updatedContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile, false))) {
            bw.write(updatedContent.toString().trim());
            JOptionPane.showMessageDialog(this, "PR data updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ButtonRenderer extends JPanel implements TableCellRenderer {

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton viewButton = new JButton("View");
            JButton deleteButton = new JButton("Delete");
            panel.add(viewButton);
            panel.add(deleteButton);
            return panel;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private String prId;

        public ButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");

            viewButton.addActionListener(e -> {
                fireEditingStopped();
                viewPR(prId);
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deletePR(prId);
            });

            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            prId = table.getValueAt(row, 0).toString();
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "View/Delete";
        }

        private void deletePR(String prId) {
            int confirm = JOptionPane.showConfirmDialog(
                    pr_pm.this,
                    "Delete PR " + prId + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    File inputFile = new File(PR_FILE);
                    StringBuilder newContent = new StringBuilder();

                    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (!line.startsWith(prId + "|")) {
                                newContent.append(line).append("\n");
                            }
                        }
                    }

                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
                        bw.write(newContent.toString().trim());
                    }

                    detailsTableModel.setRowCount(0);
                    loadPRData();
                    JOptionPane.showMessageDialog(pr_pm.this, "PR deleted successfully");

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            pr_pm.this,
                            "Error deleting PR: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
}
