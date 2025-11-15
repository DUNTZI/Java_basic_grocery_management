
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class frame extends JFrame {

    private static final int FRAME_BORDER_SIZE = 30;

    public frame() {
        setTitle("JAVA FRAME");
        setSize(1920, 1080); //scale 100%
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create a content pane with BorderLayout
        JPanel contentPane = new JPanel(new BorderLayout());

        // Set the EmptyBorder on the content pane
        contentPane.setBorder(new EmptyBorder(FRAME_BORDER_SIZE, FRAME_BORDER_SIZE, FRAME_BORDER_SIZE, FRAME_BORDER_SIZE));

        // Set the content pane of the frame
        setContentPane(contentPane);

        setVisible(true); // Ensure frame is visible
    }

    // Method to switch panels
    public void switchPanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel, BorderLayout.CENTER); // Use BorderLayout.CENTER to fill the content pane
        revalidate();
        repaint();
    }
}
