package kademlia;

import auctions.Auction;
import auctions.BrokerService;
import blockchain.Blockchain;
import blockchain.Transaction;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static kademlia.Kademlia.rt;
import static kademlia.Kademlia.protocol;
import static kademlia.Kademlia.bc;

public class UserInterface {
    private static Kademlia k;
    private static Scanner sc;
    private static KademliaNode kn;
    private static Auction auction;

    private static void mainMenu() {
        while (true) {
            System.out.println("================== MAIN MENU =================");
            System.out.println("Select one of the following options:");
            System.out.println("1) Subscribe/unsubscribe to auctions or get information");
            System.out.println("2) Create auction");
            System.out.println("3) View account");
            System.out.println("4) Exit");

            int option = Integer.parseInt(sc.nextLine());

            switch (option) {
                case 1:
                    System.out.println("Pick the number of the auction from the previous ones ");
                    int auction = sc.nextInt();
                    viewAuctions();
                    break;
                case 2:
                    createAuction();
                    break;
                case 3:
                    //System.out.println("-> Balance: ");
                    System.out.println("-> Reputation: " + kn.getReputation());
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Error: invalid parameter");
                    break;
            }
        }
    }


    private static void viewAuctions() {
        while (true) {
            System.out.println("=========== VIEW AUCTIONS SUB-MENU ===========");
            System.out.println("Select one of the following options:");
            System.out.println("1. Subscribe to an auctionId");
            System.out.println("2. Unsubscribe to an auctionId");
            System.out.println("3. Get Information and bid ");
            System.out.println("4. Back to main menu");

            int option = Integer.parseInt(sc.nextLine());
            String service = "";
            switch (option) {
                case 1:
                    System.out.print("Enter name of service\n-> ");
                    service = sc.nextLine();
                    byte [] serviceId1 = bc.addSubscribe(service);
                    System.out.println("Sucessfully subscribed to serviceId " + k.rt.printId(serviceId1));
                    //System.out.println("  - Auction status: " + auction.); // if it's open or closed
                    break;
                case 2:
                    System.out.print("Enter name of service\n-> ");
                    service = sc.nextLine();
                    boolean succesful = bc.removeSubscribe(service);
                    if(succesful)
                    {
                        System.out.println("Sucessfully unsubscribed " );
                    }
                    else
                    {
                        System.out.println("Error : service not found" );
                    }
                    break;
                case 3 :
                    System.out.print("Enter name of service\n-> ");
                    service = sc.nextLine();
                    ArrayList<Transaction> latestInformationOnBlockChain= bc.getInformation(service);
                    showAuctionsInformation(latestInformationOnBlockChain);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Error: invalid parameter");
                    break;
            }
        }
    }

    private static void showAuctionsInformation(ArrayList<Transaction> latestInformationOnBlockChain)
    {
        System.out.println("============= Information about auctions =============");
        int i = 1;
        for(Transaction t : latestInformationOnBlockChain)
        {
            KademliaNode owner = t.getReceiver();

            System.out.println(i);
            // Print all owner attributes
            System.out.println("Owner - IP Address: " + owner.ipAdress);
            System.out.println("Owner - Port: " + owner.getPort());
            System.out.println("Owner - Reputation: " + KademliaNode.reputation);
            System.out.println("Price: " + t.getPrice());
            i++;
        }

            System.out.println("Please select an option:");
            System.out.println("1. Place a Bid");
            System.out.println("2. Go Back");
            System.out.println("3. Leave and Go to Main Menu");
            System.out.print("Enter your choice (1, 2, or 3): ");

            String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.println("Pick the number of the auction");
                int auction = sc.nextInt();
                Transaction t = latestInformationOnBlockChain.get(i-1);
                placeBid(t.getServiceID(),t.getReceiver());
                break;
            case "2":
                viewAuctions();
                break;
            case "3":
                mainMenu();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;

        }
        sc.nextLine();
    }

    private static void placeBid(byte[] serviceId, KademliaNode owner) {
        System.out.println("============= PLACE BID SUB-MENU =============");
        System.out.print("Enter bid amount\n-> ");
        float bidAmount = Float.parseFloat(sc.nextLine());

        //TODO : GET RECEIVER SEARCH RECEIVER WITH THE AUCTION THAT HAS THE MOST CHEAP AUCTION FOR THIS SERVICE  FALTA METODO
        //
        KademliaNode receiverNode = new KademliaNode("","".getBytes(),0, "".getBytes());

        boolean didTransactionGoThrough = false;

        try {
            didTransactionGoThrough = k.protocol.storeTransactionOp(new Transaction(owner, bidAmount, serviceId, Transaction.TransactionType.BID ));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (didTransactionGoThrough)
        {
            System.out.println("Bid done successfully");
        }
        else {
            System.out.println("Error: Invalid auction ID");
        }
    }

    private static void createAuction() {
        System.out.println("========== CREATE AUCTION SUB-MENU ===========");

        System.out.print("Enter item\n-> ");
        String item = sc.nextLine();
        System.out.print("Enter maximum time between bids\n-> ");
        int auctionDuration = Integer.parseInt(sc.nextLine());
        byte[] serviceId = auction.initiateService(item,auctionDuration);
        System.out.println("Auction service created with ID: " + Arrays.toString(serviceId));
    }

    public static void main(String[] args) {
        System.out.println("Initializing Kademlia...");
        k = new Kademlia(args[0], args[1], Integer.parseInt(args[2]));
        sc = new Scanner(System.in);
        auction = new Auction(k, bc);
        mainMenu();
    }
}
