package kademlia;

import auctions.Auction;
import blockchain.Blockchain;
import java.util.Scanner;

public class UserInterface {
    private Kademlia k;
    private Blockchain bc;
    private KademliaProtocol p;
    private Scanner sc;
    private KademliaNode kn;
    private Auction auction;

    UserInterface(Kademlia k, KademliaProtocol p, Blockchain bc, KademliaNode kn) {
        this.k = k;
        this.bc = bc;
        this.p = p;
        this.sc = new Scanner(System.in);
        auction = null;
        mainMenu();
    }

    private void mainMenu() {
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


    private void viewAuctions() {
        while (true) {
            System.out.println("=========== VIEW AUCTIONS SUB-MENU ===========");
            System.out.println("Select one of the following options:");
            System.out.println("1. Ongoing auctions");
            System.out.println("2. Closed auctions");
            System.out.println("3. Back to main menu");

            int option = Integer.parseInt(sc.nextLine());

            switch (option) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Error: invalid parameter");
                    break;
            }
        }
    }

    private void placeBid() {
        System.out.println("============= PLACE BID SUB-MENU =============");
        System.out.print("Enter auction ID\n-> ");
        String auctionID = sc.nextLine();
        System.out.print("Enter bid amount\n-> ");
        int bidAmount = Integer.parseInt(sc.nextLine());
        // TODO: call place bid function
        //storeTransactionOp();
    }

    private void createAuction() {
        System.out.println("========== CREATE AUCTION SUB-MENU ===========");

        System.out.print("Enter item\n-> ");
        String item = sc.nextLine();
        System.out.print("Enter starting price\n-> ");
        int startingPrice = Integer.parseInt(sc.nextLine());
        System.out.print("Enter auction duration\n-> ");
        auction = new Auction(k, bc);
    }
}
