package kademlia;

import java.util.HashMap;
import java.util.Map;

public class KademliaStore
{
    // TODO desenvolver classe - Hugo
    // Id do que queremos guardar - Key, Object - o que foi pedido para guardar
    // BLOCKCHAIN
    // Informação referente a auction
    Map<String,Object> storage ;
    KademliaStore()
    {
        this.storage = new HashMap<String, Object>();
    }
}
