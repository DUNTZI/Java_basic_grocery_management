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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class inventory_e extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_DELETED = "Deleted";

    private JTabbedPane tabbedPane;
    private JPanel inventoryInfoPanel;
    private JPanel inventoryListPanel;

    private JComboBox<String> itemIdInfoComboBox;
    private JTextField lastUpdatedInfoField;
    private JTextField rQuantityInfoField;
    private JTextField updateByInfoField;
    private JButton addButton;

    private DefaultTableModel inventoryListTableModelTop = new DefaultTableModel(new Object[]{"Inventory ID - (Status)", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : super.getColumnClass(columnIndex);
        }
    };
    private JTable inventoryListTableTop;
    private JScrollPane inventoryListScrollPaneTop;

    private JTextField inventoryIdUpdateField;
    private JComboBox<String> itemIdUpdateComboBox;
    private JTextField stockLevelUpdateField;
    private JTextField lastUpdatedUpdateField;
    private JTextField rQuantityUpdateField;
    private JTextField updateByUpdateField;
    private JTextField statusUpdateField;
    private JButton updateButton;

    private List<InventoryRecord> inventoryRecords = new ArrayList<>();
    private Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    public inventory_e() {
        this(new ArrayList<>(), new HashMap<>());
    }

    public inventory_e(List<InventoryRecord> inventoryRecords, Map<String, ItemDetails> itemDetailsMap) {
        this.inventoryRecords = inventoryRecords;
        this.itemDetailsMap = itemDetailsMap;
        loadItemDetailsForDropdown();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        inventoryInfoPanel = createInventoryInfoPanel();
        tabbedPane.addTab("Inventory Info", inventoryInfoPanel);

        inventoryListPanel = createInventoryListPanel();
        tabbedPane.addTab("Inventory List", inventoryListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadInventoryData();
        populateInventoryListTableTop();
    }

    private JPanel createInventoryInfoPanel() {
        return new inventory_c(inventoryRecords, itemDetailsMap, this);
    }

    private JPanel createInventoryListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        inventoryListTableTop = new JTable(inventoryListTableModelTop);
        inventoryListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        inventoryListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(inventoryListTableTop));
        inventoryListScrollPaneTop = new JScrollPane(inventoryListTableTop);
        inventoryListScrollPaneTop.setMinimumSize(new Dimension(inventoryListScrollPaneTop.getMinimumSize().width, 50));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inventoryListScrollPaneTop, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createInventoryListBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInventoryListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Inventory Details");
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

        JLabel inventoryIdLabel = new JLabel("Inventory ID:");
        inventoryIdUpdateField = new JTextField(15);
        inventoryIdUpdateField.setEditable(false);

        JLabel itemIdLabelUpdate = new JLabel("Item ID:");
        DefaultComboBoxModel<String> itemIdUpdateComboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            itemIdUpdateComboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        itemIdUpdateComboBox = new JComboBox<>(itemIdUpdateComboBoxModel);
        itemIdUpdateComboBox.setMaximumRowCount(15);

        JLabel stockLevelLabel = new JLabel("Stock Level:");
        stockLevelUpdateField = new JTextField(15);
        stockLevelUpdateField.setEditable(false);

        JLabel lastUpdatedLabelUpdate = new JLabel("Last Updated:");
        lastUpdatedUpdateField = new JTextField(15);
        JLabel rQuantityLabelUpdate = new JLabel("Received Qty:");
        rQuantityUpdateField = new JTextField(15);
        JLabel updateByLabelUpdate = new JLabel("Update By (User ID):");
        updateByUpdateField = new JTextField(15);
        updateByUpdateField.setEditable(false);
        JLabel statusLabel = new JLabel("Status:");
        statusUpdateField = new JTextField(15);
        statusUpdateField.setEditable(false);
        updateButton = new JButton("Update");

        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(inventoryIdLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(inventoryIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(itemIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(itemIdUpdateComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(stockLevelLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(stockLevelUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(lastUpdatedLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(lastUpdatedUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(rQuantityLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(rQuantityUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(updateByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(updateByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(statusUpdateField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(updateButton, gbc);

        itemIdUpdateComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) itemIdUpdateComboBox.getSelectedItem();
                if (selectedItem != null) {
                    String itemId = selectedItem.split(" ")[1];
                    int stockLevel = getStockLevelFromItems(itemId);
                    stockLevelUpdateField.setText(String.valueOf(stockLevel));
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inventoryId = inventoryIdUpdateField.getText();
                boolean found = false;
                for (InventoryRecord record : inventoryRecords) {
                    if (record.getInventoryId().equals(inventoryId) && !record.getStatus().equals(STATUS_DELETED)) {
                        found = true;
                        int confirm = JOptionPane.showConfirmDialog(
                                inventory_e.this,
                                "Are you sure you want to update Inventory ID: " + record.getInventoryId() + "?",
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            String selectedItem = (String) itemIdUpdateComboBox.getSelectedItem();
                            if (selectedItem != null) {
                                String itemId = selectedItem.split(" ")[1];
                                record.setItemId(itemId);
                            }
                            record.setLastUpdated(lastUpdatedUpdateField.getText());
                            String rQuantityText = rQuantityUpdateField.getText().trim();
                            try {
                                int rQuantity = Integer.parseInt(rQuantityText.isEmpty() ? "0" : rQuantityText);
                                record.setReorderQuantity(rQuantity);
                                record.setStockLevel(Integer.parseInt(stockLevelUpdateField.getText().isEmpty() ? "0" : stockLevelUpdateField.getText()));

                                record.setUpdatedBy(0);
                                record.setUpdatedBy(Integer.parseInt(login_c.currentUserId));
                                JOptionPane.showMessageDialog(inventory_e.this, "Inventory updated successfully! Updated By: " + login_c.currentUserId, "Success", JOptionPane.INFORMATION_MESSAGE);
                                clearUpdateFields();
                                populateInventoryListTableTop();
                                saveInventoryData();
                                updateItemIdDropdown();
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(inventory_e.this, "Invalid input for Received Quantity or Stock Level. Please enter correct numeric data.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            JOptionPane.showMessageDialog(inventory_e.this, "Update cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                }
                clearUpdateFields();
                if (!found) {
                    JOptionPane.showMessageDialog(inventory_e.this, "Could not find inventory record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return bottomPanel;
    }

    public void populateInventoryListTableTop() {
        inventoryListTableModelTop.setRowCount(0);
        for (InventoryRecord record : inventoryRecords) {
            inventoryListTableModelTop.addRow(new Object[]{record.getInventoryId() + " - (" + record.getStatus() + ")", new ButtonPanel(record)});
        }
    }

    private void loadInventoryData() {
        inventoryRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 7) {
                    try {
                        String inventoryId = data[0].trim();
                        String itemId = data[1].trim();
                        int currentStock = Integer.parseInt(data[2].trim());
                        String lastUpdated = data[3].trim();
                        int reorderLevel = Integer.parseInt(data[4].trim());
                        int updatedBy = Integer.parseInt(data[5].trim());
                        String status = data[6].trim();

                        InventoryRecord record = new InventoryRecord(inventoryId, itemId, currentStock, lastUpdated, reorderLevel, updatedBy, status);
                        inventoryRecords.add(record);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in line (inventory.txt): " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in inventory.txt: " + line + ". Expected 7 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItemDetailsForDropdown() {
        itemDetailsMap.clear();
        List<ItemDetails> tempList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6) {
                    String itemId = data[0].trim();
                    String itemName = data[1].trim();
                    String supplierId = data[2].trim();
                    String category = data[3].trim();
                    double price = Double.parseDouble(data[4].trim());
                    int stockQuantity = Integer.parseInt(data[5].trim());
                    tempList.add(new ItemDetails(itemId, itemName, category, stockQuantity));
                    if (itemId.equals("2005")) {
                        System.out.println("Item ID 2005 Stock Quantity: " + stockQuantity);
                    }
                } else {
                    System.err.println("Skipping invalid line in items.txt: " + line + ". Expected itemId, ItemName, SupplierId, Category, Price, StockQuantity.");
                }
            }
        } catch (IOException e) {
            System.err.println("IOException caught in loadItemDetailsForDropdown(): " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading items file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Collections.sort(tempList, Comparator.comparing(ItemDetails::getItemId));

        itemDetailsMap.clear();
        for (ItemDetails itemDetails : tempList) {
            itemDetailsMap.put(itemDetails.getItemId(), itemDetails);
        }
    }

    public void saveInventoryData() {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            for (InventoryRecord record : inventoryRecords) {
                writer.write(record.getInventoryId() + "|"
                        + record.getItemId() + "|"
                        + record.getStockLevel() + "|"
                        + record.getLastUpdated() + "|"
                        + record.getReorderQuantity() + "|"
                        + record.getUpdatedBy() + "|"
                        + record.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String generateNewInventoryId() {
        long maxId = 0;
        for (InventoryRecord record : inventoryRecords) {
            try {
                long currentId = Long.parseLong(record.getInventoryId());
                maxId = Math.max(maxId, currentId);
            } catch (NumberFormatException e) {
            }
        }
        return String.valueOf(maxId + 1);
    }

    private void populateUpdateFields(InventoryRecord record) {
        inventoryIdUpdateField.setText(record.getInventoryId());
        for (int i = 0; i < itemIdUpdateComboBox.getItemCount(); i++) {
            String item = itemIdUpdateComboBox.getItemAt(i);
            if (item.startsWith("ID: " + record.getItemId() + " ")) {
                itemIdUpdateComboBox.setSelectedIndex(i);
                break;
            }
        }
        stockLevelUpdateField.setText(String.valueOf(record.getStockLevel()));
        lastUpdatedUpdateField.setText(record.getLastUpdated());
        rQuantityUpdateField.setText(String.valueOf(record.getReorderQuantity()));
        updateByUpdateField.setText(String.valueOf(record.getUpdatedBy()));
        statusUpdateField.setText(record.getStatus());
    }

    private void clearUpdateFields() {
        inventoryIdUpdateField.setText("");
        itemIdUpdateComboBox.setSelectedIndex(-1);
        stockLevelUpdateField.setText("");
        lastUpdatedUpdateField.setText("");
        rQuantityUpdateField.setText("");
        updateByUpdateField.setText("");
        statusUpdateField.setText("");
    }

    private void updateItemIdDropdown() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            comboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        itemIdInfoComboBox.setModel(comboBoxModel);
        itemIdInfoComboBox.setSelectedIndex(-1);

        DefaultComboBoxModel<String> updateComboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            updateComboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        itemIdUpdateComboBox.setModel(updateComboBoxModel);
        itemIdUpdateComboBox.setSelectedIndex(-1);
    }

    public static class InventoryRecord {
        private String inventoryId;
        private String itemId;
        private int stockLevel;
        private String lastUpdated;
        private int reorderQuantity;
        private int updatedBy;
        private String status;

        public InventoryRecord(String inventoryId, String itemId, int stockLevel, String lastUpdated, int reorderQuantity, int updatedBy, String status) {
            this.inventoryId = inventoryId;
            this.itemId = itemId;
            this.stockLevel = stockLevel;
            this.lastUpdated = lastUpdated;
            this.reorderQuantity = reorderQuantity;
            this.updatedBy = updatedBy;
            this.status = status;
        }

        public String getInventoryId() {
            return inventoryId;
        }

        public String getItemId() {
            return itemId;
        }

        public int getStockLevel() {
            return stockLevel;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public int getReorderQuantity() {
            return reorderQuantity;
        }

        public int getUpdatedBy() {
            return updatedBy;
        }

        public String getStatus() {
            return status;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public void setStockLevel(int stockLevel) {
            this.stockLevel = stockLevel;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public void setReorderQuantity(int reorderQuantity) {
            this.reorderQuantity = reorderQuantity;
        }

        public void setUpdatedBy(int updatedBy) {
            this.updatedBy = updatedBy;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ItemDetails {
        private String itemId;
        private String itemName;
        private String category;
        private int stockQuantity;

        public ItemDetails(String itemId, String itemName, String category, int stockQuantity) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.category = category;
            this.stockQuantity = stockQuantity;
        }

        public String getItemId() {
            return itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public String getCategory() {
            return category;
        }

        public int getStockQuantity() {
            return stockQuantity;
        }
    }

    private class ButtonPanel extends JPanel {
        private JButton viewButton;
        private JButton deleteButton;
        private InventoryRecord record;

        public ButtonPanel(InventoryRecord record) {
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
            add(deleteButton);

            viewButton.setEnabled(true);
            deleteButton.setEnabled(record.getStatus().equals(STATUS_PENDING));

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
                    if (record.getStatus().equals(STATUS_PENDING)) {
                        int confirm = JOptionPane.showConfirmDialog(
                                inventory_e.this,
                                "Are you sure you want to delete Inventory ID: " + record.getInventoryId() + "?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            record.setStatus(STATUS_DELETED);
                            populateInventoryListTableTop();
                            saveInventoryData();
                            refreshActionColumn();
                            clearUpdateFields();
                            updateItemIdDropdown();
                            tabbedPane.setSelectedIndex(0);
                            JOptionPane.showMessageDialog(inventory_e.this, "Inventory ID: " + record.getInventoryId() + " deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(inventory_e.this, "Delete cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(inventory_e.this, "Cannot delete records with status '" + record.getStatus() + "'.", "Delete Not Allowed", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }

        public void updateButtonState() {
            deleteButton.setEnabled(record.getStatus().equals(STATUS_PENDING));
        }
    }

    private void refreshActionColumn() {
        int actionsColumnIndex = 1;
        for (int row = 0; row < inventoryListTableTop.getRowCount(); row++) {
            Rectangle cellRect = inventoryListTableTop.getCellRect(row, actionsColumnIndex, true);
            inventoryListTableTop.repaint(cellRect);
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            InventoryRecord record = inventoryRecords.get(row);
            if (record != null) {
                ButtonPanel panel = new ButtonPanel(record);
                panel.updateButtonState();
                return panel;
            } else {
                return new JLabel();
            }
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private ButtonPanel panel;

        public ButtonEditor(JTable table) {
            super(new JTextField());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            InventoryRecord record = inventoryRecords.get(row);
            if (record != null) {
                panel = new ButtonPanel(record);
                panel.updateButtonState();
                return panel;
            } else {
                return new JPanel();
            }
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return false;
        }
    }

    private int getStockLevelFromItems(String itemId) {
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6 && data[0].trim().equals(itemId)) {
                    return Integer.parseInt(data[5].trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading items file or parsing stock level: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new inventory_e());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}