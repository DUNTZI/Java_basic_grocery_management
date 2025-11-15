import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class inventory_r extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String SALES_FILE = "TXT/sales_data.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18); // Added for consistency

    // TreeMap to store unique dates with inventory/sales activity, sorted in descending order
    private TreeMap<Date, String> activityDates = new TreeMap<>(new Comparator<Date>() {
        @Override
        public int compare(Date date1, Date date2) {
            // Sort by date in descending order
            return date2.compareTo(date1);
        }
    });

    public inventory_r() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the panel

        JLabel titleLabel = new JLabel("Inventory Reports", SwingConstants.LEFT);
        titleLabel.setFont(TITLE_FONT);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        readInventoryAndSalesDates(); // Populate activityDates

        SimpleDateFormat dateFormatForDisplay = new SimpleDateFormat("dd-MM-yyyy");

        // Iterate through sorted dates and create report buttons
        for (Map.Entry<Date, String> entry : activityDates.entrySet()) {
            JPanel rowPanel = new JPanel(new GridLayout(1, 2));
            rowPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Limit height

            JLabel dateLabel = new JLabel(dateFormatForDisplay.format(entry.getKey()));
            dateLabel.setFont(CONTENT_FONT);
            JButton viewButton = new JButton("View Report");
            viewButton.setFont(CONTENT_FONT);

            rowPanel.add(dateLabel);
            rowPanel.add(viewButton);

            mainPanel.add(rowPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add a small gap between rows

            String selectedDate = dateFormatForDisplay.format(entry.getKey());
            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InventoryReport inventoryReportPanel = new InventoryReport(selectedDate);
                    JDialog dialog = new JDialog();
                    dialog.setTitle("Inventory Report - " + selectedDate);
                    dialog.add(inventoryReportPanel);
                    dialog.setSize(1000, 600); // Increased size for better table display
                    dialog.setLocationRelativeTo(inventory_r.this);
                    dialog.setModal(true); // Make it modal so user has to close it
                    dialog.setVisible(true);
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove scroll pane border
        add(scrollPane, BorderLayout.CENTER);
    }

    private void readInventoryAndSalesDates() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        // Read inventory.txt for dates
        if (new File(INVENTORY_FILE).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    // inventory.txt: inventory_id|item_id|stock_level|last_updated|received_quantity|updated_by|status
                    if (parts.length >= 4) { // We need at least the last_updated date
                        try {
                            Date date = inputDateFormat.parse(parts[3].trim()); // last_updated is at index 3
                            activityDates.put(date, ""); // Value doesn't matter, just need the date
                        } catch (ParseException e) {
                            System.err.println("Error parsing date in " + INVENTORY_FILE + ": '" + parts[3].trim() + "' from line: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading " + INVENTORY_FILE + ": " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println(INVENTORY_FILE + " not found. Skipping inventory data.");
        }

        // Read sales_data.txt for dates
        if (new File(SALES_FILE).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    // sales_data.txt: sales_id|item_id|sales_date|quantity_sold|remaining_stock|sold_by
                    if (parts.length >= 3) { // We need sales_date at index 2
                        try {
                            Date date = inputDateFormat.parse(parts[2].trim());
                            activityDates.put(date, ""); // Value doesn't matter, just need the date
                        } catch (ParseException e) {
                            System.err.println("Error parsing date in " + SALES_FILE + ": '" + parts[2].trim() + "' from line: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading " + SALES_FILE + ": " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println(SALES_FILE + " not found. Skipping sales data.");
        }
    }

    static class InventoryReport extends JPanel {
        private DefaultTableModel tableModel;
        private JTable reportTable;
        private String selectedDate; // The date for which the report is generated

        public InventoryReport(String date) {
            this.selectedDate = date;
            setLayout(new BorderLayout(10, 10)); // Add gaps between components
            setBorder(new EmptyBorder(20, 20, 20, 20)); // Add padding

            JLabel titleLabel = new JLabel("Daily Inventory Report - " + selectedDate, SwingConstants.CENTER);
            titleLabel.setFont(TITLE_FONT);
            add(titleLabel, BorderLayout.NORTH);

            tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make cells non-editable
                }
            };
            reportTable = new JTable(tableModel);

            // Define columns
            tableModel.addColumn("Item ID");
            tableModel.addColumn("Item Name");
            tableModel.addColumn("Stock In");
            tableModel.addColumn("Stock Out");
            tableModel.addColumn("Remaining Stock"); // Column name updated

            // Set preferred column widths
            int[] columnWidths = {100, 200, 100, 100, 200}; // Adjusted widths
            for (int i = 0; i < columnWidths.length; i++) {
                TableColumn column = reportTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(columnWidths[i]);
            }

            reportTable.setFont(CONTENT_FONT);
            reportTable.getTableHeader().setFont(HEADER_FONT);
            reportTable.setRowHeight(30);
            reportTable.setFillsViewportHeight(true); // Fill the entire height of the scroll pane

            JScrollPane scrollPane = new JScrollPane(reportTable);
            add(scrollPane, BorderLayout.CENTER);

            // Process data for the selected date
            processDailyInventory();
        }

        private void processDailyInventory() {
            // SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Not directly used for parsing here, but good to keep if needed

            // Map to store item names and their remaining stock from items.txt
            Map<String, String> itemNames = new HashMap<>();
            Map<String, Integer> itemRemainingStock = new HashMap<>();

            // Read item details from items.txt
            try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    // items.txt: item_id|item_name|description|category|price|stock
                    if (parts.length >= 6) { // Ensure enough parts for item_id and stock
                        String itemId = parts[0].trim();
                        String itemName = parts[1].trim();
                        try {
                            int stock = Integer.parseInt(parts[5].trim()); // Remaining stock from items.txt
                            itemNames.put(itemId, itemName);
                            itemRemainingStock.put(itemId, stock);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing stock quantity in " + ITEMS_FILE + ": '" + parts[5].trim() + "' for item ID: " + itemId);
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading " + ITEMS_FILE + " for item details: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error reading " + ITEMS_FILE + " for item details: " + e.getMessage());
            }

            // Maps to store total stock in and total stock out for the selected date
            Map<String, Integer> totalStockInToday = new HashMap<>();
            Map<String, Integer> totalStockOutToday = new HashMap<>();

            // Read inventory.txt for stock in on selectedDate (only "Verified" status)
            if (new File(INVENTORY_FILE).exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\\|");
                        // inventory.txt: inventory_id|item_id|stock_level|last_updated|received_quantity|updated_by|status
                        if (parts.length >= 7 && parts[3].trim().equals(selectedDate) && parts[6].trim().equalsIgnoreCase("Verified")) {
                            String itemId = parts[1].trim();
                            try {
                                int quantityReceived = Integer.parseInt(parts[4].trim()); // received_quantity at index 4
                                totalStockInToday.put(itemId, totalStockInToday.getOrDefault(itemId, 0) + quantityReceived);
                                // Ensure item name is captured even if not in items.txt (for newly added items)
                                if (!itemNames.containsKey(itemId)) {
                                    itemNames.put(itemId, "Unknown Item (from Inventory)");
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing received_quantity in " + INVENTORY_FILE + ": '" + parts[4].trim() + "' for item ID: " + itemId);
                            }
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error reading " + INVENTORY_FILE + ": " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Error reading " + INVENTORY_FILE + ": " + e.getMessage());
                }
            }

            // Read sales_data.txt for stock out on selectedDate (summing quantity_sold)
            if (new File(SALES_FILE).exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\\|");
                        // sales_data.txt: sales_id|item_id|sales_date|quantity_sold|remaining_stock|sold_by
                        if (parts.length >= 4 && parts[2].trim().equals(selectedDate)) {
                            String itemId = parts[1].trim();
                            try {
                                int quantitySold = Integer.parseInt(parts[3].trim()); // quantity_sold at index 3
                                totalStockOutToday.put(itemId, totalStockOutToday.getOrDefault(itemId, 0) + quantitySold);
                                // Ensure item name is captured even if not in items.txt
                                if (!itemNames.containsKey(itemId)) {
                                    itemNames.put(itemId, "Unknown Item (from Sales)");
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing quantity_sold in " + SALES_FILE + ": '" + parts[3].trim() + "' for item ID: " + itemId);
                            }
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error reading " + SALES_FILE + ": " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Error reading " + SALES_FILE + ": " + e.getMessage());
                }
            }

            // Collect all unique item IDs that had any activity today (either stock in or stock out)
            Set<String> allItemsForReport = new HashSet<>();
            allItemsForReport.addAll(totalStockInToday.keySet());
            allItemsForReport.addAll(totalStockOutToday.keySet());

            // Sort item IDs for consistent report order
            List<String> sortedItemIds = new ArrayList<>(allItemsForReport);
            Collections.sort(sortedItemIds);

            // Populate the table
            for (String itemId : sortedItemIds) {
                int stockIn = totalStockInToday.getOrDefault(itemId, 0);
                int stockOut = totalStockOutToday.getOrDefault(itemId, 0);

                // Only add the row if either Stock In or Stock Out is not zero
                if (stockIn > 0 || stockOut > 0) {
                    int remainingStockFromItemsTxt = itemRemainingStock.getOrDefault(itemId, 0);

                    tableModel.addRow(new Object[]{
                            itemId,
                            itemNames.getOrDefault(itemId, "Unknown Item"), // Use stored name or "Unknown"
                            stockIn,
                            stockOut,
                            remainingStockFromItemsTxt
                    });
                }
            }
        }
    }

    public static void main(String[] args) {
        // For testing purposes
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Reports");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new inventory_r());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}