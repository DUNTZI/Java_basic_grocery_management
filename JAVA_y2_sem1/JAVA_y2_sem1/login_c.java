
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class login_c {

    private String selectedRole;
    private String enteredUsername;
    private String enteredPassword;

    // Store user info after login
    public static String currentUserId;
    public static String currentUsername;
    public static String currentRole;

    // Updated constructor to accept username
    public login_c(String selectedRole, String enteredUsername, String enteredPassword) {
        this.selectedRole = selectedRole;
        this.enteredUsername = enteredUsername;
        this.enteredPassword = enteredPassword;
    }

    public boolean authenticate() {
        File file = new File("TXT/users.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length == 6) {
                    String userId = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    String role = parts[3];

                    // Check username, password, and role
                    if (username.equalsIgnoreCase(enteredUsername)
                            && password.equals(enteredPassword)
                            && role.equalsIgnoreCase(selectedRole)) {

                        // Save logged-in user info
                        currentUserId = userId;
                        currentUsername = username;
                        currentRole = role;
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading users.txt: " + e.getMessage());
        }

        return false;
    }
}
