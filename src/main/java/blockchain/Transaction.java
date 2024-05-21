package blockchain;

import kademlia.KademliaNode;

public class Transaction {

    public enum TransactionType {
        BID,
        OPENING,
        CLOSURE
        // Add more transaction types as needed
    }
    private KademliaNode sender;
    private KademliaNode receiver;
    private float price;
    private String serviceID;

    private TransactionType type;

    public Transaction(KademliaNode owner, float price, String serviceID, TransactionType type) {
        this.receiver = owner;
        this.price = price;
        this.serviceID = serviceID;
        this.type = type;
    }

    public KademliaNode getSender() {
        return sender;
    }

    public void setSender(KademliaNode sender) {
        this.sender = sender;
    }

    public KademliaNode getReceiver() {
        return receiver;
    }

    public void setReceiver(KademliaNode receiver) {
        this.receiver = receiver;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
