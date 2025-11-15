import java.util.List;
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
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class finance_r extends JPanel {

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 18);
    private TreeMap<Date, String> monthYearData = new TreeMap<>(new Comparator<Date>() {
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyyMM");

        @Override
        public int compare(Date date1, Date date2) {
            return monthYearFormat.format(date2).compareTo(monthYearFormat.format(date1));
        }
    });

    public finance_r() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Financial Reports", SwingConstants.LEFT);
        titleLabel.setFont(TITLE_FONT);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        readFinanceAndSalesData();

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM-yyyy");
        Map<String, String> combinedData = new LinkedHashMap<>();

        for (Map.Entry<Date, String> entry : monthYearData.entrySet()) {
            String monthYear = monthYearFormat.format(entry.getKey());
            combinedData.put(monthYear, combinedData.getOrDefault(monthYear, "") + entry.getValue());
        }

        for (Map.Entry<String, String> entry : combinedData.entrySet()) {
            JPanel rowPanel = new JPanel(new GridLayout(1, 2));
            rowPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

            JLabel monthYearLabel = new JLabel(entry.getKey());
            monthYearLabel.setFont(CONTENT_FONT);
            JButton monthYearButton = new JButton("View");
            monthYearButton.setFont(CONTENT_FONT);

            rowPanel.add(monthYearLabel);
            rowPanel.add(monthYearButton);

            mainPanel.add(rowPanel);

            monthYearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    FinanceReport financeReportPanel = new FinanceReport(entry.getKey());
                    JDialog dialog = new JDialog();
                    dialog.add(financeReportPanel);
                    dialog.setSize(1600, 1000);
                    dialog.setLocationRelativeTo(finance_r.this);
                    dialog.setVisible(true);
                }
            });
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void readFinanceAndSalesData() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String financeFilePath = "TXT/finance.txt";
        String salesFilePath = "TXT/sales_data.txt";

        if (new File(financeFilePath).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(financeFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5 && parts[3].equals("Paid")) {
                        try {
                            Date date = inputDateFormat.parse(parts[4]);
                            monthYearData.put(date, monthYearData.getOrDefault(date, "") + "Finance: " + line + "\n");
                        } catch (ParseException e) {
                            System.err.println("Error parsing date in finance.txt: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading finance.txt: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "finance.txt not found.");
        }

        if (new File(salesFilePath).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(salesFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        try {
                            Date date = inputDateFormat.parse(parts[2]);
                            monthYearData.put(date, monthYearData.getOrDefault(date, "") + "Sales: " + line + "\n");
                        } catch (ParseException e) {
                            System.err.println("Error parsing date in sales_data.txt: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading sales_data.txt: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "sales_data.txt not found.");
        }
    }

    static class FinanceReport extends JPanel {

        private DefaultTableModel tableModel;
        private JTable reportTable;
        private double overallTotal = 0.0;
        private String selectedMonthYear;

        public FinanceReport(String monthYear) {
            this.selectedMonthYear = monthYear;
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Financial Report - " + monthYear, SwingConstants.LEFT);
            titleLabel.setFont(TITLE_FONT);
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.add(titleLabel, BorderLayout.NORTH);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(titleLabel, gbc);

            tableModel = new DefaultTableModel();
            reportTable = new JTable(tableModel);
            tableModel.addColumn("Date");
            tableModel.addColumn("Item Purchase (Finance_ID)");
            tableModel.addColumn("Item Sold (Sales_ID)");
            tableModel.addColumn("Total");

            TableColumn column;
            column = reportTable.getColumnModel().getColumn(0);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(1);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(2);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(3);
            column.setPreferredWidth(150);

            reportTable.setFont(CONTENT_FONT);
            reportTable.getTableHeader().setFont(CONTENT_FONT);

            reportTable.setRowHeight(30);

            JScrollPane scrollPane = new JScrollPane(reportTable);
            scrollPane.setPreferredSize(new Dimension(800, 400));

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            add(scrollPane, gbc);

            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel totalLabel = new JLabel("Total: ");
            totalLabel.setFont(CONTENT_FONT);
            JLabel totalValueLabel = new JLabel("0.00");
            totalValueLabel.setFont(CONTENT_FONT);
            totalPanel.add(totalLabel);
            totalPanel.add(totalValueLabel);

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(totalPanel, gbc);

            processFinanceAndSalesData();
            totalValueLabel.setText(String.format("%.2f", overallTotal));
        }

        private void processFinanceAndSalesData() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM-yyyy");
            SimpleDateFormat monthYearCompareFormat = new SimpleDateFormat("yyyyMM");

            Map<String, Double> financeData = new HashMap<>();
            Map<String, String> salesData = new HashMap<>();
            Map<String, Double> itemPrices = readItemPrices();

            try (BufferedReader br = new BufferedReader(new FileReader("TXT/finance.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 6 && parts[3].equals("Paid")) {
                        String financeId = parts[0];
                        String dateStr = parts[4];
                        double amount = Double.parseDouble(parts[5]);

                        try {
                            Date date = dateFormat.parse(dateStr);
                            if (monthYearCompareFormat.format(date).equals(monthYearCompareFormat.format(monthYearFormat.parse(selectedMonthYear)))) {
                                financeData.put(dateStr + "|" + financeId, amount);
                            }
                        } catch (ParseException e) {
                            System.err.println("Error parsing date: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading finance.txt: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format in finance.txt.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred.");
            }

            try (BufferedReader br = new BufferedReader(new FileReader("TXT/sales_data.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String salesId = parts[0];
                        String dateStr = parts[2];
                        try {
                            Date date = dateFormat.parse(dateStr);
                            if (monthYearCompareFormat.format(date).equals(monthYearCompareFormat.format(monthYearFormat.parse(selectedMonthYear)))) {
                                salesData.put(dateStr + "|" + salesId, salesId);
                            }
                        } catch (ParseException e) {
                            System.err.println("Error parsing date: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading sales_data.txt: " + e.getMessage());
            }

            Set<String> allDates = new HashSet<>();
            allDates.addAll(financeData.keySet());
            allDates.addAll(salesData.keySet());
            List<String> sortedDates = new ArrayList<>(allDates);
            sortedDates.sort(Comparator.comparing(s -> {
                try {
                    return dateFormat.parse(s.split("\\|")[0]);
                } catch (ParseException e) {
                    return new Date(0);
                }
            }));

            double totalSales = 0.0;
            double totalFinance = 0.0;

            for (String dateKey : sortedDates) {
                String[] parts = dateKey.split("\\|");
                String dateStr = parts[0];
                String financeId = financeData.containsKey(dateKey) ? parts[1] : "";
                String salesId = salesData.containsKey(dateKey) ? parts[1] : "";
                double amount = financeData.getOrDefault(dateKey, 0.0);

                if (salesId != "") {
                    try (BufferedReader br = new BufferedReader(new FileReader("TXT/sales_data.txt"))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] saleParts = line.split("\\|");
                            if (saleParts[0].equals(salesId) && saleParts.length >= 4 && saleParts[2].equals(dateStr)) {
                                String itemId = saleParts[1];
                                int quantitySold = Integer.parseInt(saleParts[3]);
                                amount = itemPrices.get(itemId) * quantitySold;
                                break;
                            }
                        }
                    } catch (IOException | NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Error processing sales data: " + e.getMessage());
                    }
                    totalSales+=amount;
                }else if(financeId != ""){
                    totalFinance+=amount;
                }

                String formattedAmount;
                if(financeId != ""){
                    formattedAmount = String.format("(%.2f)", amount);
                }else{
                    formattedAmount = String.format("%.2f", amount);
                }

                tableModel.addRow(new Object[]{dateStr, financeId, salesId, formattedAmount});
            }
            overallTotal = totalSales - totalFinance;
        }

        private Map<String, Double> readItemPrices() {
            Map<String, Double> itemPrices = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader("TXT/items.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        itemPrices.put(parts[0], Double.parseDouble(parts[4]));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error reading items.txt: " + e.getMessage());
            }
            return itemPrices;
        }
    }
}