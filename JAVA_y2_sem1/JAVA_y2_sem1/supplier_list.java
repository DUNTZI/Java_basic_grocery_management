import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class supplier_list extends JPanel {
    private static final String SUPPLIER_FILE = "TXT/suppliers.txt";
    private static final int MARGIN = 20;
    private static final Font GLOBAL_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);

    private static class Supplier {
        private String id, name, phone, email, address, category;

        public Supplier(String id, String name, String phone, String email, String address, String category) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.category = category;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
        public String getCategory() { return category; }
    }

    public supplier_list() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));

        // Add title label
        JLabel titleLabel = new JLabel("Supplier List", SwingConstants.LEFT);
        titleLabel.setFont(TITLE_FONT);
        add(titleLabel, BorderLayout.NORTH);

        ArrayList<Supplier> suppliers = loadSuppliers();

        String[] columnNames = {"ID", "Supplier Name", "Phone Number", "Email", "Address", "Item Category"};
        Object[][] data = new Object[suppliers.size()][6];
        for (int i = 0; i < suppliers.size(); i++) {
            Supplier supplier = suppliers.get(i);
            data[i][0] = supplier.getId();
            data[i][1] = supplier.getName();
            data[i][2] = supplier.getPhone();
            data[i][3] = supplier.getEmail();
            data[i][4] = supplier.getAddress();
            data[i][5] = supplier.getCategory();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setFont(GLOBAL_FONT);
        table.getTableHeader().setFont(HEADER_FONT);
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);

        // Adjust column widths
        int maxColumnWidth = 300;
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
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                table, table.getColumnName(i), false, false, 0, i);
            width = Math.max(width, headerComp.getPreferredSize().width);
            width = Math.min(width + 10, maxColumnWidth);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    private ArrayList<Supplier> loadSuppliers() {
        ArrayList<Supplier> suppliers = new ArrayList<>();
        File file = new File(SUPPLIER_FILE);

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                "Supplier file not found: " + SUPPLIER_FILE,
                "Error", JOptionPane.ERROR_MESSAGE);
            return suppliers;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(SUPPLIER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split("\\|");
                if (attributes.length >= 6) {
                    String id = attributes[0].trim();
                    String name = attributes[1].trim();
                    String phone = attributes[2].trim();
                    String email = attributes[3].trim();
                    String address = attributes[4].trim();
                    String category = attributes[5].trim();
                    suppliers.add(new Supplier(id, name, phone, email, address, category));
                } else {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error reading suppliers file: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        return suppliers;
    }
}