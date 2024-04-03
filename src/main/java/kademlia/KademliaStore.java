package kademlia;

import java.util.HashMap;
import java.util.Map;

public class KademliaStore
{
    // TODO desenvolver classe - Hugo
    // Id do que queremos guardar - Key, Object - o que foi pedido para guardar
    // BLOCKCHAIN
    // Informação referente a auction
    private static KademliaStore instance;
    private Map<String, String> dataStored;

    private KademliaStore() {
        // Initialize the data storage
        dataStored = new HashMap<>();
    }

    // Singleton pattern to ensure only one instance of the data store exists
    public static synchronized KademliaStore getInstance() {
        if (instance == null) {
            instance = new KademliaStore();
        }
        return instance;
    }

    // Method to store a key-value pair in the data store
    public synchronized void store(String key, String value) {
        dataStored.put(key, value);
    }

    // Method to retrieve the value associated with a key from the data store
    public synchronized String findValue(String key) {
        return dataStored.get(key);
    }

}
