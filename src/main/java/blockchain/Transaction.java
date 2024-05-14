package blockchain;

public class Transaction {

    public enum TransactionType {
        BID,
        OPENING,
        CLOSURE
        // Add more transaction types as needed
    }
    private String sender;
    private String receiver;
    private double amount;
    // Add other fields as needed
    private TransactionType type;

    // Constructor
    public Transaction(String sender, String receiver, double amount, TransactionType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                '}';
    }
}
