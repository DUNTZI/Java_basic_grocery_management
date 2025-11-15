import java.io.Serializable;

public class Supplier implements Serializable {
    private int id;
    private String name;
    private String contact;
    private String email;
    private String address;
    private String item;

    public Supplier(int id, String name, String contact, String email, String address, String item) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.address = address;
        this.item = item;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getItem() { return item; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setItem(String item) { this.item = item; }
}
