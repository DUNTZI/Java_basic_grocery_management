import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
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

public class inventory_fm extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";

    private JTabbedPane tabbedPane;
    private JPanel inventoryListPanel;

    private DefaultTableModel inventoryTableModelTop = new DefaultTableModel(new Object[]{"Inventory ID - (Status)", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : String.class;
        }
    };
    private JTable inventoryTableTop;
    private int currentlySelectedRow = -1;

    private JTextField inventoryIdUpdateField;
    private JTextField itemIdUpdateField;
    private JTextField stockLevelUpdateField;
    private JTextField lastUpdatedUpdateField;
    private JTextField receivedQuantityUpdateField;
    private JTextField updatedByUpdateField;
    private JComboBox<String> statusComboBox;
    private JButton updateButton;

    private List<InventoryRecord> inventoryRecords = new ArrayList<>();
    private Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    public inventory_fm() {
        loadItemDetailsForDropdown();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        inventoryListPanel = createInventoryListPanel();
        tabbedPane.addTab("Inventory List", inventoryListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadInventoryData();
        populateInventoryTableTop();
    }

    private JPanel createInventoryListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        inventoryTableTop = new JTable(inventoryTableModelTop) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                // Highlight the selected row
                if (row == currentlySelectedRow) {
                    c.setBackground(new Color(173, 216, 230)); // Light blue
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(240, 240, 240)); // Light gray
                    }
                }
                
                if (column == 0) {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    ((JComponent)c).setToolTipText(getValueAt(row, column).toString());
                }
                return c;
            }
        };
        
        inventoryTableTop.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        TableColumnModel columnModel = inventoryTableTop.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(300);
        columnModel.getColumn(1).setPreferredWidth(100);
        inventoryTableTop.setFillsViewportHeight(true);
        
        inventoryTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        inventoryTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(inventoryTableTop));
        
        JScrollPane inventoryScrollPaneTop = new JScrollPane(inventoryTableTop);
        inventoryScrollPaneTop.setPreferredSize(new Dimension(inventoryScrollPaneTop.getPreferredSize().width, 200));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inventoryScrollPaneTop, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createInventoryListBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInventoryListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
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
        inventoryIdUpdateField = new JTextField(25);
        inventoryIdUpdateField.setEditable(false);

        JLabel itemIdLabelUpdate = new JLabel("Item ID:");
        itemIdUpdateField = new JTextField(25);
        itemIdUpdateField.setEditable(false);

        JLabel stockLevelLabelUpdate = new JLabel("Stock Level:");
        stockLevelUpdateField = new JTextField(25);
        stockLevelUpdateField.setEditable(false);

        JLabel lastUpdatedLabelUpdate = new JLabel("Last Updated:");
        lastUpdatedUpdateField = new JTextField(25);
        lastUpdatedUpdateField.setEditable(false);

        JLabel receivedQuantityLabelUpdate = new JLabel("Received Quantity:");
        receivedQuantityUpdateField = new JTextField(25);
        receivedQuantityUpdateField.setEditable(false);

        JLabel updatedByLabelUpdate = new JLabel("Updated By:");
        updatedByUpdateField = new JTextField(25);
        updatedByUpdateField.setEditable(false);

        JLabel statusLabel = new JLabel("Status:");
        statusComboBox = new JComboBox<>(new String[]{"Verified", "Not Verified", "Deleted"});
        statusComboBox.setEnabled(true);

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
        bottomPanel.add(itemIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(stockLevelLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(stockLevelUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(lastUpdatedLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(lastUpdatedUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(receivedQuantityLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(receivedQuantityUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(updatedByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(updatedByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(statusComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(updateButton);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(buttonPanel, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inventoryId = inventoryIdUpdateField.getText();
                boolean found = false;
                
                for (InventoryRecord record : inventoryRecords) {
                    if (record.getInventoryId().equals(inventoryId)) {
                        found = true;
                        int confirm = JOptionPane.showConfirmDialog(
                                inventory_fm.this,
                                "Are you sure you want to update Inventory ID: " + record.getInventoryId() + "?",
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                String selectedStatus = (String) statusComboBox.getSelectedItem();
                                String previousStatus = record.getStatus();
                                
                                // If status is changing to Verified from another status
                                if (selectedStatus.equals("Verified") && !previousStatus.equals("Verified")) {
                                    // Update the stock level in items.txt
                                    updateItemStockLevel(record.getItemId(), record.getReceivedQuantity());
                                }
                                
                                // Update the status in inventory record
                                record.setStatus(selectedStatus);
                                
                                populateInventoryTableTop();
                                saveInventoryData();
                                
                                JOptionPane.showMessageDialog(inventory_fm.this, 
                                    "Inventory status updated successfully!" + 
                                    (selectedStatus.equals("Verified") && !previousStatus.equals("Verified") ? 
                                    "\nItem stock level updated in items.txt." : ""), 
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                                
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(inventory_fm.this, 
                                    "Invalid input for numeric fields. Please enter correct data.", 
                                    "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            JOptionPane.showMessageDialog(inventory_fm.this, 
                                "Inventory status update cancelled.", 
                                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                }
                
                clearUpdateFields();
                if (!found) {
                    JOptionPane.showMessageDialog(inventory_fm.this, 
                        "Could not find Inventory record.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return bottomPanel;
    }

    private void updateItemStockLevel(String itemId, int quantityToAdd) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6 && data[0].trim().equals(itemId)) {
                    found = true;
                    try {
                        int currentStock = Integer.parseInt(data[5].trim());
                        int newStock = currentStock + quantityToAdd;
                        line = data[0] + "|" + data[1] + "|" + data[2] + "|" + data[3] + "|" + data[4] + "|" + newStock;
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing stock quantity for item " + itemId);
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading items file: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (found) {
            try (FileWriter writer = new FileWriter(ITEMS_FILE)) {
                for (String line : lines) {
                    writer.write(line + "\n");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving items file: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Item ID " + itemId + " not found in items file", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateInventoryTableTop() {
        inventoryTableModelTop.setRowCount(0);
        for (InventoryRecord record : inventoryRecords) {
            inventoryTableModelTop.addRow(new Object[]{record.getInventoryId() + " - (" + record.getStatus() + ")", new ButtonPanel(record)});
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
                        int stockLevel = Integer.parseInt(data[2].trim());
                        String lastUpdated = data[3].trim();
                        int receivedQuantity = Integer.parseInt(data[4].trim());
                        int updatedBy = Integer.parseInt(data[5].trim());
                        String status = data[6].trim();

                        InventoryRecord record = new InventoryRecord(
                            inventoryId, itemId, stockLevel, 
                            lastUpdated, receivedQuantity, 
                            updatedBy, status
                        );
                        inventoryRecords.add(record);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in line (inventory.txt): " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in inventory.txt: " + line + ". Expected 7 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading Inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    String category = data[3].trim();
                    int stockQuantity = Integer.parseInt(data[5].trim());
                    tempList.add(new ItemDetails(itemId, itemName, category, stockQuantity));
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

    private void saveInventoryData() {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            for (InventoryRecord record : inventoryRecords) {
                writer.write(record.getInventoryId() + "|"
                        + record.getItemId() + "|"
                        + record.getStockLevel() + "|"
                        + record.getLastUpdated() + "|"
                        + record.getReceivedQuantity() + "|"
                        + record.getUpdatedBy() + "|"
                        + record.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving Inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateUpdateFields(InventoryRecord record) {
        // Find the row index of the record
        currentlySelectedRow = -1;
        for (int i = 0; i < inventoryTableModelTop.getRowCount(); i++) {
            String rowInventoryId = ((String)inventoryTableModelTop.getValueAt(i, 0)).split(" - ")[0];
            if (rowInventoryId.equals(record.getInventoryId())) {
                currentlySelectedRow = i;
                break;
            }
        }
        
        // Repaint the table to update the highlighting
        inventoryTableTop.repaint();
        
        // Populate the fields
        inventoryIdUpdateField.setText(record.getInventoryId());
        itemIdUpdateField.setText(record.getItemId());
        stockLevelUpdateField.setText(String.valueOf(record.getStockLevel()));
        lastUpdatedUpdateField.setText(record.getLastUpdated());
        receivedQuantityUpdateField.setText(String.valueOf(record.getReceivedQuantity()));
        updatedByUpdateField.setText(String.valueOf(record.getUpdatedBy()));
        statusComboBox.setSelectedItem(record.getStatus());
    }

    private void clearUpdateFields() {
        currentlySelectedRow = -1;
        inventoryTableTop.repaint();
        
        inventoryIdUpdateField.setText("");
        itemIdUpdateField.setText("");
        stockLevelUpdateField.setText("");
        lastUpdatedUpdateField.setText("");
        receivedQuantityUpdateField.setText("");
        updatedByUpdateField.setText("");
        statusComboBox.setSelectedItem("Verified");
    }

    private static class InventoryRecord {
        private String inventoryId;
        private String itemId;
        private int stockLevel;
        private String lastUpdated;
        private int receivedQuantity;
        private int updatedBy;
        private String status;

        public InventoryRecord(String inventoryId, String itemId, int stockLevel, 
                             String lastUpdated, int receivedQuantity, 
                             int updatedBy, String status) {
            this.inventoryId = inventoryId;
            this.itemId = itemId;
            this.stockLevel = stockLevel;
            this.lastUpdated = lastUpdated;
            this.receivedQuantity = receivedQuantity;
            this.updatedBy = updatedBy;
            this.status = status;
        }

        public String getInventoryId() { return inventoryId; }
        public String getItemId() { return itemId; }
        public int getStockLevel() { return stockLevel; }
        public String getLastUpdated() { return lastUpdated; }
        public int getReceivedQuantity() { return receivedQuantity; }
        public int getUpdatedBy() { return updatedBy; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }

    private static class ItemDetails {
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

        public String getItemId() { return itemId; }
    }

    private class ButtonPanel extends JPanel {
        private JButton viewButton;
        private InventoryRecord record;

        public ButtonPanel(InventoryRecord record) {
            this.record = record;
            setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
            viewButton = new JButton("View");

            Dimension buttonSize = new Dimension(90, 14);
            viewButton.setPreferredSize(buttonSize);

            Font smallerFont = new Font(viewButton.getFont().getName(), Font.PLAIN, 13);
            viewButton.setFont(smallerFont);

            add(viewButton);

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateUpdateFields(record);
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
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return label;
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
            if (value instanceof ButtonPanel) {
                panel = (ButtonPanel) value;
            }
            return panel;
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            inventory_fm panel = new inventory_fm();
            frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
            
            frame.setPreferredSize(new Dimension(1000, 700));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}