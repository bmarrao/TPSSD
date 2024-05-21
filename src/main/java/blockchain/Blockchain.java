
package blockchain;

import auctions.BrokerService;
import kademlia.Node;
import kademlia.Offer;
import kademlia.SignatureClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Blockchain
{
    private final List<Block> chain;
    private final int difficulty;
    HashMap<byte[] ,Transaction > transactions;
    ArrayList<byte[]> topicsSubscribed;
    // Constructor
    public Blockchain(int initialDifficulty)
    {
        this.chain = new ArrayList<>();
        this.topicsSubscribed = new ArrayList<>();
        this.difficulty = initialDifficulty;
        Block genesis = createGenesisBlock();
        this.chain.add(genesis);

    }

    // Create the genesis block
    private Block createGenesisBlock() {
        // Implement logic to create the first block in the blockchain
        ArrayList<Transaction> transactions = new ArrayList<>();
        Block genesisBlock = new Block("", transactions);
        // Ensure the genesis block has valid proof-of-work
        genesisBlock.mineBlock(difficulty);

        return genesisBlock;
    }


    // Getters
    public List<Block> getChain() {
        return chain;
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void newBid(byte[] serviceId, Offer of)
    {
        // Adicionar no bloco
        // criar uma instancia de uma transaction
        // disseminar transaction
    }
    public void newAuction(byte[] serviceId, Node owner)
    {
        // Adicionar no bloco
        // criar uma instancia de uma transaction
        // disseminar transaction
    }

    public void closeAuction(byte[] serviceId, Offer highestOffer, Node owner)
    {
        // Adicionar no bloco
        // criar uma instancia de uma transaction
        // disseminar transaction
    }

    // Method to add a new block to the blockchain
    public void addBlock(Block newBlock) {
        // Set the previous hash to the hash of the latest block
        newBlock.mineBlock(difficulty); // Ensure the block has valid proof-of-work
        chain.add(newBlock);
    }


    public Transaction getInformation(String service)
    {
        byte[] serviceId = encryptService(service);
        //TODO : FINISH THIS METHOD
        return null;
    }
    public byte[] addSubscribe(String service)
    {
        byte[] serviceId = encryptService(service);
        this.topicsSubscribed.add(serviceId);
        return serviceId;
    }

    public boolean removeSubscribe(String service)
    {
        byte[] serviceId = encryptService(service);
        byte[] toRemove = this.getSubscription(serviceId);
        return topicsSubscribed.remove(toRemove);
    }
    public byte[] encryptService(String service)
    {
        byte[] serviceId  = null;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            serviceId = sha1.digest(service.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return serviceId;
    }

    private boolean compareId(byte[] id1, byte[] id2)
    {

        // Iterate through each byte and compare them
        for (int i = 0; i < id1.length; i++)
        {
            if (id1[i] != id2[i])
            {
                return false; // If any byte differs, return false
            }
        }


        // If all bytes are the same, return true
        return true;
    }
    public byte[] getSubscription(byte[] serviceId)
    {
        for(byte[] bs : this.topicsSubscribed)
        {
            if (compareId(bs,serviceId))
            {
                return bs;
            }
        }
        return null;
    }

    // Method to validate the blockchain
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Validate the block's hash
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Block " + i + " has been tampered with!");
                return false; // Hash doesn't match, block may have been tampered with
            }

            // Validate the previous hash
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("Block " + i + " has incorrect previous hash!");
                return false; // The block's previous hash doesn't point to the correct previous block
            }
        }
        return true; // All checks passed, the blockchain is valid
    }


    public boolean isBlockValid(Block block, int difficulty, float repIncreasePercentage) {
        int currRepScore = block.getReputation();

        // Verify previous block reference and that POW was done
        String target = new String(new char[difficulty]).replace('\0', '0');
        String bcLatestBlockHash = getLatestBlock().getHash();
        String previousBlockHash = block.getPreviousHash();
        if (!bcLatestBlockHash.equals(previousBlockHash) || !block.getHash().startsWith(target)) {
            block.setReputation(0);
            return false;
        }

        // Verify block signature
        byte[] infoToVerify = {
                Byte.parseByte(block.getPreviousHash()),
                (byte) block.getTimestamp(),
                (byte) block.getNonce(),
                Byte.parseByte(String.valueOf(block.getTransactionList())),
                Byte.parseByte(block.getHash()),
                (byte) block.getReputation()
        };

        try {
            if (!SignatureClass.verify(infoToVerify, block.getSignature(), block.getPublicKey().getEncoded())) {
                block.setReputation(0);
                return false;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Verify block transactions
        ArrayList<Transaction> transactions = block.getTransactionList();
        if (transactions.isEmpty()) {
            block.setReputation(0);
            return false;
        }
        for (Transaction transaction : transactions) {
            if (transaction.getPrice() <= 0 || transaction.getSender() == null || transaction.getReceiver() == null) {
                block.setReputation(0);
                return false;
            }
        }

        // increases reputation based on defined percentage
        if (currRepScore != 0) {
            block.setReputation((int) ((currRepScore * repIncreasePercentage) + currRepScore));
        }
        else {
            block.setReputation((int) (0.01 + currRepScore)); // TODO: mudar
        }
        return true;
    }
}

