import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;

public class sales_e extends JPanel {

    private JTextField itemNameField, quantityField;
    private JComboBox<String> salesPersonCombo;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private static final String SALES_FILE = "TXT/sales_data.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";
    private static final String USERS_FILE = "TXT/users.txt";
    private List<Sales> salesList = new ArrayList<>();
    private List<String[]> itemsList = new ArrayList<>();
    private List<String> salesPersons = new ArrayList<>();
    private int nextSalesId = 4000;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public sales_e() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1000, 500));

        initializeUI();
        loadData();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Sales Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(createSalesInfoPanel());
        centerPanel.add(createSalesListPanel());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadData() {
        loadItems();
        loadSalesPersons();
        loadSales();
        findNextSalesId();
    }

    private void findNextSalesId() {
        nextSalesId = 4001;
        for (Sales sale : salesList) {
            try {
                int currentId = Integer.parseInt(sale.getSalesId());
                if (currentId >= nextSalesId) {
                    nextSalesId = currentId + 1;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid Sales ID format: " + sale.getSalesId());
            }
        }
    }

    private JPanel createSalesInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Sales Info"));
        panel.setLayout(new GridLayout(5, 2, 5, 5));

        panel.add(new JLabel("Item Name:"));
        itemNameField = new JTextField();
        itemNameField.setEditable(false);
        JButton selectItemButton = new JButton("Select");
        selectItemButton.addActionListener(e -> selectItem());

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(itemNameField, BorderLayout.CENTER);
        itemPanel.add(selectItemButton, BorderLayout.EAST);
        panel.add(itemPanel);

        panel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        quantityField.setDocument(new IntegerDocument());
        panel.add(quantityField);

        panel.add(new JLabel("Sales Person:"));
        salesPersonCombo = new JComboBox<>();
        salesPersonCombo.setModel(new DefaultComboBoxModel<>(salesPersons.toArray(new String[0])));
        panel.add(salesPersonCombo);

        panel.add(new JLabel());
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addSale());
        panel.add(addButton);

        return panel;
    }

    private void selectItem() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Select Item");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Name", "Total Price", "Total Stock"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Aggregate items by name
        Map<String, AggregatedItem> aggregatedItems = new HashMap<>();

        for (String[] item : itemsList) {
            if (item.length >= 6) {
                String itemName = item[1];
                double price = Double.parseDouble(item[4]);
                int stock = Integer.parseInt(item[5]);

                if (aggregatedItems.containsKey(itemName)) {
                    AggregatedItem existing = aggregatedItems.get(itemName);
                    existing.totalStock += stock;
                    existing.items.add(item);
                } else {
                    AggregatedItem newItem = new AggregatedItem();
                    newItem.name = itemName;
                    newItem.price = price;
                    newItem.totalStock = stock;
                    newItem.items.add(item);
                    aggregatedItems.put(itemName, newItem);
                }
            }
        }

        // Add aggregated items to table
        for (AggregatedItem aggItem : aggregatedItems.values()) {
            model.addRow(new Object[]{
                aggItem.name,
                String.format("%.2f", aggItem.price),
                aggItem.totalStock
            });
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for low stock warning
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Check if total stock is below 10
                int totalStock = (Integer) table.getValueAt(row, 2);
                if (totalStock < 10) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.BLACK);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(table.getBackground());
                }

                return c;
            }
        });

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                itemNameField.setText(table.getValueAt(row, 0).toString());
                dialog.dispose();
            }
        });

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(selectButton, BorderLayout.SOUTH);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSalesListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sales List"));

        String[] columns = {"Sales ID", "Item ID", "Item Name", "Date", "Qty Sold", "Remaining", "Sales Person", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(30);
        salesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        salesTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadItems() {
        itemsList.clear();
        File file = new File(ITEMS_FILE);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Items file not found: " + ITEMS_FILE, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6) {
                    itemsList.add(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalesPersons() {
        salesPersons.clear();
        File file = new File(USERS_FILE);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Users file not found: " + USERS_FILE, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 4 && ("sm".equalsIgnoreCase(data[3]) || "am".equalsIgnoreCase(data[3]))) {
                    salesPersons.add(data[1]);
                }
            }
            salesPersonCombo.setModel(new DefaultComboBoxModel<>(salesPersons.toArray(new String[0])));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSales() {
        salesList.clear();
        tableModel.setRowCount(0);

        File file = new File(SALES_FILE);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                return;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error creating sales file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6) {
                    LocalDate date = LocalDate.parse(data[2], DISPLAY_FORMAT);
                    Sales sale = new Sales(
                            data[0],
                            data[1],
                            date,
                            Integer.parseInt(data[3]),
                            Integer.parseInt(data[4]),
                            data[5]
                    );
                    salesList.add(sale);

                    String itemName = "Unknown";
                    for (String[] item : itemsList) {
                        if (item[0].equals(data[1])) {
                            itemName = item[1];
                            break;
                        }
                    }

                    tableModel.addRow(new Object[]{
                        sale.getSalesId(),
                        sale.getItemId(),
                        itemName,
                        date.format(DISPLAY_FORMAT),
                        sale.getQuantitySold(),
                        sale.getRemainingStock(),
                        sale.getSalesPerson(),
                        "View/Delete"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading sales: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSales() {
        File file = new File(SALES_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Sales sale : salesList) {
                String formattedSale = sale.getSalesId() + "|" +
                                     sale.getItemId() + "|" +
                                     sale.getSalesDate().format(DISPLAY_FORMAT) + "|" +
                                     sale.getQuantitySold() + "|" +
                                     sale.getRemainingStock() + "|" +
                                     sale.getSalesPerson();
                writer.write(formattedSale + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving sales: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSale() {
        try {
            if (itemNameField.getText().isEmpty()) {
                throw new Exception("Please select an item");
            }

            if (quantityField.getText().isEmpty()) {
                throw new Exception("Please enter quantity");
            }

            int quantitySold = Integer.parseInt(quantityField.getText());
            if (quantitySold <= 0) {
                throw new Exception("Quantity must be positive");
            }

            String salesPerson = (String) salesPersonCombo.getSelectedItem();
            if (salesPerson == null || salesPerson.isEmpty()) {
                throw new Exception("Please select a sales person");
            }

            String selectedItemName = itemNameField.getText();

            // Get all items with the same name and calculate total stock
            List<String[]> matchingItems = new ArrayList<>();
            int totalStock = 0;

            for (String[] item : itemsList) {
                if (item[1].equals(selectedItemName)) {
                    matchingItems.add(item);
                    totalStock += Integer.parseInt(item[5]);
                }
            }

            if (matchingItems.isEmpty()) {
                throw new Exception("Selected item not found in inventory");
            }

            if (quantitySold > totalStock) {
                throw new Exception("Not enough stock available. Total available: " + totalStock);
            }

            // Sort items by stock (highest first) to deduct from highest stock first
            matchingItems.sort((a, b) -> Integer.compare(Integer.parseInt(b[5]), Integer.parseInt(a[5])));

            // Deduct stock from items (starting with highest stock)
            int remainingToDeduct = quantitySold;
            String primaryItemId = matchingItems.get(0)[0]; // Use the first item's ID for sales record

            for (String[] item : matchingItems) {
                if (remainingToDeduct <= 0) break;

                int currentStock = Integer.parseInt(item[5]);
                int deductFromThis = Math.min(remainingToDeduct, currentStock);

                // Update this item's stock
                updateSingleItemStock(item[0], deductFromThis);

                remainingToDeduct -= deductFromThis;
            }

            int finalRemainingStock = totalStock - quantitySold;

            Sales newSale = new Sales(
                    String.valueOf(nextSalesId++),
                    primaryItemId,
                    LocalDate.now(),
                    quantitySold,
                    finalRemainingStock,
                    salesPerson
            );

            salesList.add(newSale);
            tableModel.addRow(new Object[]{
                newSale.getSalesId(),
                newSale.getItemId(),
                selectedItemName,
                newSale.getSalesDate().format(DISPLAY_FORMAT),
                newSale.getQuantitySold(),
                newSale.getRemainingStock(),
                newSale.getSalesPerson(),
                "View/Delete"
            });

            saveSales();
            loadItems();
            clearInputFields();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSingleItemStock(String itemId, int quantityToDeduct) {
        try {
            List<String> updatedItems = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(ITEMS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split("\\|");
                    if (data.length >= 6 && data[0].equals(itemId)) {
                        int currentStock = Integer.parseInt(data[5]);
                        data[5] = String.valueOf(currentStock - quantityToDeduct);
                    }
                    updatedItems.add(String.join("|", data));
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ITEMS_FILE))) {
                for (String item : updatedItems) {
                    writer.write(item + "\n");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating stock: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItemStock(String itemId, int quantitySold) {
        try {
            List<String> updatedItems = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(ITEMS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split("\\|");
                    if (data.length >= 6 && data[0].equals(itemId)) {
                        int currentStock = Integer.parseInt(data[5]);
                        data[5] = String.valueOf(currentStock - quantitySold);
                    }
                    updatedItems.add(String.join("|", data));
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ITEMS_FILE))) {
                for (String item : updatedItems) {
                    writer.write(item + "\n");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating stock: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputFields() {
        itemNameField.setText("");
        quantityField.setText("");
    }

    private void viewSale(int row) {
        Sales sale = salesList.get(row);
        JOptionPane.showMessageDialog(this,
                "Sales ID: " + sale.getSalesId() + "\n"
                + "Item ID: " + sale.getItemId() + "\n"
                + "Date: " + sale.getSalesDate().format(DISPLAY_FORMAT) + "\n"
                + "Quantity Sold: " + sale.getQuantitySold() + "\n"
                + "Remaining Stock: " + sale.getRemainingStock() + "\n"
                + "Sales Person: " + sale.getSalesPerson(),
                "Sale Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSale(int row) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this sale?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Sales sale = salesList.get(row);
            updateItemStock(sale.getItemId(), -sale.getQuantitySold());
            salesList.remove(row);
            tableModel.removeRow(row);
            saveSales();
            loadItems();
        }
    }

    // Helper class for aggregating items
    private static class AggregatedItem {
        String name;
        double price;
        int totalStock;
        List<String[]> items = new ArrayList<>();
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            button.setText("View/Delete");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            int option = JOptionPane.showOptionDialog(sales_e.this,
                    "Choose action", "Sales Action",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[]{"View", "Delete", "Cancel"}, "Cancel");

            if (option == 0) {
                viewSale(salesTable.getEditingRow());
            } else if (option == 1) {
                deleteSale(salesTable.getEditingRow());
            }

            return "View/Delete";
        }
    }

    private static class IntegerDocument extends PlainDocument {
        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) {
                return;
            }

            for (int i = 0; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i))) {
                    return;
                }
            }
            super.insertString(offset, str, attr);
        }
    }

    // Sales class definition
    class Sales {
        private String salesId;
        private String itemId;
        private LocalDate salesDate;
        private int quantitySold;
        private int remainingStock;
        private String salesPerson;

        public Sales(String salesId, String itemId, LocalDate salesDate,
                     int quantitySold, int remainingStock, String salesPerson) {
            this.salesId = salesId;
            this.itemId = itemId;
            this.salesDate = salesDate;
            this.quantitySold = quantitySold;
            this.remainingStock = remainingStock;
            this.salesPerson = salesPerson;
        }

        public String getSalesId() { return salesId; }
        public String getItemId() { return itemId; }
        public LocalDate getSalesDate() { return salesDate; }
        public int getQuantitySold() { return quantitySold; }
        public int getRemainingStock() { return remainingStock; }
        public String getSalesPerson() { return salesPerson; }

        public void setSalesId(String salesId) { this.salesId = salesId; }
        public void setItemId(String itemId) { this.itemId = itemId; }
        public void setSalesDate(LocalDate salesDate) { this.salesDate = salesDate; }
        public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
        public void setRemainingStock(int remainingStock) { this.remainingStock = remainingStock; }
        public void setSalesPerson(String salesPerson) { this.salesPerson = salesPerson; }
    }
}