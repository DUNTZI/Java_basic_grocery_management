import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

public class sales_v extends JPanel {
    private static final String SALES_FILE = "TXT/sales_data.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchTextField;
    private static final Font GLOBAL_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
    private static final int MARGIN = 20;

    public sales_v() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 500));

        // Top Panel for Title and Search Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, MARGIN, 10, MARGIN));

        // Title Label (Top Left)
        JLabel title = new JLabel("Sales List");
        title.setFont(TITLE_FONT);
        topPanel.add(title, BorderLayout.WEST);

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

        // Load items for name lookup
        ArrayList<String[]> itemsList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6) {
                    itemsList.add(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading items", "Error", JOptionPane.ERROR_MESSAGE);
        }

        String[] columns = {"Sales ID", "Item ID", "Item Name", "Date", "Qty Sold", "Remaining", "Sales Person"};
        tableModel = new DefaultTableModel(columns, 0);

        try (BufferedReader reader = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6) {
                    // Find item name
                    String itemName = "Unknown";
                    for (String[] item : itemsList) {
                        if (item[0].equals(data[1])) {
                            itemName = item[1];
                            break;
                        }
                    }

                    tableModel.addRow(new Object[]{
                            data[0].trim(),
                            data[1].trim(),
                            itemName,
                            data[2].trim(),
                            data[3].trim(),
                            data[4].trim(),
                            data[5].trim()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sales", "Error", JOptionPane.ERROR_MESSAGE);
        }

        table = new JTable(tableModel);
        table.setFont(GLOBAL_FONT);
        table.getTableHeader().setFont(HEADER_FONT);
        table.setRowHeight(25);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

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
}