package skademlia;

public class KademliaServer {
    Server server;
    
    KademliaServer(int port) {
        Server server = ServerBuilder.forPort(port).addService(new KademliaService()).build();
        
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            System.out.println("Server successfully shutdown!");
        }));
        
        server.awaitTermination();
    }
}
