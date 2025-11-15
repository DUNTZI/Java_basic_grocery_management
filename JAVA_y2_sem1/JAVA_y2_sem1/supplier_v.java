import javax.swing.*;
import java.awt.*;

public class supplier_v extends JDialog {
    private JTextField nameField, contactField, emailField, addressField, itemField;
    private boolean saved = false;

    public supplier_v(JFrame parent, String name, String contact, String email, String address, String item) {
        super(parent, "Edit Supplier", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField(name);
        panel.add(nameField);

        panel.add(new JLabel("Contact No.:"));
        contactField = new JTextField(contact);
        panel.add(contactField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField(email);
        panel.add(emailField);

        panel.add(new JLabel("Address:"));
        addressField = new JTextField(address);
        panel.add(addressField);

        panel.add(new JLabel("Supply Category:"));
        itemField = new JTextField(item);
        panel.add(itemField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            saved = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isSaved() {
        return saved;
    }

    public String[] getEditedData() {
        return new String[]{
            nameField.getText(),
            contactField.getText(),
            emailField.getText(),
            addressField.getText(),
            itemField.getText()
        };
    }
}
