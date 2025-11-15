
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

// Data model representing a user
public class user_c {

    private String id;
    private String username;
    private String password;
    private String role;
    private String contact;
    private String email;

    public user_c(String id, String username, String password, String role, String contact, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.contact = contact;
        this.email = email;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Convert to a formatted string for file storage
    public String toFileString() {
        return id + "|" + username + "|" + password + "|" + role + "|" + contact + "|" + email;
    }
}

// Controller class that handles user CRUD operations
class UserController {

    private static final String USER_FILE = "TXT/users.txt";

    // Load users from the file
    public static List<user_c> loadUsers() {
        List<user_c> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    user_c user = new user_c(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user file: " + e.getMessage());
        }
        return users;
    }

    // Add a new user to the file
    public static void addUser(user_c user) {
        if (!isValidUser(user)) {
            return; // ✅ Add this line
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(user.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing user file: " + e.getMessage());
        }
    }

    // Update an existing user in the file
    public static void updateUser(user_c updatedUser) {
        if (!isValidUser(updatedUser)) {
            return; // ✅ Add this line
        }
        File inputFile = new File(USER_FILE);
        StringBuilder updatedContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && parts[0].equals(updatedUser.getId())) {
                    updatedContent.append(updatedUser.toFileString()).append("\n");
                } else {
                    updatedContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user file: " + e.getMessage());
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
            bw.write(updatedContent.toString().trim());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error updating user file: " + e.getMessage());
        }
    }

    // Delete a user from the file by user ID
    public static void deleteUser(String userId) {
        File inputFile = new File(USER_FILE);
        StringBuilder updatedContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(userId + "|")) {
                    updatedContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user file: " + e.getMessage());
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
            bw.write(updatedContent.toString().trim());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error updating user file: " + e.getMessage());
        }
    }

    public static boolean isValidUser(user_c user) {
        if (!user.getContact().matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(null, "Invalid contact number. It must be 10–11 digits.");
            return false;
        }

        if (!user.getEmail().endsWith("@gmail.com")) {
            JOptionPane.showMessageDialog(null, "Invalid email. It must end with @gmail.com");
            return false;
        }

        String password = user.getPassword();
        int digitCount = 0;
        int letterCount = 0;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                digitCount++;
            } else if (Character.isLetter(c)) {
                letterCount++;
            }
        }

        if (password.length() < 8 || digitCount < 6 || letterCount < 2) {
            JOptionPane.showMessageDialog(null, "Invalid password. Must be at least 8 characters with 6 digits and 2 letters.");
            return false;
        }

        return true;
    }

    // Generate the next user ID (for adding a new user)
    public static String generateNextUserId() {
        int lastId = 1000;
        List<user_c> users = loadUsers();
        for (user_c u : users) {
            int id = Integer.parseInt(u.getId());
            if (id > lastId) {
                lastId = id;
            }
        }
        return String.valueOf(lastId + 1);
    }
}
