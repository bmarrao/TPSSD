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
        private Map<byte[], Node> dataStored;

        KademliaStore() {
            // Initialize the data storage
            dataStored = new HashMap<>();
        }

        /*
        // Singleton pattern to ensure only one instance of the data store exists
        public static synchronized KademliaStore getInstance() {
            if (instance == null) {
                instance = new KademliaStore();
            }
            return instance;
        }

         */

        // Method to store a key-value pair in the data store
        public synchronized void store(byte[] key, Node value) {
            dataStored.put(key, value);
        }

        // Method to retrieve the value associated with a key from the data store
        public synchronized Node findValue(byte[] key) {
            for (Map.Entry<byte[], Node> entry : dataStored.entrySet()) {
                byte[] storedKey = entry.getKey();
                if (storedKey.length != key.length) {
                    continue; // Skip if lengths are different
                }
                boolean equal = true;
                for (int i = 0; i < storedKey.length; i++) {
                    if (storedKey[i] != key[i]) {
                        equal = false;
                        break; // Exit loop if a mismatch is found
                    }
                }
                if (equal) {
                    return entry.getValue(); // Return value if keys match
                }
            }
            return null; // Return null if key not found
        }

    }
