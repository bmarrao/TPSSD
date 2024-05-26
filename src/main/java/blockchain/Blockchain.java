
package blockchain;

import auctions.BrokerService;
import com.google.protobuf.ByteString;
import kademlia.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;
import java.util.Random;


public class Blockchain
{
    private static final int TRANSACTIONS_LIMIT = 5;
    private static final double GOSSIP_CHANCE = 0.1;

    Block latestMinedBlock;
    // currentMiningBlock
    Block currentMiningBlock;
    private final ArrayList<Block> blocks;
    Thread miningBlock;
    private final Kademlia k;
    private final int difficulty;
    private PrivateKey privateKey;
    HashMap<AuctionId , BrokerService> activeAuctions;
    ArrayList<Transaction> pendingTransactions;
    ArrayList<byte[]> topicsSubscribed;
    Random random = new Random();

    // Constructor
    public Blockchain(int initialDifficulty, Kademlia k, PrivateKey privateKey)
    {
        this. k = k;
        this.topicsSubscribed = new ArrayList<>();
        this.activeAuctions = new HashMap<>();
        this.blocks = new ArrayList<>();
        this.difficulty = initialDifficulty;
        this.privateKey = privateKey;
        this.pendingTransactions = new ArrayList<>();
        this.latestMinedBlock = createGenesisBlock();
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
        return this.addBlock(new Block(b));
    }
    // Getters
    public Block getLastBlock() {
        return this.latestMinedBlock;
    }


    public int getDifficulty() {
        return difficulty;
    }

    public void newAuction(byte[] serviceId, Node owner, BrokerService bs)
    {
        this.activeAuctions.put(new AuctionId(serviceId,owner),bs);
        byte[] ownerData = owner.toByteArray();
        byte[] offer = bs.getOffer().toByteArray();
        byte[] data = new byte[serviceId.length + ownerData.length + offer.length];

        System.arraycopy(serviceId, 0, data, 0, serviceId.length);
        System.arraycopy(ownerData, 0, data, serviceId.length, ownerData.length);
        System.arraycopy(offer, 0, data, serviceId.length+ownerData.length, offer.length);

        byte[] signature = this.k.signData(data);

        Transaction t= Transaction.newBuilder()
                .setId(ByteString.copyFrom(serviceId)).setOwner(owner).setType(1)
                .setSignature(ByteString.copyFrom(signature)).build();
        this.addTransaction(t);
    }

    public Collection<Transaction> getInformation(String service)
    {
        byte[] serviceId = encryptService(service);
        return getInformationFromBlockchain(serviceId,latestMinedBlock).values();
    }

    public byte[] addSubscribe(String service)
    {
        byte[] serviceId = encryptService(service);
        this.topicsSubscribed.add(serviceId);
        return serviceId;
    }

    public Block addBlock(Block block1)
    {
        // Verify previous block reference and that POW was done
        String target = new String(new char[difficulty]).replace('\0', '0');
        byte[] bcLatestBlockHash = block1.getHash();
        byte[] previousBlockHash = block1.getPreviousHash();
        block1.setPreviousBlock(findBlock(block1.getPreviousHash()));

        // Convert the byte array to a hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : bcLatestBlockHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        if (!Arrays.equals(bcLatestBlockHash, previousBlockHash) || !compareId(bcLatestBlockHash,previousBlockHash) || !hexString.toString().startsWith(target))
        {

            if (isBlockValid(block1))
            {
                if(resolveForks(block1))
                {
                    latestMinedBlock =block1 ;
                    adjustCurrentMiningBlock();
                    gossipBlockToOthers(block1);
                }
            }
            else
            {
                this.k.rt.setReputation(block1.getNode(),-1);

            }
        }
        else
        {
            if (isBlockValid(block1))
            {
                latestMinedBlock =block1  ;
                adjustCurrentMiningBlock();
                gossipBlockToAllOthers(block1);
            }
            else
            {
                this.k.rt.setReputation(block1.getNode(),-1);

            }
        }

        return null;
    }

    private void adjustCurrentMiningBlock()
    {
        for(Transaction t: currentMiningBlock.getTransactionList())
        {
            if (isTransactionValid(t,latestMinedBlock))
            {
                this.pendingTransactions.add(0,t);
            }
        }
        changeInLatestBlok();
    }
    private boolean resolveForks(Block block)
    {
        LengthSumRep lrBlock = new LengthSumRep();
        LengthSumRep lrLastMinedBlock = new LengthSumRep();
        lrBlock = calculateChainLength(block,lrBlock);
        lrLastMinedBlock = calculateChainLength(latestMinedBlock,lrLastMinedBlock);
        if (lrBlock.length == lrLastMinedBlock.length)
        {
            return lrBlock.sumRep >= lrLastMinedBlock.sumRep;
        }
        else return lrBlock.length > lrLastMinedBlock.length;
    }


    //There are 3 Transactions Type:
    // 0 -> Bid
    // 1 -> NewAuction
    // 2 -> CloseAuction
    public boolean isTransactionValid(Transaction transaction, Block block)
    {
        boolean isValid = false;
        Transaction lastTransaction;


        if(block == null){
            lastTransaction = getSameAuctionTransactionsFromBlockchain(transaction, this.getLastBlock());
        }else{
            lastTransaction = getSameAuctionTransactionsFromBlockchain(transaction, block);
        }

        if (verifyTransactionSignature(transaction) || verifyTransactionTypeConsistency(transaction, lastTransaction)) {

            //Significa que a Transação é coerente com transações anteriores para a mesma auction e que tem a assinatura válida
            isValid = true;
        }

        return isValid;
    }

    private Transaction getSameAuctionTransactionsFromBlockchain(Transaction receivedTransaction, Block block){

        Transaction lastTransactionFromAuction = null;
        boolean hasFound = false;

        while(block != null && !hasFound){

            for(int i = block.getTransactionList().size(); i > 0 ; i--){

                Transaction t = block.getTransactionList().get(i);

                if(t.getId().equals(receivedTransaction.getId()) && t.getOwner().equals(receivedTransaction.getOwner())){
                    lastTransactionFromAuction = t;
                    hasFound = true;
                }
            }
            block = block.getPreviousBlock();
        }

        return lastTransactionFromAuction;
    }

    private HashMap<AuctionId, Transaction> getInformationFromBlockchain(byte[] serviceId, Block block)
    {

        HashMap<AuctionId, Transaction> ret = new HashMap<>();

        AuctionId key ;
        byte[] tServiceId;

        while(block != null)
        {

            for(int i = block.getTransactionList().size(); i > 0 ; i--){

                Transaction t = block.getTransactionList().get(i); 
                tServiceId = t.getId().toByteArray();
                if(compareId(tServiceId,serviceId) )
                {
                    key = new AuctionId(tServiceId,t.getOwner());
                    if(!(ret.containsKey(key)))
                    {
                        ret.put(key,t);
                    }
                }
            }
            block = block.getPreviousBlock();
        }

        return ret;
    }

    private boolean verifyTransactionTypeConsistency(Transaction transaction, Transaction lastTransaction){

        boolean isValid = false;

        switch (transaction.getType()) {
            case 1:
                //Verificar se existe alguma Transaction.
                if(lastTransaction == null || lastTransaction.getType() == 2){
                    isValid = true;
                }
                break;
            case 2:
                //Verificar se existe uma auction a decorrer. Se sim, fazer close.
                if(lastTransaction.getType() == 0 || lastTransaction.getType() == 1){
                    isValid = true;
                }
                break;
            default:
                //Verificar se exista uma auction a decorrer. Se sim, verificar se o valor é maior que o atual.
                if(lastTransaction.getType() == 1 && lastTransaction.getSender().getPrice() > 0){
                    BrokerService bs = activeAuctions.get(new AuctionId(transaction.getId().toByteArray(),transaction.getOwner()));
                    bs.receiveOffer(transaction.getSender());
                    isValid = true;
                }else if(lastTransaction.getType() == 0 && (transaction.getSender().getPrice() > lastTransaction.getSender().getPrice()) ){
                    isValid = true;
                }
                break;
        }
        return isValid;
    }

    public void addTransaction(Transaction t)
    {
        this.pendingTransactions.add(t);
        byte[] transactionId = t.getId().toByteArray();
        for(byte[] id  : this.topicsSubscribed )
        {
            if (compareId(id,transactionId))
            {
                System.out.println("New bid on topic you subscribed : " + k.rt.printId(id) + "Bid of " + t.getSender().getPrice());
            }
        }
        startMiningIfConditions();
        gossipTransactionToOthers(t);
    }

    public void startMiningIfConditions()
    {
        if (currentMiningBlock == null)
        {
            if (this.pendingTransactions.size() >= TRANSACTIONS_LIMIT) {

                ArrayList<Transaction> transactionToBlock = this.maxLimitPendingTransactions(pendingTransactions);
                currentMiningBlock = new Block(this.latestMinedBlock.getHash(), transactionToBlock, this.latestMinedBlock);
                MineThread mt = new MineThread(this.currentMiningBlock, this.difficulty,this);
                this.miningBlock = new Thread(mt);
                miningBlock.start();
            }
        }
    }

    public void changeInLatestBlok()
    {
        if (!(currentMiningBlock != null))
        {
            currentMiningBlock.running = false;
            currentMiningBlock = null;
            startMiningIfConditions();
        }
    }


    private ArrayList<Transaction> maxLimitPendingTransactions(ArrayList<Transaction> pendingTransactions){

        ArrayList<Transaction> transactionsToBlock = new ArrayList<>();

        for(int i=0; i < TRANSACTIONS_LIMIT; i++){
            transactionsToBlock.add(pendingTransactions.remove(0));
        }

        return transactionsToBlock;
    }


    public void gossipTransactionToOthers(Transaction t)
    {
        if (random.nextDouble() > GOSSIP_CHANCE)
        {
            ArrayList<KademliaNode> allNodes = k.rt.getAllNodes();
            for(KademliaNode n :allNodes)
            {
                try
                {
                    k.protocol.storeTransactionOp(t,n.getIpAdress(),n.getPort());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addFromMyAuction(Transaction t)
    {
        this.pendingTransactions.add(t);
        if (currentMiningBlock == null) {
            if (this.pendingTransactions.size() == TRANSACTIONS_LIMIT) {

                ArrayList<Transaction> transactionToBlock = this.maxLimitPendingTransactions(pendingTransactions);
                currentMiningBlock = new Block(this.latestMinedBlock.getHash(), transactionToBlock, this.latestMinedBlock);
                MineThread mt = new MineThread(this.currentMiningBlock, this.difficulty,this);
                this.miningBlock = new Thread(mt);
                miningBlock.start();
            }
        }
        gossipTransactionToAllOthers(t);
    }

    public void gossipTransactionToAllOthers(Transaction t)
    {
        ArrayList<KademliaNode> allNodes = k.rt.getAllNodes();
        for(KademliaNode n :allNodes)
        {
            try
            {
                k.protocol.storeTransactionOp(t,n.getIpAdress(),n.getPort());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    public LengthSumRep calculateChainLength(Block b, LengthSumRep lr)
    {
        if (b != null) {
            lr.length += 1;
            lr.sumRep += b.getReputation();
        }
        return lr;
    }
    public void gossipBlockToAllOthers(Block b)
    {
        ArrayList<KademliaNode> allNodes = k.rt.getAllNodes();
        for(KademliaNode n :allNodes)
        {
            try
            {
                k.protocol.storeBlockOp(n.getNodeId(),n.getIpAdress(),n.getPort(),b);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    }
    public void gossipBlockToOthers(Block b)
    {
        if (random.nextDouble() > GOSSIP_CHANCE)
        {
            gossipBlockToAllOthers(b);
        }
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
    public boolean isChainValid()
    {
        isChainValidAux(this.latestMinedBlock);
        return true; // All checks passed, the blockchain is valid
    }

    public boolean isChainValidAux(Block block)
    {
        if(!isBlockValid(block))
        {
            return false ;
        }
        return isChainValidAux(block.getPreviousBlock());
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

            byte[] signature = SignatureClass.sign(outputStream.toByteArray(), privateKey);
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
            if(!isTransactionValid(transaction, block))
            {
                this.k.rt.setReputation(block.getNode(),-1)      ;
                return false;
            }
        }

        this.k.rt.setReputation(block.getNode(),1);
        block.setReputation(1);
        return true;
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

    public static class LengthSumRep
    {
        public int length;
        public int sumRep;
    }
}

