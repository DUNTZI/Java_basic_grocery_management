import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.RowFilter;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;

public class pr_e extends JPanel {

    private static final String PR_INFO_CARD = "INFO";
    public static final String PR_LIST_CARD = "LIST";

    // --- Main Layout ---
    private CardLayout cardLayout;
    private JPanel cardPanel; // Panel holding the different views (Info, List)
    private JPanel topButtonPanel; // Panel for PR Info/PR List buttons

    // --- PR Info Components ---
    protected JComboBox<String> itemIDComboBox, supplierIDComboBox;
    protected JTextField quantityField, requiredDateField;
    protected JButton addPrButton;

    // --- PR List Components ---
    protected JTable prTable; // Top table (PR ID, Actions)
    protected JTable prDetailsTable; // Bottom table (Full details)
    protected DefaultTableModel tableModel; // Model for prTable
    protected DefaultTableModel detailsTableModel; // Model for prDetailsTable
    protected JButton updateButton;
    protected JTextField searchField;
    protected JButton searchButton;
    protected TableRowSorter<DefaultTableModel> sorter; // Sorter for prTable
    protected List<String[]> fullPrData; // To store all data read from file
    protected pr_e_c controller;
    public Map<String, String> itemIdToNameMap = new HashMap<>();
    public Map<String, java.util.List<String>> itemNameToSupplierIdsMap = new HashMap<>();
    public Map<String, Map<String, String>> itemNameAndSupplierToIdMap = new HashMap<>();

    public pr_e() {
        setLayout(new BorderLayout(0, 5)); // Main layout with vertical gap
        fullPrData = new ArrayList<>();
        controller = new pr_e_c(this);

        createTopButtons(); // Create the PR Info/PR List buttons
        createCardPanel(); // Create the panel that switches between views
        createPrInfoPanel(); // Create the content for the PR Info view
        createPrListPanel(); // Create the content for the PR List view

        add(topButtonPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        controller.loadPRData(); // Load initial data
        showCard(PR_INFO_CARD); // Show PR Info page initially
    }

    private void createTopButtons() {
        topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        JButton infoButton = new JButton("PR Info");
        JButton listButton = new JButton("PR List");

        infoButton.addActionListener(e -> showCard(PR_INFO_CARD));
        listButton.addActionListener(e -> {
            controller.loadPRData(); // Reload data when switching to list view
            showCard(PR_LIST_CARD);
        });

        topButtonPanel.add(infoButton);
        topButtonPanel.add(listButton);
    }

    private void createCardPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
    }

    protected void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }

    // =================================================
    // --- PR Info Panel Creation ---
    // =================================================
    private void createPrInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Load items and suppliers from items.txt ---
        List<String> itemNames = new ArrayList<>();
        itemIdToNameMap.clear();
        itemNameToSupplierIdsMap.clear();
        itemNameAndSupplierToIdMap.clear();

        // For summing stock and collecting suppliers
        Map<String, Integer> itemNameToTotalStock = new HashMap<>();
        Map<String, List<String>> itemNameToSuppliers = new HashMap<>();
        Map<String, String> itemNameToFirstId = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("TXT/items.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String itemId = parts[0];
                    String itemName = parts[1];
                    String supplierId = parts[2];
                    int stockQty = 0;
                    try { stockQty = Integer.parseInt(parts[5]); } catch (NumberFormatException ignored) {}

                    // Sum stock by item name
                    itemNameToTotalStock.put(itemName, itemNameToTotalStock.getOrDefault(itemName, 0) + stockQty);

                    // Collect suppliers for each item name
                    itemNameToSuppliers.computeIfAbsent(itemName, k -> new ArrayList<>()).add(supplierId);

                    // Store the first itemId for each item name (for mapping)
                    if (!itemNameToFirstId.containsKey(itemName)) {
                        itemNameToFirstId.put(itemName, itemId);
                    }

                    // Build (itemName, supplierId) -> itemId map
                    itemNameAndSupplierToIdMap
                        .computeIfAbsent(itemName, k -> new HashMap<>())
                        .put(supplierId, itemId);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading items.txt: " + e.getMessage());
        }

        // Only add item names with total stock < 10
        for (String itemName : itemNameToTotalStock.keySet()) {
            if (itemNameToTotalStock.get(itemName) < 10) {
                itemNames.add(itemName);
                itemIdToNameMap.put(itemName, itemNameToFirstId.get(itemName));
                itemNameToSupplierIdsMap.put(itemName, itemNameToSuppliers.get(itemName));
            }
        }

        // --- ComboBoxes ---
        JLabel itemLabel = new JLabel("Item Name:");
        itemIDComboBox = new JComboBox<>(itemNames.toArray(new String[0]));
        supplierIDComboBox = new JComboBox<>();

        // Populate supplierIDComboBox when item is selected
        itemIDComboBox.addActionListener(e -> {
            Object selected = itemIDComboBox.getSelectedItem();
            supplierIDComboBox.removeAllItems();
            if (selected != null) {
                String itemName = selected.toString();
                java.util.List<String> supplierIds = itemNameToSupplierIdsMap.get(itemName);
                if (supplierIds != null) {
                    for (String sid : supplierIds) {
                        supplierIDComboBox.addItem(sid);
                    }
                }
                supplierIDComboBox.setSelectedIndex(-1); // Let user choose
            }
        });

        // --- Ensure supplierIDComboBox is populated on first load ---
        if (itemIDComboBox.getItemCount() > 0) {
            itemIDComboBox.setSelectedIndex(0); // Select first item
            Object selected = itemIDComboBox.getSelectedItem();
            supplierIDComboBox.removeAllItems();
            if (selected != null) {
                String itemName = selected.toString();
                java.util.List<String> supplierIds = itemNameToSupplierIdsMap.get(itemName);
                if (supplierIds != null) {
                    for (String sid : supplierIds) {
                        supplierIDComboBox.addItem(sid);
                    }
                }
                supplierIDComboBox.setSelectedIndex(-1); // Let user choose
            }
        }

        JLabel supplierLabel = new JLabel("Supplier ID:");
        JLabel quantityLabel = new JLabel("Quantity Request:");
        quantityField = new JTextField(15);
        JLabel dateLabel = new JLabel("Required Date (DD-MM-YYYY):");
        requiredDateField = new JTextField(15);

        // Layout components using GridBagLayout
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; infoPanel.add(itemLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; infoPanel.add(itemIDComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; infoPanel.add(supplierLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; infoPanel.add(supplierIDComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; infoPanel.add(quantityLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; infoPanel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; infoPanel.add(dateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; infoPanel.add(requiredDateField, gbc);

        // Add PR Button
        addPrButton = new JButton("Add Purchase Requisition");
        addPrButton.setFont(new Font("Arial", Font.BOLD, 14));
        addPrButton.addActionListener(e -> controller.addPR());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.insets = new Insets(20, 8, 8, 8);
        infoPanel.add(addPrButton, gbc);

        // Filler to push components up
        gbc.gridy = 5; gbc.weighty = 1.0;
        infoPanel.add(new JLabel(""), gbc);

        cardPanel.add(infoPanel, PR_INFO_CARD);
    }

    // =================================================
    // --- PR List Panel Creation ---
    // =================================================
    private void createPrListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(new JLabel("Search by PR ID:"));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.addActionListener(e -> controller.searchPR());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        listPanel.add(searchPanel, BorderLayout.NORTH);
        // --------------------

        // --- Center Panel holding both tables ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // 2 rows, 1 column, vertical gap

        // --- Top Table (PR List) ---
        String[] columnNames = {"PR ID", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 1; } // Actions editable
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 1 ? JPanel.class : String.class; }
        };
        prTable = new JTable(tableModel);
        prTable.setRowHeight(35);
        prTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        sorter = new TableRowSorter<>(tableModel);
        prTable.setRowSorter(sorter);
        // Set up Actions column
        TableColumn actionsCol = prTable.getColumnModel().getColumn(1);
        actionsCol.setCellRenderer(new ButtonRenderer());
        actionsCol.setCellEditor(new ButtonEditor(controller)); // Pass controller
        actionsCol.setMinWidth(140);
        actionsCol.setPreferredWidth(150);
        prTable.getColumnModel().getColumn(0).setPreferredWidth(100); // PR ID width
        prTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane topScrollPane = new JScrollPane(prTable);
        topScrollPane.setBorder(BorderFactory.createTitledBorder("Purchase Requisitions"));
        centerPanel.add(topScrollPane);
        // -------------------------

        // --- Bottom Table (PR Details) ---
        String[] detailsColumnNames = {"PR ID", "Item Name", "Supplier ID", "Quantity Request", "Required Date", "Raised By", "Status"};
        detailsTableModel = new DefaultTableModel(detailsColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing for Quantity (column 3)
                return column == 3;
            }
        };
        prDetailsTable = new JTable(detailsTableModel);
        prDetailsTable.setRowHeight(30);
        prDetailsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        prDetailsTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // --- Load items and suppliers from items.txt for dropdowns ---
        java.util.List<String> itemIDs = new java.util.ArrayList<>();
        java.util.List<String> supplierIDs = new java.util.ArrayList<>();
        // Map from item ID to supplier ID
        Map<String, String> itemToSupplierMap = new HashMap<>();
        itemIdToNameMap.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("TXT/items.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    itemIDs.add(parts[0]);
                    supplierIDs.add(parts[2]);
                    itemToSupplierMap.put(parts[0], parts[2]);
                    itemIdToNameMap.put(parts[0], parts[1]); // Map item id to item name
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading items.txt: " + e.getMessage());
        }
        // Remove duplicates from supplierIDs
        java.util.Set<String> uniqueSuppliers = new java.util.LinkedHashSet<>(supplierIDs);

        // --- Set ComboBox editor for Item ID column ---
        JComboBox<String> itemComboBox = new JComboBox<>(itemIDs.toArray(new String[0]));
        DefaultCellEditor itemEditor = new DefaultCellEditor(itemComboBox);
        prDetailsTable.getColumnModel().getColumn(1).setCellEditor(itemEditor); // Item Name

        // Add CellEditorListener to update Supplier ID after editing Item ID
        itemEditor.addCellEditorListener(new javax.swing.event.CellEditorListener() {
            @Override
            public void editingStopped(javax.swing.event.ChangeEvent e) {
                int row = prDetailsTable.getSelectedRow();
                if (row != -1) {
                    String itemId = prDetailsTable.getValueAt(row, 1).toString();
                    String supplierId = itemToSupplierMap.get(itemId);
                    if (supplierId != null) {
                        prDetailsTable.setValueAt(supplierId, row, 2);
                    }
                }
            }
            @Override
            public void editingCanceled(javax.swing.event.ChangeEvent e) {}
        });

        // TableModelListener for auto-filling Supplier ID
        prDetailsTable.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == 1 && row != -1) { // Item ID changed
                String itemId = prDetailsTable.getValueAt(row, 1).toString();
                String supplierId = itemToSupplierMap.get(itemId);
                if (supplierId != null) {
                    // Temporarily remove listener to avoid recursion
                    javax.swing.event.TableModelListener[] listeners = ((DefaultTableModel) prDetailsTable.getModel()).getTableModelListeners();
                    for (javax.swing.event.TableModelListener l : listeners)
                        prDetailsTable.getModel().removeTableModelListener(l);
                    prDetailsTable.setValueAt(supplierId, row, 2);
                    // Re-add listeners
                    for (javax.swing.event.TableModelListener l : listeners)
                        prDetailsTable.getModel().addTableModelListener(l);
                }
            }
        });

        JScrollPane detailsScrollPane = new JScrollPane(prDetailsTable);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Selected PR Details (Editable)"));
        centerPanel.add(detailsScrollPane);
        // -----------------------------

        listPanel.add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Button Panel ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateButton = new JButton("Update Selected PR Details");
        updateButton.addActionListener(e -> controller.updatePRData());
        bottomButtonPanel.add(updateButton);
        listPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        // ------------------------

        cardPanel.add(listPanel, PR_LIST_CARD); // Add this panel to the CardLayout
    }

    // =================================================
    // --- Inner Classes for Table Buttons ---
    // =================================================

    // Renders the View/Delete button panel
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        final JButton viewButton = new JButton("View");
        final JButton deleteButton = new JButton("Delete");

        public ButtonRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 5, 2)); // Center buttons
            setOpaque(true);
            viewButton.setMargin(new Insets(2, 5, 2, 5));
            deleteButton.setMargin(new Insets(2, 5, 2, 5));
            add(viewButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Set background based on selection
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

    // Handles clicks on View/Delete buttons in the table cell
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private String currentPrId; // Store PR ID of the row being edited
        private pr_e_c controller;

        public ButtonEditor(pr_e_c controller) {
            super(); // No need for checkbox arg with AbstractCellEditor
            this.controller = controller;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");
            viewButton.setMargin(new Insets(2, 5, 2, 5));
            deleteButton.setMargin(new Insets(2, 5, 2, 5));

            viewButton.addActionListener(e -> {
                if (currentPrId != null) {
                    SwingUtilities.invokeLater(() -> { // Ensure actions run after editing stops
                        controller.viewPR(currentPrId);
                    });
                    fireEditingStopped();
                }
            });

            deleteButton.addActionListener(e -> {
                if (currentPrId != null) {
                    // Store ID before stopping edit, as table might change
                    String idToDelete = currentPrId;
                    SwingUtilities.invokeLater(() -> {
                        controller.deletePR(idToDelete);
                    });
                    fireEditingStopped();
                }
            });

            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Get the PR ID from the first column of the current row
            currentPrId = table.getValueAt(row, 0).toString();
            panel.setBackground(table.getSelectionBackground()); // Match selection color
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            // Value stored in the model doesn't change
            return "View/Delete";
        }

        @Override
        public boolean stopCellEditing() {
            currentPrId = null; // Clear stored ID when editing stops
            return super.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            currentPrId = null; // Clear stored ID when editing is cancelled
            super.cancelCellEditing();
        }
    }

    // =================================================
    // --- Main Method for Testing ---
    // =================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { System.err.println("Couldn't set system L&F."); }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Purchase Requisition Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new pr_e());
            frame.setMinimumSize(new Dimension(800, 700)); // Adjusted height for two tables + update button
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}