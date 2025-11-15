import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class finance_c {
    private static final String FINANCE_FILE = "TXT/finance.txt";
    private static final String PO_FILE = "TXT/po.txt";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_VERIFIED = "Verified";
    public static final String STATUS_NOT_VERIFIED = "Not Verified";
    public static final String STATUS_PAID = "Paid";
    
    private List<FinanceRecord> financeRecords = new ArrayList<>();

    public finance_c() {
        loadFinanceData();
    }

    public List<FinanceRecord> getFinanceRecords() {
        return financeRecords;
    }

    public void addFinanceRecord(FinanceRecord record) {
        financeRecords.add(record);
        saveFinanceData();
    }

    public void updateFinanceRecord(FinanceRecord updatedRecord) {
        for (int i = 0; i < financeRecords.size(); i++) {
            if (financeRecords.get(i).getFinanceId().equals(updatedRecord.getFinanceId())) {
                financeRecords.set(i, updatedRecord);
                saveFinanceData();
                break;
            }
        }
    }

    public void deleteFinanceRecord(String financeId) {
        for (int i = 0; i < financeRecords.size(); i++) {
            if (financeRecords.get(i).getFinanceId().equals(financeId)) {
                financeRecords.remove(i);
                saveFinanceData();
                break;
            }
        }
    }

    public boolean isPoPaid(String poId) {
        for (FinanceRecord record : financeRecords) {
            if (record.getPoId().equals(poId)) {
                return record.getPaymentStatus().equals(STATUS_PAID);
            }
        }
        return false;
    }

    public List<String> loadPoIds() {
        List<String> poIds = new ArrayList<>();
        poIds.add(""); // Add empty option
        
        try (BufferedReader br = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0) {
                    String poId = data[0].trim();
                    if (!poId.isEmpty() && !isPoPaid(poId)) {
                        poIds.add(poId);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading PO file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return poIds;
    }

    private void loadFinanceData() {
        financeRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FINANCE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 7) {
                    String financeId = data[0].trim();
                    String poId = data[1].trim();
                    String approvalStatus = data[2].trim();
                    String paymentStatus = data[3].trim();
                    String paymentDate = data[4].trim();
                    String amount = data[5].trim();
                    int verifiedBy = Integer.parseInt(data[6].trim());

                    FinanceRecord record = new FinanceRecord(
                            financeId,
                            poId,
                            approvalStatus,
                            paymentStatus,
                            paymentDate,
                            amount,
                            verifiedBy
                    );
                    financeRecords.add(record);
                } else {
                    System.err.println("Skipping invalid line in finance.txt: " + line + ". Expected 7 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading finance file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing verifiedBy in finance.txt: " + e.getMessage());
        }
    }

    public void saveFinanceData() {
        try (FileWriter writer = new FileWriter(FINANCE_FILE)) {
            for (FinanceRecord record : financeRecords) {
                writer.write(record.getFinanceId() + "|"
                        + record.getPoId() + "|"
                        + record.getApprovalStatus() + "|"
                        + record.getPaymentStatus() + "|"
                        + record.getPaymentDate() + "|"
                        + record.getAmount() + "|"
                        + record.getVerifiedBy() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving finance file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String generateNewFinanceId() {
        int lastId = 8000;
        for (FinanceRecord record : financeRecords) {
            try {
                int currentId = Integer.parseInt(record.getFinanceId());
                if (currentId > lastId) {
                    lastId = currentId;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric IDs
            }
        }
        return String.valueOf(lastId + 1);
    }

    public static class FinanceRecord {
        private String financeId;
        private String poId;
        private String approvalStatus;
        private String paymentStatus;
        private String paymentDate;
        private String amount;
        private int verifiedBy;

        public FinanceRecord(String financeId, String poId, String approvalStatus, String paymentStatus, 
                           String paymentDate, String amount, int verifiedBy) {
            this.financeId = financeId;
            this.poId = poId;
            this.approvalStatus = approvalStatus;
            this.paymentStatus = paymentStatus;
            this.paymentDate = paymentDate;
            this.amount = amount;
            this.verifiedBy = verifiedBy;
        }

        public String getFinanceId() { return financeId; }
        public String getPoId() { return poId; }
        public String getApprovalStatus() { return approvalStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getPaymentDate() { return paymentDate; }
        public String getAmount() { return amount; }
        public int getVerifiedBy() { return verifiedBy; }

        public void setPoId(String poId) { this.poId = poId; }
        public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
        public void setAmount(String amount) { this.amount = amount; }
        public void setVerifiedBy(int verifiedBy) { this.verifiedBy = verifiedBy; }
    }
}