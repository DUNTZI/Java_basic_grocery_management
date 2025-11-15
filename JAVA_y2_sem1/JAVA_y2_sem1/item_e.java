import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class item_e extends JPanel {

    private JTextField nameField, supplierIdField, supplierNameField, categoryField, priceField, stockQuantityField;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private static final String FILE_NAME = "TXT/items.txt";
    private static final String SUPPLIER_FILE = "TXT/suppliers.txt";
    private List<Item> items = new ArrayList<>();
    private int nextId = 2000;

    public item_e() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1000, 500));

        // Create the item list panel FIRST
        JPanel itemListPanel = createItemListPanel();

        // Load existing items to determine next ID
        loadItems();
        findNextId();

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Item Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Left - Item info panel
        JPanel itemInfoPanel = createItemInfoPanel();
        centerPanel.add(itemInfoPanel);

        // Right - Item list panel (already created)
        centerPanel.add(itemListPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void findNextId() {
        nextId = 2001;
        for (Item item : items) {
            try {
                int currentId = Integer.parseInt(item.getId());
                if (currentId >= nextId) {
                    nextId = currentId + 1;
                }
            } catch (NumberFormatException e) {
                // Ignore non-numeric IDs
            }
        }
    }

    private JPanel createItemInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Item Info"));
        panel.setLayout(new GridLayout(7, 2, 5, 5));

        // Supplier ID
        panel.add(new JLabel("Supplier ID:"));
        supplierIdField = new JTextField();
        JButton selectSupplierButton = new JButton("Select");
        selectSupplierButton.addActionListener(e -> selectSupplier());

        JPanel supplierIdPanel = new JPanel(new BorderLayout());
        supplierIdPanel.add(supplierIdField, BorderLayout.CENTER);
        supplierIdPanel.add(selectSupplierButton, BorderLayout.EAST);
        panel.add(supplierIdPanel);

        // Supplier Name (read-only)
        panel.add(new JLabel("Supplier Name:"));
        supplierNameField = new JTextField();
        supplierNameField.setEditable(false);
        panel.add(supplierNameField);

        // Item Name
        panel.add(new JLabel("Item Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        // Category
        panel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        panel.add(categoryField);

        // Price
        panel.add(new JLabel("Price:"));
        priceField = new JTextField();
        panel.add(priceField);

        // Stock Quantity
        panel.add(new JLabel("Stock Quantity:"));
        stockQuantityField = new JTextField();
        panel.add(stockQuantityField);

        // Add button
        panel.add(new JLabel());
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addItem());
        panel.add(addButton);

        return panel;
    }

    private void selectSupplier() {
        File file = new File(SUPPLIER_FILE);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Supplier file not found at: " + file.getAbsolutePath(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(SUPPLIER_FILE))) {
            List<String[]> suppliers = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2) {
                    // Store only ID and Name for selection
                    suppliers.add(new String[]{data[0].trim(), data[1].trim()});
                }
            }

            if (suppliers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No suppliers found in file", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Supplier", true);
            dialog.setLayout(new BorderLayout());

            String[] columnNames = {"ID", "Name"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            for (String[] supplier : suppliers) {
                model.addRow(supplier);
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JButton selectButton = new JButton("Select");
            selectButton.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    supplierIdField.setText((String) model.getValueAt(row, 0));
                    supplierNameField.setText((String) model.getValueAt(row, 1));
                    dialog.dispose();
                }
            });

            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
            dialog.add(selectButton, BorderLayout.SOUTH);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error reading suppliers: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createItemListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Item List"));

        // Create table model
        String[] columnNames = {"ID", "Name", "Supplier ID", "Supplier Name", "Category", "Price", "Stock", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only actions column is editable
            }
        };

        // Create table
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(30);

        // Add action buttons
        TableButtonRenderer buttonRenderer = new TableButtonRenderer();
        TableButtonEditor buttonEditor = new TableButtonEditor(new JCheckBox());
        itemTable.getColumn("Actions").setCellRenderer(buttonRenderer);
        itemTable.getColumn("Actions").setCellEditor(buttonEditor);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(itemTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private String getSupplierName(String supplierId) {
        File file = new File(SUPPLIER_FILE);
        if (!file.exists()) {
            return "Unknown Supplier";
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(SUPPLIER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 2 && data[0].trim().equals(supplierId)) {
                    return data[1].trim(); // Returns the supplier name (second field)
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Supplier";
    }

    private void loadItems() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error creating item file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6) {
                    Item item = new Item(
                            data[0].trim(),
                            data[1].trim(),
                            data[2].trim(),
                            data[3].trim(),
                            Double.parseDouble(data[4].trim()),
                            Integer.parseInt(data[5].trim())
                    );
                    items.add(item);
                    String supplierName = getSupplierName(item.getSupplierId());
                    tableModel.addRow(new Object[]{
                        item.getId(),
                        item.getName(),
                        item.getSupplierId(),
                        supplierName,
                        item.getCategory(),
                        String.format("%.2f", item.getPrice()),
                        item.getStockQuantity(),
                        "View/Edit/Delete"
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error parsing data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveItems() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Item item : items) {
                writer.write(item.toString() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addItem() {
        String name = nameField.getText();
        String supplierId = supplierIdField.getText();
        String supplierName = supplierNameField.getText();
        String category = categoryField.getText();
        String priceStr = priceField.getText();
        String stockStr = stockQuantityField.getText();

        if (name.isEmpty() || supplierId.isEmpty() || category.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            String id = String.valueOf(nextId++);
            Item newItem = new Item(id, name, supplierId, category, price, stock);
            items.add(newItem);
            tableModel.addRow(new Object[]{
                id,
                name,
                supplierId,
                supplierName,
                category,
                String.format("%.2f", price),
                stock,
                "View/Edit/Delete"
            });
            saveItems();
            clearInputFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be numbers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editItem(int row) {
        Item item = items.get(row);

        // Create edit dialog
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Item", true);
        editDialog.setSize(400, 400);
        editDialog.setLayout(new GridLayout(8, 2, 5, 5));

        // Add fields
        editDialog.add(new JLabel("Item ID:"));
        JTextField editIdField = new JTextField(item.getId());
        editIdField.setEditable(false);
        editDialog.add(editIdField);

        editDialog.add(new JLabel("Supplier ID:"));
        JTextField editSupplierIdField = new JTextField(item.getSupplierId());
        JButton editSelectSupplierButton = new JButton("Select");
        editSelectSupplierButton.addActionListener(e -> {
            selectSupplier();
            // Copy selected supplier to edit dialog
            editSupplierIdField.setText(supplierIdField.getText());
            supplierNameField.setText(supplierNameField.getText());
        });

        JPanel editSupplierIdPanel = new JPanel(new BorderLayout());
        editSupplierIdPanel.add(editSupplierIdField, BorderLayout.CENTER);
        editSupplierIdPanel.add(editSelectSupplierButton, BorderLayout.EAST);
        editDialog.add(editSupplierIdPanel);

        editDialog.add(new JLabel("Supplier Name:"));
        JTextField editSupplierNameField = new JTextField(getSupplierName(item.getSupplierId()));
        editSupplierNameField.setEditable(false);
        editDialog.add(editSupplierNameField);

        editDialog.add(new JLabel("Item Name:"));
        JTextField editNameField = new JTextField(item.getName());
        editDialog.add(editNameField);

        editDialog.add(new JLabel("Category:"));
        JTextField editCategoryField = new JTextField(item.getCategory());
        editDialog.add(editCategoryField);

        editDialog.add(new JLabel("Price:"));
        JTextField editPriceField = new JTextField(String.valueOf(item.getPrice()));
        editDialog.add(editPriceField);

        editDialog.add(new JLabel("Stock Quantity:"));
        JTextField editStockField = new JTextField(String.valueOf(item.getStockQuantity()));
        editDialog.add(editStockField);

        // Add buttons
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                item.setName(editNameField.getText());
                item.setSupplierId(editSupplierIdField.getText());
                item.setCategory(editCategoryField.getText());
                item.setPrice(Double.parseDouble(editPriceField.getText()));
                item.setStockQuantity(Integer.parseInt(editStockField.getText()));

                // Update table
                String supplierName = getSupplierName(item.getSupplierId());
                tableModel.setValueAt(item.getName(), row, 1);
                tableModel.setValueAt(item.getSupplierId(), row, 2);
                tableModel.setValueAt(supplierName, row, 3);
                tableModel.setValueAt(item.getCategory(), row, 4);
                tableModel.setValueAt(String.format("%.2f", item.getPrice()), row, 5);
                tableModel.setValueAt(item.getStockQuantity(), row, 6);

                saveItems();
                editDialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editDialog, "Price and Stock must be numbers", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> editDialog.dispose());

        editDialog.add(saveButton);
        editDialog.add(cancelButton);

        editDialog.setVisible(true);
    }

    private void deleteItem(int row) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this item?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            items.remove(row);
            tableModel.removeRow(row);
            saveItems();
        }
    }

    private void viewItem(int row) {
        Item item = items.get(row);
        String supplierName = getSupplierName(item.getSupplierId());
        String message = String.format(
                "ID: %s\nName: %s\nSupplier ID: %s\nSupplier Name: %s\nCategory: %s\nPrice: %.2f\nStock: %d",
                item.getId(), item.getName(), item.getSupplierId(), supplierName,
                item.getCategory(), item.getPrice(), item.getStockQuantity());

        JOptionPane.showMessageDialog(this, message, "Item Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearInputFields() {
        nameField.setText("");
        supplierIdField.setText("");
        supplierNameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        stockQuantityField.setText("");
    }

    // Table button renderer and editor
    class TableButtonRenderer extends JButton implements TableCellRenderer {

        public TableButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class TableButtonEditor extends DefaultCellEditor {

        private JButton button;
        private String label;
        private boolean isPushed;

        public TableButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int option = JOptionPane.showOptionDialog(
                        item_e.this,
                        "Choose action for " + items.get(itemTable.getEditingRow()).getName(),
                        "Item Action",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"View", "Edit", "Delete", "Cancel"},
                        "Cancel");

                if (option == 0) {
                    viewItem(itemTable.getEditingRow());
                }else if (option == 1) {
                    editItem(itemTable.getEditingRow());
                }else if (option == 2) {
                    deleteItem(itemTable.getEditingRow());
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}