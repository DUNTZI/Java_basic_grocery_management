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

public class pr_v extends JPanel {

    private static final String PR_FILE = "TXT/pr.txt"; // Path to the purchase requisition file
    private static final Font GLOBAL_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
    private static final int MARGIN = 20; // Consistent margin

    private static class PurchaseRequisition {
        private int prId;
        private int itemId;
        private int supplierId;
        private int quantityRequested;
        private Date requiredDate;
        private int raisedBy;
        private String status;

        // Constructor
        public PurchaseRequisition(int prId, int itemId, int supplierId, int quantityRequested, Date requiredDate, int raisedBy, String status) {
            this.prId = prId;
            this.itemId = itemId;
            this.supplierId = supplierId;
            this.quantityRequested = quantityRequested;
            this.requiredDate = requiredDate;
            this.raisedBy = raisedBy;
            this.status = status;
        }

        // Getter methods
        public int getPrId() { return prId; }
        public int getItemId() { return itemId; }
        public int getSupplierId() { return supplierId; }
        public int getQuantityRequested() { return quantityRequested; }
        public Date getRequiredDate() { return requiredDate; }
        public int getRaisedBy() { return raisedBy; }
        public String getStatus() { return status; }
    }

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchTextField;

    public pr_v() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, MARGIN, 0)); // Apply consistent margin

        // Top Panel for Title and Search Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, MARGIN, 10, MARGIN));

        // Title Label (Top Left)
        JLabel titleLabel = new JLabel("Purchase Requisition List");
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

        // Load purchase requisitions from the file
        ArrayList<PurchaseRequisition> prList = loadPurchaseRequisitions();

        // Sort the purchase requisitions by PR ID in descending order
        Collections.sort(prList, (pr1, pr2) -> Integer.compare(pr2.getPrId(), pr1.getPrId()));

        // Define the column names for the table
        String[] columnNames = {"PR ID", "Item ID", "Supplier ID", "Quantity Requested", "Required Date", "Raised By", "Status"};

        // Prepare data for the table
        Object[][] data = new Object[prList.size()][7];
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (int i = 0; i < prList.size(); i++) {
            PurchaseRequisition pr = prList.get(i);
            data[i][0] = pr.getPrId();
            data[i][1] = pr.getItemId();
            data[i][2] = pr.getSupplierId();
            data[i][3] = pr.getQuantityRequested();
            data[i][4] = (pr.getRequiredDate() != null) ? dateFormat.format(pr.getRequiredDate()) : "N/A";
            data[i][5] = pr.getRaisedBy();
            data[i][6] = pr.getStatus();
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

    private ArrayList<PurchaseRequisition> loadPurchaseRequisitions() {
        ArrayList<PurchaseRequisition> prList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try (BufferedReader br = new BufferedReader(new FileReader(PR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split("\\|");
                if (attributes.length == 7) {
                    try {
                        int prId = Integer.parseInt(attributes[0].trim());
                        int itemId = Integer.parseInt(attributes[1].trim());
                        int supplierId = Integer.parseInt(attributes[2].trim());
                        int quantityRequested = Integer.parseInt(attributes[3].trim());
                        String requiredDateStr = attributes[4].trim();
                        Date requiredDate = null;
                        if (!requiredDateStr.equals("00-00-0000") && !requiredDateStr.equals("11-11-1111")) {
                            try {
                                requiredDate = dateFormat.parse(requiredDateStr);
                            } catch (ParseException e) {
                                System.err.println("Error parsing date: " + requiredDateStr + " in line: " + line);
                                requiredDate = null; // Handle invalid date
                            }
                        }
                        int raisedBy = Integer.parseInt(attributes[5].trim());
                        String status = attributes[6].trim();

                        prList.add(new PurchaseRequisition(prId, itemId, supplierId, quantityRequested, requiredDate, raisedBy, status));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing number in purchase requisition file: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in purchase requisition file: " + line + ". Expected 7 pipe-separated values.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading purchase requisition file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return prList;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Purchase Requisition List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new pr_v());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}