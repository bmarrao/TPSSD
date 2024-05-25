
package blockchain;

import auctions.BrokerService;
import kademlia.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Blockchain
{

    private static final int TRANSACTIONS_LIMIT = 5;
    Block latestMinedBlock;
    // currentMiningBlock
    Block currentMiningBlock;
    private final ArrayList<Block> blocks;
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
        this.blocks = new ArrayList<>();
        this.myAuctions = new ArrayList<>();
        this.difficulty = initialDifficulty;
        Block genesis = createGenesisBlock();
        this.latestMinedBlock = genesis;

    }

    // Create the genesis block
    private Block createGenesisBlock()
    {
        // Implement logic to create the first block in the blockchain
        ArrayList<Transaction> transactions = new ArrayList<>();
        Block genesisBlock = new Block(null, transactions,null);
        // Ensure the genesis block has valid proof-of-work
        genesisBlock.mineBlock(difficulty);

        return genesisBlock;
    }


    public Block findBlock(byte[] currentHash)
    {

        for(Block b :blocks)
        {
            if (compareId(b.getHash(),currentHash))
            {
                return b;
            }
        }
        grpcBlock b = this.k.sKadBlockLookup(currentHash,3);
        return this.addBlock(b);
    }
    // Getters
    public Block getLastBlock() {
        return this.latestMinedBlock;
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

    public Block addBlock(grpcBlock block)
    {
        Block block1 = new Block(block);
        // Verify previous block reference and that POW was done
        String target = new String(new char[difficulty]).replace('\0', '0');
        byte[] bcLatestBlockHash = block.getCurrentHash().toByteArray();
        byte[] previousBlockHash = block1.getPreviousHash();
        if (!Arrays.equals(bcLatestBlockHash, previousBlockHash) || !compareId(bcLatestBlockHash,previousBlockHash) || !Arrays.toString(block1.getHash()).startsWith(target))
        {
            this.k.rt.setReputation(block1.getNode(),-1);
            if (isBlockValid(block1))
            {
                latestMinedBlock =block1  ;
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
            if(!isTransactionValid(transaction,block))
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
    public boolean isTransactionValid(Transaction transaction)
    {
        boolean isValid;

        HashMap<Integer, Transaction> sameAuctionTransactions = getSameAuctionTransactionsFromBlockchain(transaction);

        if (!verifyTransactionSignature(transaction) || !verifyTransactionTypeConsistency(transaction, sameAuctionTransactions)) {
            isValid = false;
        }
        else
        {
            //Significa que a Transação é coerente com transações anteriores para a mesma auction e que tem a assinatura válida
            //TODO Validar se o limite de transactions foi atingido
            //Se sim, minerar bloco novo e colocar transaction lá. Isto será fora. No método que pedir a validação da transaction

            isValid = true;
        }

        return isValid;
    }

    // Transaction Type , Transaction Object
    private HashMap<Integer, Transaction> getSameAuctionTransactionsFromBlockchain(Transaction receivedTransaction){

        HashMap<Integer, Transaction> sameAuctionTransactions = new HashMap<>();

        for (Block b : this.blocks) {

            for (Transaction t : b.getTransactionList()) {

                if(t.getId().equals(receivedTransaction.getId()) && t.getOwner().equals(receivedTransaction.getOwner())){
                    sameAuctionTransactions.put(t.getType(), t);
                }

            }

        }

        return sameAuctionTransactions;
    }

    private HashMap<Integer, Transaction> getSameAuctionTransactionsFromBlockchain(Transaction receivedTransaction, Block block){

        HashMap<Integer, Transaction> sameAuctionTransactions = new HashMap<>();

        Block previousBlock = block.getPreviousBlock();

        while(previousBlock != null){

            for (Transaction t : block.getTransactionList()) {

                if(t.getId().equals(receivedTransaction.getId()) && t.getOwner().equals(receivedTransaction.getOwner())){
                    sameAuctionTransactions.put(t.getType(), t);
                }

            }
            //TODO Posso fazer isto?
            block = previousBlock;

            previousBlock = block.getPreviousBlock();

        }

        return sameAuctionTransactions;
    }

    private boolean verifyTransactionTypeConsistency(Transaction transaction, HashMap<Integer, Transaction> sameAuctionTransactions){

        boolean isValid = true;

        switch (transaction.getType()) {
            case 1:
                //Verificar se existe alguma Transaction.
                if(!sameAuctionTransactions.isEmpty()){
                    isValid = true;
                }
                break;
            case 2:
                //Verificar se existe uma auction a decorrer. Se sim, fazer close.
                if(sameAuctionTransactions.get(1) != null && sameAuctionTransactions.get(2) == null){
                    isValid = true;
                }
                break;
            default:
                //Verificar se exista uma auction a decorrer. Se sim, verificar se o valor é maior que o atual.
                if(sameAuctionTransactions.get(1) != null && sameAuctionTransactions.get(2) == null){
                    if(sameAuctionTransactions.get(0) == null) {
                        isValid = true;
                    }else if(transaction.getSender().getPrice() > sameAuctionTransactions.get(0).getSender().getPrice()) {
                        isValid = true;
                    }
                }
                break;
        }

        return isValid;
    }


    private boolean isBlockTransactionValid(Transaction transaction, Block block)
    {

        boolean isValid;

        HashMap<Integer, Transaction> sameAuctionTransactions = getSameAuctionTransactionsFromBlockchain(transaction, block);

        if (!verifyTransactionSignature(transaction) || !verifyTransactionTypeConsistency(transaction, sameAuctionTransactions)) {
            isValid = false;
        }
        else
        {
            //Significa que a Transação é coerente com transações anteriores para a mesma auction e que tem a assinatura válida
            //TODO Validar se o limite de transactions foi atingido
            //Se sim, minerar bloco novo e colocar transaction lá. Isto será fora. No método que pedir a validação da transaction

            isValid = true;
        }

        return isValid;
    }

    private boolean verifyTransactionSignature(Transaction transaction)
    {
        boolean isValid = false;
        byte[] transactionOriginalSign = transaction.getSignature().toByteArray();

        byte[] serviceID = transaction.getId().toByteArray();
        byte[] ownerToVerify = transaction.getOwner().toByteArray();
        byte[] senderNodeToVerify = transaction.getSender().getNode().toByteArray();

        int totalLength = serviceID.length + ownerToVerify.length + senderNodeToVerify.length;
        byte[] infoToVerify = new byte[totalLength];

        System.arraycopy(serviceID, 0, infoToVerify, 0, serviceID.length);
        System.arraycopy(ownerToVerify, 0, infoToVerify, serviceID.length, ownerToVerify.length);
        System.arraycopy(senderNodeToVerify, 0, infoToVerify, serviceID.length + ownerToVerify.length, senderNodeToVerify.length);

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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(block.getPreviousHash());
            outputStream.write(block.getTransactionList().toString().getBytes(StandardCharsets.UTF_8));
            outputStream.write(block.getHash());
            outputStream.write(ByteBuffer.allocate(8).putLong(block.getTimestamp()).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(block.getNonce()).array());
            outputStream.write(ByteBuffer.allocate(4).putFloat(block.getReputation()).array());
            outputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        try {
            if (!SignatureClass.verify(outputStream.toByteArray(), block.getSignature(), block.getNode().getPublicKey().toByteArray()))
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

