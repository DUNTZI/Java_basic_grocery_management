import java.awt.*;
import javax.swing.*;

public class sm extends JPanel {
    private JPanel contentPanel;
    private JFrame mainFrame;

    public sm(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        String username = login_c.currentUsername;
        String userId = login_c.currentUserId;
        String role = login_c.currentRole;
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel userLabel = new JLabel("Role:" + role + "   " + "Username: " + username + "   " + "User Id: " + userId);
        userLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(userLabel);

        // ===== CONTENT PANEL =====
        contentPanel = new JPanel(new BorderLayout());
        JLabel defaultLabel = new JLabel("WELCOME SALES MANAGER", SwingConstants.CENTER);
        defaultLabel.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(defaultLabel, BorderLayout.CENTER);

        // ===== BOTTOM BUTTON PANEL =====
        JPanel bottomPanel = new JPanel(new GridLayout(1, 7));
        String[] buttonNames = {"Supplier", "Item", "Sales", "PR", "PO List", "Inventory List"};

        // Add buttons with proper action listeners
        JButton supplierButton = new JButton("Supplier");
        supplierButton.addActionListener(e -> switchContent(new supplier_e()));
        bottomPanel.add(supplierButton);

        JButton itemButton = new JButton("Item");
        itemButton.addActionListener(e -> switchContent(new item_e()));
        bottomPanel.add(itemButton);

        JButton salesButton = new JButton("Sales");
        salesButton.addActionListener(e -> switchContent(new sales_e()));
        bottomPanel.add(salesButton);

        JButton prButton = new JButton("PR");
        prButton.addActionListener(e -> switchContent(new pr_e()));
        bottomPanel.add(prButton);

        JButton poButton = new JButton("PO List");
        poButton.addActionListener(e -> switchContent(new po_v()));
        bottomPanel.add(poButton);

        JButton inventoryButton = new JButton("Inventory List");
        inventoryButton.addActionListener(e -> switchContent(new inventory_v()));
        bottomPanel.add(inventoryButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitToLogin());
        bottomPanel.add(exitButton);

        // ===== ADD TO MAIN PANEL =====
        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void switchContent(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void exitToLogin() {
        mainFrame.getContentPane().removeAll();
        mainFrame.add(new login((frame) mainFrame));
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}