
package blockchain;

import auctions.BrokerService;
import kademlia.*;
import org.checkerframework.checker.units.qual.A;

import javax.accessibility.AccessibleIcon;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.*;


public class Blockchain
{

    private static final int TRANSACTIONS_LIMIT = 5;
    Block latestBlock;
    private final HashMap<String, Block> blocks;
    private final Kademlia k;
    private final List<BrokerService> myAuctions;
    private final int difficulty;
    HashMap<AuctionId , Transaction> activeAuctions;
    ArrayList<byte[]> topicsSubscribed;
    // Constructor
    public Blockchain(int initialDifficulty, Kademlia k)
    {
        this. k = k;
        this.topicsSubscribed = new ArrayList<>();
        this.activeAuctions = new HashMap<>();
        this.blocks = new HashMap<>();
        this.myAuctions = new ArrayList<>();
        this.difficulty = initialDifficulty;
        Block genesis = createGenesisBlock();
        this.latestBlock = genesis;

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
    public Block getLastBlock() {
        return latestBlock;
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


    public Transaction getInformation(String service, Node owner)
    {
        byte[] serviceId = encryptService(service);
        return this.activeAuctions.get(new AuctionId(serviceId, owner));
    }

    public ArrayList<Transaction> getInformation(String service)
    {
        byte[] serviceId = encryptService(service);
        ArrayList<Transaction> ret = new ArrayList<>();
        for (Transaction t : this.activeAuctions.values())
        {
            if (compareId(t.getId().toByteArray(),serviceId))
            {
                ret.add(t);
            }
        }
        return ret;
    }
    public byte[] addSubscribe(String service)
    {
        byte[] serviceId = encryptService(service);
        this.topicsSubscribed.add(serviceId);
        return serviceId;
    }

    public byte[] addBlock(grpcBlock block)
    {
        Block block1 = new Block(block);
        // Verify previous block reference and that POW was done
        String target = new String(new char[difficulty]).replace('\0', '0');
        String bcLatestBlockHash = getLatestBlock().getH;
        String previousBlockHash = block1.getPreviousHash();
        if (!bcLatestBlockHash.equals(previousBlockHash) || !block1.getHash().startsWith(target))
        {
            this.k.rt.setReputation(block1.getNode(),-1);
            if (isBlockValid(block1))
            {
                chain.add(block1);
            }
        }
        else
        {

        }
        //resolveForks();
        return null;
    }
    /*
    private void resolveForks() {
        // Check all orphan blocks to see if they can be connected to the main chain
        List<Block> toBeRemoved = new ArrayList<>();
        for (Block orphan : orphanBlocks) {
            if (isValidNewBlock(orphan, getLastBlock())) {
                chain.add(orphan);
                toBeRemoved.add(orphan);
            }
        }
        orphanBlocks.removeAll(toBeRemoved);

        // Optionally, implement more sophisticated fork resolution
    }
    */

    private boolean hasValidTransactions(Block block, ArrayList<Transaction> transactions)
    {
        for (Transaction transaction : transactions)
        {
            if(!isTransactionSignValid(transaction))
            {
                return false;
            }
        }
        return true;
    }

    //There are 3 Transactions Type:
    // 0 -> Bid
    // 1 -> NewAuction
    // 2 -> CloseAuction
    private boolean isTransactionSignValid(Transaction transaction, Block block)
    {
        boolean isValid = false;
        Transaction lastTransaction;

        /*for (Block b : chain)
        {

        }*/

        //Checa no bloco ;
        // Se não tiver no bloco
        // Block newB = getBLock(b.previousHash)
        // return isTransactionValid(t, newB)

        if(block != null)
        {
            lastTransaction = isBlockTransactionValid(transaction, block);
            

        }else{

            if(!verifyTransactionSignature(transaction)){
                return isValid;
            }
            else
            {
                //TODO Validar se o limite de transactions foi atingido
                //Se sim, minerar bloco novo e colocar transaction lá.

                //TODO Validar se existe uma auction

                switch (transaction.getType()) {
                    case 1:
                        //Verificar se já não existe uma. Dar put se não existir.
                        break;
                    case 2:
                        //Verificar se existe uma auction a decorrer. Se sim, fazer close.
                        break;
                    default:
                        //Verificar se exista uma auction a decorrer. Se sim, verificar se o valor é maior que o atual.
                        break;
                }

            }
        }

        return isValid;
    }

    private boolean verifyTransactionSignature(Transaction transaction)
    {
        boolean isValid = false;

        byte[] transactionOriginalSign = transaction.getSignature().toByteArray();
        byte[] serviceID = transaction.getId().toByteArray();
        byte[] senderNodeToVerify = transaction.getSender().getNode().toByteArray();
        byte[] transactionToVerify = transaction.toByteArray();

        int senderAndTransactionLength = senderNodeToVerify.length + transactionToVerify.length;
        int totalLength = senderAndTransactionLength + serviceID.length;

        byte[] infoToVerify = new byte[totalLength];

        System.arraycopy(senderNodeToVerify, 0, infoToVerify, 0, senderNodeToVerify.length);;
        System.arraycopy(transactionToVerify, 0, infoToVerify, senderNodeToVerify.length, transactionToVerify.length );;
        System.arraycopy(serviceID, 0, infoToVerify, senderAndTransactionLength, serviceID.length);

        try {
            byte[] publicKey = transaction.getSender().getNode().getPublicKey().toByteArray();
            if (SignatureClass.verify(infoToVerify, transactionOriginalSign, publicKey)) {
                isValid = true;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return isValid;

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


    public boolean isBlockValid(Block block)
    {


        // TODO TEST THIS
        // Verify block signature
        byte[] infoToVerify = 
        {
                Byte.parseByte(block.getPreviousHash()),
                (byte) block.getTimestamp(),
                (byte) block.getNonce(),
                Byte.parseByte(String.valueOf(block.getTransactionList())),
                Byte.parseByte(block.getHash()),
        };

        try {
            if (!SignatureClass.verify(infoToVerify, block.getSignature(), block.getNode().getPublicKey().toByteArray()))
            {
                this.k.rt.setReputation(block.getNode(),-1);
                return false;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Verify block transactions
        ArrayList<Transaction> transactions = block.getTransactionList();
        if (transactions.isEmpty())
        {
            this.k.rt.setReputation(block.getNode(),-1);
            return false;
        }
        for (Transaction transaction : transactions)
        {
            if(!isTransactionSignValid(transaction, block))
            {
                this.k.rt.setReputation(block.getNode(),-1)      ;
                return false;
            }
        }

        this.k.rt.setReputation(block.getNode(),1);
        block.setReputation(1);
        return true;
    }

    public boolean checkValidityOfTransactions(ArrayList<Transaction> transactions, Block block)
    {
        return false;
    }

    public class AuctionId
    {
        byte[] serviceId;
        Node Owner;
        AuctionId(byte[] serviceId, Node owner)
        {
            this.serviceId = serviceId;
            this.Owner = owner;
        }

        public boolean equals(AuctionId a)
        {
            return compareId(a.serviceId,this.serviceId) && a.Owner.equals(this.Owner);
        }
    }
}

