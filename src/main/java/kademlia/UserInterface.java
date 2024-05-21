package kademlia;

import auctions.Auction;
import auctions.BrokerService;
import blockchain.Blockchain;

import java.security.NoSuchAlgorithmException;
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
            System.out.println("1) View auctions");
            System.out.println("2) Place bid");
            System.out.println("3) Create auction");
            System.out.println("4) View account");
            System.out.println("5) Exit");

            int option = Integer.parseInt(sc.nextLine());

            switch (option) {
                case 1:
                    viewAuctions();
                    break;
                case 2:
                    placeBid();
                    break;
                case 3:
                    createAuction();
                    break;
                case 4:
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
            System.out.println("1. Receive information about specific auction");
            System.out.println("2. Ongoing auctions");
            System.out.println("3. Closed auctions");
            System.out.println("4. Back to main menu");

            int option = Integer.parseInt(sc.nextLine());

            switch (option) {
                case 1:
                    System.out.print("Enter auction ID\n-> ");
                    String serviceId = sc.nextLine();
                    System.out.println("  - Current highest bid: " + auction.getPrice(serviceId.getBytes()));
                    //System.out.println("  - Auction status: " + auction.); // if it's open or closed
                    break;
                case 2:
                    if (auction.getServices().isEmpty()) {
                        System.out.println("No ongoing auctions");
                    }
                    for (BrokerService service : auction.getServices()) {
                        System.out.println(Arrays.toString(service.getServiceId()));
                    }
                    break;
                case 3:
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Error: invalid parameter");
                    break;
            }
        }
    }

    private static void placeBid() {
        System.out.println("============= PLACE BID SUB-MENU =============");
        System.out.print("Enter auction service ID\n-> ");
        String serviceID = sc.nextLine();
        System.out.print("Enter bid amount\n-> ");
        double bidAmount = Double.parseDouble(sc.nextLine());

        // TODO: if auction ID is valid call place bid function
        if (auction.hasService(serviceID.getBytes())) {
            // p.storeTransactionOp(new Transaction(sender, receiver, bidAmount, blockchain.Transaction.TransactionType.BID));
        }
        else {
            System.out.println("Error: Invalid auction ID");
        }
    }

    private static void createAuction() {
        System.out.println("========== CREATE AUCTION SUB-MENU ===========");

        System.out.print("Enter item\n-> ");
        String item = sc.nextLine();
        System.out.print("Enter starting price\n-> ");
        int startingPrice = Integer.parseInt(sc.nextLine());
        System.out.print("Enter auction duration\n-> ");
        int auctionDuration = Integer.parseInt(sc.nextLine());
        auction.createService(item,startingPrice,auctionDuration); // TODO: o que é o "a" do createService?
        System.out.println("Auction service created with ID: " + Arrays.toString(auction.getServiceId(item)));
    }

    public static void main(String[] args) {
        System.out.println("Initializing Kademlia...");
        k = new Kademlia(args[0], args[1], Integer.parseInt(args[2]));
        sc = new Scanner(System.in);
        auction = new Auction(k, bc);
        mainMenu();
    }
}
