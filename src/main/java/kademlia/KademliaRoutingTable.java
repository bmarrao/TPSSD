package kademlia;
import com.google.protobuf.ByteString;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Classe do no da arvore

class SortedArrayList<T> extends ArrayList<T>
{
    private Comparator<T> comparator;

    public SortedArrayList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T element) {
        boolean result = super.add(element);
        Collections.sort(this, comparator);
        return result;
    }
}
class TreeNode
{
    //Variavel que guarda o kbucket caso exista , caso contrario tera o valor de null
    public SortedArrayList<KademliaNode> kbucket;
    //Variavel que diz se o no tem um kbucket, caso o valor seja zero não tem kbucket , caso contrario tem
    public int kc;
    //Continuação da arvore 
    public TreeNode left, right;

    // Inicialização da classe
    public TreeNode()
    {
        left = right = null;
        this.kc = 0;
    }
    //Criação de KBucket
    public void createKBucket()
    {
        this.kbucket = new SortedArrayList<>(Comparator.comparing(KademliaNode::getDateTime).reversed());
        this.kc = 1;
    }

}

public class KademliaRoutingTable
{

    public Lock lock = new ReentrantLock();
    public KademliaProtocol protocol;
    // Raiz da arvore
    TreeNode root;
    // Id do node ao qual pertence a arvore
    byte[] myNodeId;
    // Tamanho dos buckets
    int k;
    // S/Kademlia sibling list
    TreeMap<BigInteger, Node> siblingList;
    // Sibling list size
    int s;

    //Inicialização da classe
    public KademliaRoutingTable(byte[] nodeId, KademliaProtocol protocol, int k, int s )
    {
        this.root = new TreeNode();
        this.root.createKBucket();
        // Colocar o proprio node this.insert(OWN NODE)
        this.myNodeId = nodeId;
        this.k = k;
        this.protocol = protocol;
        this.siblingList = new TreeMap<>();
        this.s = s;
    }



    // insert node to routing table considering sibling list
    // TODO: não percebo como se decide se se usa a sibling list ou o método do last recently seen online ???
    //       se calhar quando as distâncias entre o furthest e o atual forem iguais ???
    public void slInsertNode(Node n) {
        // calcula distância do nó n ao mynodeid
        BigInteger newNodeDistance = calculateDistance(myNodeId, n.getId().toByteArray());

        if (siblingList.size() >= s) {
            // se essa distância for mais proxima do que a distância do furthest node na sibling list
            // entao adiciona o nó n à sibling list
            BigInteger furthestNodeDistance = calculateDistance(myNodeId, siblingList.lastEntry().getValue().getId().toByteArray());
            int comparisonResult = newNodeDistance.compareTo(furthestNodeDistance);

            if (comparisonResult < 0) {
                siblingList.remove(siblingList.lastEntry().getKey());
                siblingList.put(newNodeDistance, n);
            }
        }
        else {
            siblingList.put(newNodeDistance, n);
        }
    }


    // o metodo addNodes deve verificar se os ids contidos já estao na routing table
    // retorna a lista de nós mais próximos que ainda não estavam na rt (para depois os contactar)
    public ArrayList<Node> addNodes(List<Node> nodesList) {
        ArrayList<Node> newAddedNodes = new ArrayList<>();

        for (Node n : nodesList) {
            // if routing table doesn't have n then
            // if (!hasObject(root.kbucket, n))
                // add to rt (idk how) -
                // addToBuckets(root.left, root.right, root.kbucket, n, 0, 0);
                // newAddedNodes.add(n);
        }
        return newAddedNodes;
    }


    //  Função que insere um no na arvore
    public boolean insert(Node node)
    {
        return false;
    }

    public boolean hasObject(SortedArrayList<KademliaNode> kbucket,KademliaNode node)
    {
        byte[] id1 = node.nodeId;
        byte[] id2;
        boolean isEqual;
        for (KademliaNode n : kbucket)
        {

                id2 = n.nodeId;
                isEqual = true;
                // Iterate through each byte and compare them
                for (int i = 0; i < id1.length; i++)
                {
                    if (id1[i] != id2[i])
                    {
                        isEqual = false;
                    }
                }

                if (isEqual)
                {
                    n.setTime();
                    return true;

                }
                // If all bytes are the same, return true
        }
        return false;
    }

    // Função recursiva

    // TOdo testar
    // Adiciona os nodes que estão na variavel kbucket para os novos buckets criados de acordo com a distancia
    // em relação ao id do Node ao qual pertence a routing table
    public void addToBuckets(TreeNode left, TreeNode right, ArrayList<KademliaNode> kbucket,KademliaNode node, int i ,int j)
    {
        // Conta quantos nos estão indo na direção a direita
        int count_right = 0;
        // Percorre a lista dos nos no bucket
        for (KademliaNode BNode: kbucket)
        {

            boolean bit1 = ((myNodeId[i] >> j) & 1) == 1;
            boolean bit2 = ((BNode.nodeId[i] >> j) & 1) == 1;

            // Testa se o no
            if (bit1 == bit2)
            {
                right.kc++;
                right.kbucket.add(BNode);
                count_right++;
            }
            else
            {
                left.kc++;
                left.kbucket.add(BNode);
            }
        }
        // Testa se todos os nos estão indo para uma direção ou outra pois neste caso teremos que adicionar o novo no por um outro metodo
        boolean direction = (((myNodeId[i] >> j) & 1) == 1) == (((node.nodeId[i] >> j) & 1) == 1);
        if (count_right == this.k)
        {
            if (direction)
            {
                testLeastRecentlySeen(right.kbucket,node);
            }
            else
            {
                left.kc++;
                left.kbucket.add(node);
            }
        }
        else if (count_right == 0)
        {
            if (direction)
            {
                right.kc++;
                right.kbucket.add(node);
            }
            else
            {
                testLeastRecentlySeen(left.kbucket,node);
            }
        }
        // Se nenhum dos buckets esta cheio podemos so adicionar na direção correta
        else
        {
            if(direction)
            {
                left.kc++;
                left.kbucket.add( node);
            }
            else
            {
                right.kc++;
                right.kbucket.add( node);
            }
        }
    }

    public ArrayList<Node> findClosestNode(byte[] nodeId,int a)
    {
        ArrayList<KademliaNode> nodos;
        lock.lock();
        // Testa se tem um kbucket
        if (this.root.kc >= 2) {
            // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
            nodos = searchMapClosest(this.root.kbucket, nodeId, a);
            System.out.println("Adding at root" + nodos.size());
        }
        else if (this.root.kc == 1)
        {
            nodos = new ArrayList<KademliaNode>();
        }
        else
        {
            boolean direction = (((myNodeId[0] >> 7) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);
            nodos = testDirection(direction,this.root,nodeId,0,7,a,"");
            if (nodos.size() +1 < a)
            {
                ArrayList<KademliaNode> nodes ;
                nodes = testDirection(!direction,this.root,nodeId,0,7,a-nodos.size(),"");
                nodos.addAll(nodes);
            }
        }// Chama a função recursiva para resolver o problema
        lock.unlock();
        ArrayList<Node> ret = new ArrayList<Node>();
        for (KademliaNode kn : nodos )
        {
            Node.Builder nd = Node.newBuilder();
            nd.setIp(kn.ipAdress);
            nd.setPort(kn.port);
            nd.setId(ByteString.copyFrom(kn.nodeId));
            ret.add(nd.build());
        }
        return ret;
    }

    private ArrayList<KademliaNode> findClosestNodeRec(TreeNode curr, byte[] nodeId, int i, int j,int a, String path)
    {
        ArrayList<KademliaNode> nodes = new ArrayList<KademliaNode>();
        if (curr != null)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 1)
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.kbucket, nodeId,a);
                System.out.println("Adding in " + path + " " + nodos.size());
                return nodos;

            }
            else
            {
                boolean direction = (((myNodeId[i] >> j ) & 1) == 1) == (((nodeId[i] >> j) & 1) == 1);
                nodes = testDirection(direction,curr,nodeId,i,j,a ,path);

                if (nodes.size() +1 < a)
                {
                    ArrayList<KademliaNode> nodos ;
                    nodos = testDirection(!direction,curr,nodeId,i,j,a-nodes.size(),path);
                    nodes.addAll(nodos);
                }

            }
        }
        return nodes;
    }

    private ArrayList<KademliaNode> testDirection (boolean direction, TreeNode curr, byte[] nodeId, int i, int j, int a, String path)
    {
        if (direction)
        {
            if (j == 0)
            {
                return findClosestNodeRec(curr.right, nodeId, i+1,7,a,path+" r");

            }
            else
            {
                return findClosestNodeRec(curr.right,nodeId, i,j-1,a,path+" r");


            }

        }
        else
        {
            if (j == 0)
            {
                return findClosestNodeRec(curr.left,nodeId, i+1,7,a,path+" l");

            }
            else
            {
                return  findClosestNodeRec(curr.left,nodeId, i,j-1,a,path+" l");


            }
        }
    }

    // TOdo testar
    // Função que testa se o no visto pela ultima vez online ainda esta online e neste caso descarta a variavel 'node' caso contrario
    //Remove o no que foi visto pela ultima vez online e adiciona a variavel 'node' a o kbucket
    public boolean testLeastRecentlySeen(SortedArrayList<KademliaNode> kbucket, KademliaNode node)
    {
        int tamanho = kbucket.size();
        // Função que ira retornar o node ultimo visto no kbucket
        KademliaNode testPing = kbucket.get(tamanho-1);
        //boolean notActive =  this.protocol.pingOp(testPing.nodeId,testPing.ipAdress,testPing.port);
        boolean notActive = true;
        //TODO Ajeitar isso quando o protocol.ping tiver funcionando
        // Ping least recently active node
        if (!notActive)
        {
            kbucket.remove(tamanho-1);
            kbucket.add(node);
            return true;
        }
        else
        {
            testPing.setTime();
            return false;
        }
    }

    // Função que pesquisa o map pelo node mais perto da variavel 'nodeId'
    public ArrayList<KademliaNode> searchMapClosest(ArrayList<KademliaNode> kbucket,byte[] nodeId, int a)
    {
        ArrayList<KademliaNode> result  = new ArrayList<KademliaNode>();
        // Iniciamos a distancia por 1
        // Iniciamos uma variavel para guardar o no mais perto para retorrmos
        // Percorremos a lista procurando a distancia mais perto
        if (kbucket.size() != 0)
        {
            ArrayList<Tuple> sortDist = new ArrayList<Tuple>();
            KademliaNode node = kbucket.get(0);
            BigInteger distance = calculateDistance(nodeId, node.nodeId);
            for (int i = 1 ; i <kbucket.size();i++)
            {
                KademliaNode bnode = kbucket.get(i);
                // Calcula a distancia relativamente ao no 'bnode'
                // Caso seja menor guardamos como o node mais perto e a menor distancia
                BigInteger newDistance = calculateDistance(nodeId, bnode.nodeId);
                // Caso seja menor guardamos como o node mais perto e a menor distancia
                sortDist.add(new Tuple(bnode, newDistance));
                Collections.sort(sortDist);
            }
            int tamanho = sortDist.size();
            if (tamanho > a)
            {
                tamanho = a;
            }

            for (int j = 0;  j < tamanho ; j++ )
            {
                result.add(sortDist.get(j).kd);
            }
        }

        return result;
    }

    public class Tuple implements Comparable<Tuple>
    {
        public KademliaNode kd ;
        public BigInteger dist ;

        public Tuple (KademliaNode kd, BigInteger dist)
        {
            this.kd = kd;
            this.dist = dist;
        }
        @Override
        public int compareTo(Tuple other) {
            return this.dist.compareTo(other.dist);
        }
    }
    // TODO testar se isso funciona direito
    // Função que calcula a distancia de um no
    public BigInteger calculateDistance (byte[] node1, byte[] node2)
    {
        BigInteger distance = BigInteger.ZERO;
        for (int i = 0; i < node1.length; i++)
        {
            for (int j = 7 ; j >= 0 ; j--)
            {
                boolean bit1 = ((node1[i] >> j) & 1) == 1;
                boolean bit2 = ((node2[i] >> j) & 1) == 1;

                // Testa se o no
                if (bit1 != bit2)
                {
                    //TODO Ver se é assim mesmo
                    distance = distance.add(BigInteger.valueOf((long) Math.pow(2,(((node1.length-i-1)*8)+j))));
                }
            }
        }
        return distance;
    }
    // Função que calcula o node visto pela ultima vez online em um kbucket


    public void printTree()
    {
        printTreeRec("Root",this.root,0);
    }

    private static void printTreeRec(String dir,TreeNode node, int depth)
    {

        if (node == null) {
            return;
        }

        if (node.kc >= 1)
        {
            System.out.println("Direction " + dir+ "And depth " + depth+ " Kbucket with size "+ (node.kc));
            printTreeRec(dir+" r",node.right, depth + 1);
        }
        else
        {
            // Recursively print left and right subtrees
            printTreeRec(dir+" l",node.left, depth + 1);
            printTreeRec(dir+" r",node.right, depth + 1);
        }

    }

    public boolean CheckNodeIsInTree(byte[] nodeId, TreeNode node,String path)
    {
        if (node.kbucket != null)
        {
            if (hasObject(node.kbucket,new KademliaNode("",nodeId,421)))
            {
                System.out.println("path " + path);
                return true;
            }
            return false;
        }
        else
        {

            if (CheckNodeIsInTree(nodeId,node.left,path + " l"))
            {
                return true;
            }
            return CheckNodeIsInTree(nodeId,node.right,path + " r");

        }

    }

    public String printId(byte [] id)
    {
        String nodeId="";
        for (int j = 0 ; j < 20; j++)
        {
            byte b = id[j];
            for (int i = 7; i >= 0; i--) { // Start from the most significant bit (bit 7)
                // Extract the i-th bit using bitwise AND operation
                boolean bit = ((b >> i) & 1) == 1;
                // Print the bit value
                if (bit)
                {
                    nodeId = nodeId+"1";
                }
                else
                {
                    nodeId = nodeId +"0";
                }
            }
        }
        return nodeId;
    }
}

