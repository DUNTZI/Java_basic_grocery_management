import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class inventory_c extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";

    private JComboBox<String> itemIdInfoComboBox;
    private JTextField lastUpdatedInfoField;
    private JTextField rQuantityInfoField;
    private JTextField stockLevelInfoField;
    private JButton addButton;

    private List<inventory_e.InventoryRecord> inventoryRecords;
    private Map<String, inventory_e.ItemDetails> itemDetailsMap;
    private inventory_e parentPanel;

    public inventory_c(List<inventory_e.InventoryRecord> inventoryRecords, Map<String, inventory_e.ItemDetails> itemDetailsMap, inventory_e parentPanel) {
        this.inventoryRecords = inventoryRecords;
        this.itemDetailsMap = itemDetailsMap;
        this.parentPanel = parentPanel;
        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Add New Inventory");
        LineBorder topBorder = new LineBorder(Color.BLACK, 2);

        setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                new CompoundBorder(topBorder, new EmptyBorder(10, 10, 10, 10))
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel itemIdLabel = new JLabel("Item ID:");
        DefaultComboBoxModel<String> itemIdComboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, inventory_e.ItemDetails> entry : itemDetailsMap.entrySet()) {
            inventory_e.ItemDetails details = entry.getValue();
            itemIdComboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        itemIdInfoComboBox = new JComboBox<>(itemIdComboBoxModel);
        itemIdInfoComboBox.setMaximumRowCount(15);

        JLabel lastUpdatedLabel = new JLabel("Last Updated:");
        lastUpdatedInfoField = new JTextField(15);
        lastUpdatedInfoField.setText("DD-MM-YYYY");

        JLabel rQuantityLabel = new JLabel("Received Qty:");
        rQuantityInfoField = new JTextField(15);
        rQuantityInfoField.setText("Number");

        JLabel stockLevelLabel = new JLabel("Stock Level:");
        stockLevelInfoField = new JTextField(15);
        stockLevelInfoField.setEditable(false);

        addButton = new JButton("Add");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(itemIdLabel, gbc);
        gbc.gridx = 1;
        add(itemIdInfoComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lastUpdatedLabel, gbc);
        gbc.gridx = 1;
        add(lastUpdatedInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(rQuantityLabel, gbc);
        gbc.gridx = 1;
        add(rQuantityInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(stockLevelLabel, gbc);
        gbc.gridx = 1;
        add(stockLevelInfoField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(addButton, gbc);

        itemIdInfoComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) itemIdInfoComboBox.getSelectedItem();
                if (selectedItem != null) {
                    String itemId = selectedItem.split(" ")[1];
                    int stockLevel = getStockLevelFromItems(itemId);
                    stockLevelInfoField.setText(String.valueOf(stockLevel));
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) itemIdInfoComboBox.getSelectedItem();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(inventory_c.this, "Please select an item.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String itemId = selectedItem.split(" ")[1];
                String lastUpdated = lastUpdatedInfoField.getText();
                String rQuantityText = rQuantityInfoField.getText().trim();

                try {
                    int rQuantity = Integer.parseInt(rQuantityText.isEmpty() ? "0" : rQuantityText);
                    String newInventoryId = parentPanel.generateNewInventoryId();

                    int stockLevel = getStockLevelFromItems(itemId);

                    inventoryRecords.add(new inventory_e.InventoryRecord(newInventoryId, itemId, stockLevel, lastUpdated, rQuantity, Integer.parseInt(login_c.currentUserId), "Pending"));
                    parentPanel.populateInventoryListTableTop();
                    parentPanel.saveInventoryData();
                    JOptionPane.showMessageDialog(inventory_c.this, "Inventory added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(inventory_c.this, "Invalid input for Received Quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void clearFields() {
        itemIdInfoComboBox.setSelectedIndex(-1);
        lastUpdatedInfoField.setText("DD-MM-YYYY");
        rQuantityInfoField.setText("Number");
        stockLevelInfoField.setText("");
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
}