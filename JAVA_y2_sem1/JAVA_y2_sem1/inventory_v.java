
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

public class inventory_v extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt"; // Path to the inventory file
    private static final Font GLOBAL_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
    private static final int MARGIN = 20; // Consistent margin

    private static class InventoryItem {
        private int inventoryId;
        private int itemId;
        private int stockLevel;
        private Date lastUpdated;
        private int receivedQuantity;
        private int updatedBy;
        private String status;

        // Constructor
        public InventoryItem(int inventoryId, int itemId, int stockLevel, Date lastUpdated, int receivedQuantity, int updatedBy, String status) {
            this.inventoryId = inventoryId;
            this.itemId = itemId;
            this.stockLevel = stockLevel;
            this.lastUpdated = lastUpdated;
            this.receivedQuantity = receivedQuantity;
            this.updatedBy = updatedBy;
            this.status = status;
        }

        // Getter methods
        public int getInventoryId() { return inventoryId; }
        public int getItemId() { return itemId; }
        public int getStockLevel() { return stockLevel; }
        public Date getLastUpdated() { return lastUpdated; }
        public int getReceivedQuantity() { return receivedQuantity; }
        public int getUpdatedBy() { return updatedBy; }
        public String getStatus() { return status; }
    }

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchTextField;

    public inventory_v() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, MARGIN, 0)); // Apply consistent margin

        // Top Panel for Title and Search Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, MARGIN, 10, MARGIN));

        // Title Label (Top Left)
        JLabel titleLabel = new JLabel("Inventory List");
        titleLabel.setFont(TITLE_FONT);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Search Bar (Top Right, Full Width)
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(GLOBAL_FONT);
        searchTextField = new JTextField(30); // Increased width for better visibility
        searchTextField.setFont(GLOBAL_FONT);
        searchContainer.add(searchLabel);
        searchContainer.add(searchTextField);
        topPanel.add(searchContainer, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Load inventory items from the file
        ArrayList<InventoryItem> inventoryList = loadInventoryItems();

        // Sort the inventory items by Inventory ID in descending order
        Collections.sort(inventoryList, (item1, item2) -> Integer.compare(item2.getInventoryId(), item1.getInventoryId()));

        // Define the column names for the table
        String[] columnNames = {"Inventory ID", "Item ID", "Stock Level", "Last Updated", "Received Qty", "Updated By", "Status"};

        // Prepare data for the table
        Object[][] data = new Object[inventoryList.size()][7];
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (int i = 0; i < inventoryList.size(); i++) {
            InventoryItem inventory = inventoryList.get(i);
            data[i][0] = inventory.getInventoryId();
            data[i][1] = inventory.getItemId();
            data[i][2] = inventory.getStockLevel();
            data[i][3] = (inventory.getLastUpdated() != null) ? dateFormat.format(inventory.getLastUpdated()) : "N/A";
            data[i][4] = inventory.getReceivedQuantity();
            data[i][5] = inventory.getUpdatedBy();
            data[i][6] = inventory.getStatus();
        }

        // Create a table model and prevent editing of the cells
        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // Disable editing of cells
            }
        };

        // Create JTable
        table = new JTable(tableModel);
        table.setFont(GLOBAL_FONT); // Set global font for table content
        table.getTableHeader().setFont(HEADER_FONT); // Set header font
        table.setRowHeight(30); // Increase row height for better readability
        table.setFillsViewportHeight(true);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Set up TableRowSorter for filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Adjust the column widths
        int maxColumnWidth = 250; // Increased max column width
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, i);
                Component comp = table.prepareRenderer(renderer, row, i);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            TableCellRenderer headerRenderer = table.getColumnModel().getColumn(i).getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, table.getColumnName(i), false, false, 0, i);
            width = Math.max(width, headerComp.getPreferredSize().width);
            width = Math.min(width + 20, maxColumnWidth);   // Increased padding
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        // Add the scroll pane to the panel
        add(scrollPane, BorderLayout.CENTER);

        // Add listener for the search bar
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = searchTextField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null); // Show all rows if the search field is empty
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); // Filter rows case-insensitively
                }
            }
        });
    }

    private ArrayList<InventoryItem> loadInventoryItems() {
        ArrayList<InventoryItem> inventoryList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split("\\|");
                if (attributes.length == 7) {
                    try {
                        int inventoryId = Integer.parseInt(attributes[0].trim());
                        int itemId = Integer.parseInt(attributes[1].trim());
                        int stockLevel = Integer.parseInt(attributes[2].trim());
                        String lastUpdatedStr = attributes[3].trim();
                        Date lastUpdated = null;
                        if (!lastUpdatedStr.equals("00-00-0000") && !lastUpdatedStr.equals("11-11-1111")) {
                            try {
                                lastUpdated = dateFormat.parse(lastUpdatedStr);
                            } catch (ParseException e) {
                                System.err.println("Error parsing date: " + lastUpdatedStr + " in line: " + line);
                                lastUpdated = null; // Handle invalid date
                            }
                        }
                        int receivedQuantity = Integer.parseInt(attributes[4].trim());
                        int updatedBy = Integer.parseInt(attributes[5].trim());
                        String status = attributes[6].trim();

                        inventoryList.add(new InventoryItem(inventoryId, itemId, stockLevel, lastUpdated, receivedQuantity, updatedBy, status));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing number in inventory file: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in inventory file: " + line + ". Expected 7 pipe-separated values.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return inventoryList;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new inventory_v());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
