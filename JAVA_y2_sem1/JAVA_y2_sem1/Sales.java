import java.io.Serializable;
import java.time.LocalDate;

public class Sales implements Serializable {
    private String salesId;
    private String itemId;
    private LocalDate salesDate;
    private int quantitySold;
    private int remainingStock;
    private String salesPerson;

    public Sales(String salesId, String itemId, LocalDate salesDate, int quantitySold, int remainingStock, String salesPerson) {
        this.salesId = salesId;
        this.itemId = itemId;
        this.salesDate = salesDate;
        this.quantitySold = quantitySold;
        this.remainingStock = remainingStock;
        this.salesPerson = salesPerson;
    }

    // Getters
    public String getSalesId() { return salesId; }
    public String getItemId() { return itemId; }
    public LocalDate getSalesDate() { return salesDate; }
    public int getQuantitySold() { return quantitySold; }
    public int getRemainingStock() { return remainingStock; }
    public String getSalesPerson() { return salesPerson; }

    @Override
    public String toString() {
        return salesId + "|" + itemId + "|" + salesDate + "|" + quantitySold + "|" + remainingStock + "|" + salesPerson;
    }
}