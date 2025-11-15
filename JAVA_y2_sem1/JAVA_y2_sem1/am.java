
import java.awt.*;
import javax.swing.*;

public class am extends JPanel {

    private JPanel contentPanel;
    private JFrame mainFrame;

    public am(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // ===== TOP PANEL (Always Visible Username) =====
        String username = login_c.currentUsername; // This should be the logged-in user's username
        String role = login_c.currentRole;
        String userId = login_c.currentUserId;
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel userLabel = new JLabel("Role:" + role + "   " + "Username: " + username + "   " + "User Id: " + userId);
        userLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(userLabel);

        // ===== CONTENT PANEL (Switches Content in Center) =====
        contentPanel = new JPanel(new BorderLayout());
        JLabel defaultLabel = new JLabel("WELCOME ADMINISTRATOR", SwingConstants.CENTER);
        defaultLabel.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(defaultLabel, BorderLayout.CENTER);

        // ===== BOTTOM BUTTON PANEL =====
        JPanel bottomPanel = new JPanel(new GridLayout(1, 7)); // 6 buttons + 1 Exit button
        String[] buttonNames = {"User", "Supplier", "Item", "Sales", "PR", "PO", "Finance"};
        String[] classNames = {"user_am", "supplier_e", "item_e", "sales_e", "pr_e", "po_e", "finance_am"};

        for (int i = 0; i < buttonNames.length; i++) {
            JButton button = new JButton(buttonNames[i]);
            final String className = classNames[i];
            button.addActionListener(e -> switchContent(className));
            bottomPanel.add(button);
        }

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitToLogin());
        bottomPanel.add(exitButton);

        // ===== ADD TO MAIN PANEL =====
        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void switchContent(String className) {
        try {
            contentPanel.removeAll();
            Class<?> clazz = Class.forName(className);
            JPanel panel = (JPanel) clazz.getDeclaredConstructor().newInstance();
            contentPanel.add(panel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error opening " + className, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitToLogin() {
        mainFrame.getContentPane().removeAll();
        mainFrame.add(new login((frame) mainFrame));
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}
