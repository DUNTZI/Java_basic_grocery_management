import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

public class finance_e extends JPanel {

    private finance_c financeController;
    private JTabbedPane tabbedPane;
    private JPanel financeInfoPanel;
    private JPanel financeListPanel;

    private JTextField financeIdInfoField;
    private JComboBox<String> poIdComboBox;
    private JTextField paymentStatusInfoField;
    private JTextField paymentDateInfoField;
    private JTextField amountInfoField;
    private JButton addButton;
    private JLabel currentUserLabel;

    private DefaultTableModel financeListTableModelTop = new DefaultTableModel(new Object[]{"Finance ID", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : super.getColumnClass(columnIndex);
        }
    };
    private JTable financeListTableTop;
    private JScrollPane financeListScrollPaneTop;

    private JTextField financeIdUpdateField;
    private JTextField poIdUpdateField;
    private JTextField approvalStatusUpdateField;
    private JTextField paymentStatusUpdateField;
    private JTextField paymentDateUpdateField;
    private JTextField amountUpdateField;
    private JTextField verifiedByUpdateField;
    private JButton updateButton;

    // Regular expressions for validation
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    public finance_e() {
        this.financeController = new finance_c();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        financeInfoPanel = createFinanceInfoPanel();
        tabbedPane.addTab("Finance Info", financeInfoPanel);

        financeListPanel = createFinanceListPanel();
        tabbedPane.addTab("Finance List", financeListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        populateFinanceListTableTop();
    }

    private JPanel createFinanceInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        currentUserLabel = new JLabel("Current User: " + login_c.currentUsername + " (ID: " + login_c.currentUserId + ")");
        currentUserLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inputPanel.add(currentUserLabel, gbc);
        gbc.gridwidth = 1;

        JLabel financeIdLabel = new JLabel("Finance ID:");
        financeIdInfoField = new JTextField(15);
        financeIdInfoField.setEditable(false);
        financeIdInfoField.setText(financeController.generateNewFinanceId());

        JLabel poIdLabel = new JLabel("PO ID:");
        poIdComboBox = new JComboBox<String>() {
            @Override
            public void setSelectedItem(Object anObject) {
                if (!financeController.isPoPaid((String) anObject)) {
                    super.setSelectedItem(anObject);
                }
            }
        };
        refreshPoIdsComboBox();

        JLabel paymentStatusLabel = new JLabel("Payment Status:");
        paymentStatusInfoField = new JTextField(finance_c.STATUS_PAID);
        paymentStatusInfoField.setEditable(false);

        JLabel paymentDateLabel = new JLabel("Payment Date (dd-mm-yyyy):");
        paymentDateInfoField = new JTextField(15);

        JLabel amountLabel = new JLabel("Amount:");
        amountInfoField = new JTextField(15);

        addButton = new JButton("Add");

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(financeIdLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(financeIdInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(poIdLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(poIdComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(paymentStatusLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentStatusInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(paymentDateLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentDateInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        inputPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(amountInfoField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String financeId = financeIdInfoField.getText().trim();
                String poId = (String) poIdComboBox.getSelectedItem();
                String paymentStatus = paymentStatusInfoField.getText().trim();
                String paymentDate = paymentDateInfoField.getText().trim();
                String amountText = amountInfoField.getText().trim();

                // Validation checks
                if (financeId.isEmpty() || poId == null || poId.isEmpty() || paymentDate.isEmpty() || amountText.isEmpty()) {
                    JOptionPane.showMessageDialog(finance_e.this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (financeController.isPoPaid(poId)) {
                    JOptionPane.showMessageDialog(finance_e.this,
                            "This PO has already been paid and cannot be selected.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate payment date format (dd-mm-yyyy)
                if (!DATE_PATTERN.matcher(paymentDate).matches()) {
                    JOptionPane.showMessageDialog(finance_e.this,
                            "Invalid payment date format. Please use dd-mm-yyyy format.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate amount is numeric
                if (!AMOUNT_PATTERN.matcher(amountText).matches()) {
                    JOptionPane.showMessageDialog(finance_e.this,
                            "Invalid amount. Please enter a valid numeric value (e.g. 100 or 100.50).",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                finance_c.FinanceRecord newRecord = new finance_c.FinanceRecord(
                        financeId,
                        poId,
                        finance_c.STATUS_PENDING,
                        paymentStatus,
                        paymentDate,
                        amountText,
                        Integer.parseInt(login_c.currentUserId)
                );

                financeController.addFinanceRecord(newRecord);
                populateFinanceListTableTop();

                financeIdInfoField.setText(financeController.generateNewFinanceId());
                poIdComboBox.setSelectedIndex(0);
                paymentDateInfoField.setText("");
                amountInfoField.setText("");

                JOptionPane.showMessageDialog(finance_e.this,
                        "Finance record added successfully!\n"
                        + "Finance ID: " + financeId + "\n"
                        + "PO ID: " + poId + "\n"
                        + "Amount: " + amountText + "\n"
                        + "Verified By: " + login_c.currentUsername + " (ID: " + login_c.currentUserId + ")",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        return panel;
    }

    private void refreshPoIdsComboBox() {
        poIdComboBox.removeAllItems();
        List<String> poIds = financeController.loadPoIds();
        for (String poId : poIds) {
            poIdComboBox.addItem(poId);
        }
    }

    private JPanel createFinanceListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        financeListTableTop = new JTable(financeListTableModelTop) {
            @Override
            public void changeSelection(int row, int column, boolean toggle, boolean extend) {
                super.changeSelection(row, column, toggle, extend);
                if (convertColumnIndexToModel(column) == 1) {
                    ButtonPanel buttonPanel = (ButtonPanel) getValueAt(row, column);
                    buttonPanel.viewButton.doClick();
                }
            }
        };
        financeListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        financeListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(financeListTableTop));
        financeListScrollPaneTop = new JScrollPane(financeListTableTop);
        financeListScrollPaneTop.setMinimumSize(new Dimension(financeListScrollPaneTop.getMinimumSize().width, 50));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(financeListScrollPaneTop, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createFinanceListBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFinanceListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Finance Details");
        LineBorder topBorder = new LineBorder(Color.BLACK, 2);

        bottomPanel.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 10, 0),
                new CompoundBorder(topBorder, new EmptyBorder(5, 0, 5, 0))
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel financeIdLabel = new JLabel("Finance ID:");
        financeIdUpdateField = new JTextField(15);
        financeIdUpdateField.setEditable(false);

        JLabel poIdLabelUpdate = new JLabel("PO ID:");
        poIdUpdateField = new JTextField(15);
        poIdUpdateField.setEditable(false);

        JLabel approvalStatusLabel = new JLabel("Approval Status:");
        approvalStatusUpdateField = new JTextField(finance_c.STATUS_PENDING);
        approvalStatusUpdateField.setEditable(false);

        JLabel paymentStatusLabel = new JLabel("Payment Status:");
        paymentStatusUpdateField = new JTextField(finance_c.STATUS_PAID);
        paymentStatusUpdateField.setEditable(false);

        JLabel paymentDateLabel = new JLabel("Payment Date (dd-mm-yyyy):");
        paymentDateUpdateField = new JTextField(15);
        paymentDateUpdateField.setEditable(true);  // Made editable

        JLabel amountLabel = new JLabel("Amount:");
        amountUpdateField = new JTextField(15);
        amountUpdateField.setEditable(false);

        JLabel verifiedByLabel = new JLabel("Verified By:");
        verifiedByUpdateField = new JTextField(15);
        verifiedByUpdateField.setEditable(false);

        updateButton = new JButton("Update");

        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(financeIdLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(financeIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(poIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(poIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(approvalStatusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(approvalStatusUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(paymentStatusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(paymentStatusUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(paymentDateLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(paymentDateUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(amountUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(verifiedByLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(verifiedByUpdateField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(updateButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String financeId = financeIdUpdateField.getText();
                String newPaymentDate = paymentDateUpdateField.getText().trim();
                
                // Validate payment date format
                if (!DATE_PATTERN.matcher(newPaymentDate).matches()) {
                    JOptionPane.showMessageDialog(finance_e.this,
                            "Invalid payment date format. Please use dd-mm-yyyy format.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean found = false;
                for (finance_c.FinanceRecord record : financeController.getFinanceRecords()) {
                    if (record.getFinanceId().equals(financeId)) {
                        found = true;
                        int confirm = JOptionPane.showConfirmDialog(
                                finance_e.this,
                                "Are you sure you want to update Finance ID: " + record.getFinanceId() + "?\n" +
                                "New Payment Date: " + newPaymentDate,
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            // Update the payment date
                            record.setPaymentDate(newPaymentDate);
                            record.setVerifiedBy(Integer.parseInt(login_c.currentUserId));

                            financeController.updateFinanceRecord(record);

                            JOptionPane.showMessageDialog(finance_e.this,
                                    "Finance updated successfully!\n" +
                                    "New Payment Date: " + newPaymentDate + "\n" +
                                    "Verified By: " + login_c.currentUsername + " (ID: " + login_c.currentUserId + ")",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                            clearUpdateFields();
                            populateFinanceListTableTop();
                        } else {
                            JOptionPane.showMessageDialog(finance_e.this,
                                    "Update cancelled.",
                                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                }
                clearUpdateFields();
                if (!found) {
                    JOptionPane.showMessageDialog(finance_e.this,
                            "Could not find finance record.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return bottomPanel;
    }

    public void populateFinanceListTableTop() {
        financeListTableModelTop.setRowCount(0);
        for (finance_c.FinanceRecord record : financeController.getFinanceRecords()) {
            financeListTableModelTop.addRow(new Object[]{
                record.getFinanceId(),
                new ButtonPanel(record)
            });
        }
        refreshPoIdsComboBox();
    }

    private void populateUpdateFields(finance_c.FinanceRecord record) {
        financeIdUpdateField.setText(record.getFinanceId());
        poIdUpdateField.setText(record.getPoId());
        approvalStatusUpdateField.setText(record.getApprovalStatus());
        paymentStatusUpdateField.setText(record.getPaymentStatus());
        paymentDateUpdateField.setText(record.getPaymentDate());
        amountUpdateField.setText(record.getAmount());
        verifiedByUpdateField.setText(String.valueOf(record.getVerifiedBy()));
    }

    private void clearUpdateFields() {
        financeIdUpdateField.setText("");
        poIdUpdateField.setText("");
        approvalStatusUpdateField.setText(finance_c.STATUS_PENDING);
        paymentStatusUpdateField.setText(finance_c.STATUS_PAID);
        paymentDateUpdateField.setText("");
        amountUpdateField.setText("");
        verifiedByUpdateField.setText("");
    }

    private class ButtonPanel extends JPanel {

        public JButton viewButton;
        public JButton deleteButton;
        private finance_c.FinanceRecord record;

        public ButtonPanel(finance_c.FinanceRecord record) {
            this.record = record;
            setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");

            Dimension buttonSize = new Dimension(90, 14);
            viewButton.setPreferredSize(buttonSize);
            deleteButton.setPreferredSize(buttonSize);

            Font smallerFont = new Font(viewButton.getFont().getName(), Font.PLAIN, 13);
            viewButton.setFont(smallerFont);
            deleteButton.setFont(smallerFont);

            add(viewButton);
            
            // Only show delete button if status is Pending
            if (record.getApprovalStatus().equals(finance_c.STATUS_PENDING)) {
                add(deleteButton);
            }

            viewButton.setEnabled(true);
            deleteButton.setEnabled(true);

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateUpdateFields(record);
                    tabbedPane.setSelectedIndex(1);
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(
                            finance_e.this,
                            "Are you sure you want to delete Finance ID: " + record.getFinanceId() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        financeController.deleteFinanceRecord(record.getFinanceId());
                        populateFinanceListTableTop();
                        clearUpdateFields();
                        JOptionPane.showMessageDialog(finance_e.this,
                                "Finance record deleted successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ButtonPanel) {
                return (ButtonPanel) value;
            }
            return new JLabel();
        }
    }

    private class ButtonEditor extends DefaultCellEditor {

        private ButtonPanel panel;

        public ButtonEditor(JTable table) {
            super(new JCheckBox());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof ButtonPanel) {
                panel = (ButtonPanel) value;
                return panel;
            }
            return new JPanel();
        }

        @Override
        public Object getCellEditorValue() {
            return panel;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return false;
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Finance Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // For testing, set some dummy user info
            login_c.currentUserId = "7001";
            login_c.currentUsername = "finance_user";
            login_c.currentRole = "Finance";

            frame.getContentPane().add(new finance_e());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}