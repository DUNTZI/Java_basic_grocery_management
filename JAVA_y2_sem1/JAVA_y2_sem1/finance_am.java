
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class finance_am extends JPanel {

    private JTable financeMainTable;
    private JTable financeDetailsTable;
    private DefaultTableModel mainTableModel;
    private DefaultTableModel detailsTableModel;

    private JTextField searchField;
    private JButton searchButton, updateButton;

    private List<String[]> financeData = new ArrayList<>();
    private static final String[] COLUMNS = {
        "Finance ID", "PO ID", "Approval Status", "Payment Status", "Payment Date", "Amount", "Verified By"
    };

    public finance_am() {
        setLayout(new BorderLayout());

        // --- TOP: Search Panel --- //
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchFinanceID());
        searchPanel.add(new JLabel("Search by Finance ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- CENTER: Main Table with View Buttons --- //
        String[] mainColumnNames = {"Finance ID", "Actions"};
        mainTableModel = new DefaultTableModel(mainColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        financeMainTable = new JTable(mainTableModel);
        financeMainTable.setRowHeight(40);
        financeMainTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        financeMainTable.getColumn("Actions").setCellEditor(new ButtonEditor(this));
        JScrollPane mainScroll = new JScrollPane(financeMainTable);
        mainScroll.setBorder(new EmptyBorder(10, 20, 10, 20));
        add(mainScroll, BorderLayout.CENTER);

        // --- SOUTH: Details Table + Update Button --- //
        detailsTableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Approval Status editable
            }
        };
        financeDetailsTable = new JTable(detailsTableModel);
        financeDetailsTable.setRowHeight(30);

        // ComboBox for Approval Status
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Approved", "Rejected"});
        financeDetailsTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusComboBox));

        JScrollPane detailsScroll = new JScrollPane(financeDetailsTable);
        detailsScroll.setBorder(new EmptyBorder(10, 20, 10, 20));

        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateApprovalStatus());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(detailsScroll, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // Load data into main table
        loadFinanceData();
    }

    private void loadFinanceData() {
        financeData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("TXT/finance.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split("\\|");
                if (row.length == COLUMNS.length) {
                    financeData.add(row);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading finance.txt", "Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshMainTable();
    }

    private void refreshMainTable() {
        mainTableModel.setRowCount(0);
        for (String[] row : financeData) {
            mainTableModel.addRow(new Object[]{row[0], "View"});
        }
    }

    private void showFinanceDetails(String financeId) {
        for (String[] row : financeData) {
            if (row[0].equals(financeId)) {
                detailsTableModel.setRowCount(0);
                detailsTableModel.addRow(row);
                return;
            }
        }
    }

    private void updateApprovalStatus() {
        if (detailsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No record selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String updatedStatus = (String) detailsTableModel.getValueAt(0, 2);
        String idToUpdate = (String) detailsTableModel.getValueAt(0, 0);

        for (String[] row : financeData) {
            if (row[0].equals(idToUpdate)) {
                row[2] = updatedStatus;
                break;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("TXT/finance.txt"))) {
            for (String[] row : financeData) {
                writer.write(String.join("|", row));
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Approval status updated.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to update file.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        refreshMainTable(); // Optional
    }

    private void searchFinanceID() {
        String query = searchField.getText().trim().toLowerCase();
        mainTableModel.setRowCount(0);
        for (String[] row : financeData) {
            if (row[0].toLowerCase().contains(query)) {
                mainTableModel.addRow(new Object[]{row[0], "View"});
            }
        }
    }

    // --- Renderer/Editor Classes for "Actions" column --- //
    class ButtonRenderer extends JPanel implements TableCellRenderer {

        private final JButton button;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT)); // Align right
            button = new JButton("View");
            add(button);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JPanel panel;
        private final JButton viewButton;
        private String financeId;

        public ButtonEditor(finance_am parent) {
            super(new JCheckBox());  // Dummy editor component

            panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            viewButton = new JButton("View");

            // Attach actual logic directly here!
            viewButton.addActionListener(e -> {
                parent.showFinanceDetails(financeId);  // Run immediately
                fireEditingStopped(); // Finish editing after action
            });

            panel.add(viewButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            financeId = table.getValueAt(row, 0).toString();  // Get ID
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "View";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Finance Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.add(new finance_am());
            frame.setVisible(true);
        });
    }
}
