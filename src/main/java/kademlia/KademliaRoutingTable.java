package kademlia;
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
    //Inicialização da classe
    public KademliaRoutingTable(byte[] nodeId, KademliaProtocol protocol, int k )
    {
        this.root = new TreeNode();
        this.root.createKBucket();
        // Colocar o proprio node this.insert(OWN NODE)
        this.myNodeId = nodeId;
        this.k = k;
        this.protocol = protocol;
    }

    //  Função que insere um no na arvore
    //  Função que insere um no na arvore
    public boolean insert(KademliaNode node)
    {
        return false;
    }

    public boolean hasObject(SortedArrayList<KademliaNode> kbucket,KademliaNode node)
    {
        for (KademliaNode n : kbucket)
        {
            if (n.nodeId.equals(node.nodeId))
            {
                return true;
            }
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

    // TOdo testar
    // Função que testa se o no visto pela ultima vez online ainda esta online e neste caso descarta a variavel 'node' caso contrario
    //Remove o no que foi visto pela ultima vez online e adiciona a variavel 'node' a o kbucket
    public boolean testLeastRecentlySeen(SortedArrayList<KademliaNode> kbucket, KademliaNode node)
    {
        int tamanho = kbucket.size();
        // Função que ira retornar o node ultimo visto no kbucket
        KademliaNode testPing = kbucket.get(tamanho-1);
        //boolean notActive =  this.protocol.ping(testPing,this.myNodeId)
        //TODO Ajeitar isso quando o protocol.ping tiver funcionando
        boolean notActive = true;
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

    // TODO testar
    // Função para achar o node mais perto da variavel 'nodeId'
    public ArrayList<KademliaNode> findClosestNode(byte[] nodeId, int j, int a)
    {
        ArrayList<KademliaNode> nodos;
        lock.lock();
        // Testa se tem um kbucket
        if (this.root.kc >= 2) {
            // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
            nodos = searchMapClosest(this.root.kbucket, nodeId, a);
        }
        else if (this.root.kc == 1)
        {
            nodos = null;
        }
        else
        {
            boolean direction = (((myNodeId[0] >> 7) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

            // Caso contrario continua percorrendo a arvore e chamando a função recursiva
            if (direction)
            {
                nodos =findClosestNodeRec(this.root.right, this.root, nodeId, 0,6,'d',a);

            }
            else
            {
                nodos = findClosestNodeRec(this.root.left, this.root, nodeId, 0,6,'e',a);
            }
        }// Chama a função recursiva para resolver o problema
        lock.unlock();
        return nodos;
    }

    private void getNodesDown(TreeNode curr,int a,byte[] nodeId ,ArrayList<KademliaNode> nodes)
    {
        if (curr != null)
        {
            if (curr.kc >= 1)
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.kbucket, nodeId,a-nodes.size());
                nodes.addAll(nodos);
            }
            else
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.left.kbucket, nodeId,a-nodes.size());
                nodes.addAll(nodos);
                if (nodes.size() +1 < a)
                {
                    getNodesDown(curr.right,a,nodeId, nodes);
                }
            }
        }
    }

    private void getNodesUp(TreeNode curr,int a,byte[] nodeId ,ArrayList<KademliaNode> nodes)
    {
        if (curr != null)
        {
            if (curr.kc >= 1)
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.kbucket, nodeId,a-nodes.size());
                nodes.addAll(nodos);
            }
            else
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.left.kbucket, nodeId,a-nodes.size());
                nodes.addAll(nodos);
                if (nodes.size() +1 < a)
                {
                    getNodesDown(curr.right,a,nodeId, nodes);
                }
            }
        }
    }
    //TODO Testar
    private ArrayList<KademliaNode> findClosestNodeRec(TreeNode curr, TreeNode parent, byte[] nodeId, int i, int j,char d, int a)
    {
        ArrayList<KademliaNode> nodes = null;
        if (i < 20)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 1)
            {
                System.out.println("Procurando num Map");
                nodes = searchMapClosest(curr.kbucket, nodeId,a);
                if (nodes.size() +1 < a)
                {
                    getNodesDown(parent.right,a,nodeId , nodes);
                }

            }
            else
            {
                boolean direction = (((myNodeId[i] >> j ) & 1) == 1) == (((nodeId[i] >> j) & 1) == 1);

                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    if (j == 0)
                    {
                        nodes = findClosestNodeRec(curr.right,curr, nodeId, i+1,7,'d',a);

                    }
                    else
                    {
                        nodes = findClosestNodeRec(curr.right,curr,nodeId, i,j-1,'d',a);


                    }

                }
                else
                {
                    if (j == 0)
                    {
                        nodes = findClosestNodeRec(curr.left,curr, nodeId, i+1,7,'e',a);

                    }
                    else
                    {
                        nodes = findClosestNodeRec(curr.left,curr,nodeId, i,j-1,'e',a);


                    }
                }
            }
        }
        if (nodes.size() +1 < a)
        {
            ArrayList<KademliaNode> nodos ;
            if (d == 'e')
            {
                nodos = searchMapClosest(parent.right.kbucket, nodeId,a-nodes.size());
            }
            else
            {
                nodos = searchMapClosest(parent.left.kbucket, nodeId,a-nodes.size());

            }
            nodes.addAll(nodos);

        }
        return nodes;
    }


    // TODO testar
    // Função que pesquisa o map pelo node mais perto da variavel 'nodeId'
    private ArrayList<KademliaNode> searchMapClosest(ArrayList<KademliaNode> kbucket,byte[] nodeId, int a)
    {
        ArrayList<Tuple> sortDist = new ArrayList<Tuple>();
        ArrayList<KademliaNode> result  = new ArrayList<KademliaNode>();
        // Iniciamos a distancia por 1
        // Iniciamos uma variavel para guardar o no mais perto para retorrmos
        // Percorremos a lista procurando a distancia mais perto
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
        for (int j = 0; j < kbucket.size() || j < a; j++ )
        {
            result.add(sortDist.get(j).kd);
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
    private BigInteger calculateDistance (byte[] node1, byte[] node2)
    {
        BigInteger distance = new BigInteger("0");
        for (int i = 0; i < 20; i++)
        {
            for (int j = 7 ; j >= 0 ; j--)
            {
                boolean bit1 = ((node1[i] >> j) & 1) == 1;
                boolean bit2 = ((node2[i] >> j) & 1) == 1;

                // Testa se o no
                if (bit1 != bit2)
                {
                    //TODO Ver se é assim mesmo
                    distance = distance.add(BigInteger.valueOf((long) Math.pow(2, 160 - (i * (j)))));
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
            System.out.println("Direction " + dir+ "And depth " + depth+ " Kbucket with size "+ (node.kc-1));
            printTreeRec(dir+" r",node.right, depth + 1);
        }
        else
        {
            // Recursively print left and right subtrees
            printTreeRec(dir+" l",node.left, depth + 1);
            printTreeRec(dir+" r",node.right, depth + 1);
        }

    }

    public void compareId(byte [] nodeId, byte [] nodeId2)
    {
        String nodeString = "";
        for (int j = 0 ; j < 20; j++)
        {
            byte b = nodeId[j];
            byte c = nodeId2[j];
            for (int i = 7; i >= 0; i--) { // Start from the most significant bit (bit 7)
                // Extract the i-th bit using bitwise AND operation
                boolean bit1 = ((b >> i) & 1) == 1;
                boolean bit2 = ((c >> i) & 1) == 1;

                // Print the bit value
                if (bit1 == bit2)
                {
                    nodeString = nodeString+"d";
                }
                else
                {
                    nodeString = nodeString +"e";
                }
            }
        }

        System.out.println(nodeString);
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
    public static void main(String[] args)
    {

        // Convert array of bits to bytes
        Kademlia kd = new Kademlia();
        KademliaRoutingTable krt = new KademliaRoutingTable(kd.generateNodeId(), kd.getKdProtocol(), 20 );;
        //System.out.println(krt.findClosestKbucket(krt.root,krt.root, kd.generateNodeId(),0,7,'r'));
        /*

        for (int i  = 0 ; i < 20000; i++)
        {
            krt.insert(new KademliaNode("localhost",kd.generateNodeId(),5000));
        }
        krt.printTree();
        byte p1[] = kd.generateNodeId();
        byte p2[] = kd.generateNodeId();

        System.out.println(krt.printId(p1));
        System.out.println(krt.printId(p2));
        krt.compareId(p1,p2);


         */
    }
}

