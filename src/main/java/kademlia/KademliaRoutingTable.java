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

    private Lock lock = new ReentrantLock();
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
    public void insert(KademliaNode node)
    {
        lock.lock();
        TreeNode curr = root;
        System.out.println("My node = " + this.printId(this.myNodeId));
        System.out.println("Other Node = " + this.printId(node.nodeId));
        System.out.print("path = ");
        // Função recursiva que ira percorrer a arvore
        insertRec(node, curr,0, 7, 'd');
        lock.unlock();
    }

    // Função recursiva
    public void insertRec(KademliaNode node, TreeNode curr,int i,int j, char prevDir )
    {
        if (i < 20)
        {
            // Testa se tem um kbucket no node curr
            if (curr.kc >= 1)
            {
                System.out.println("Kbucket has size of " + curr.kc);
                // Testa se o node curr esta na capacidade maxima
                if (curr.kbucket.size() >= this.k)
                {
                    // Testa se ele vem da direção que tem uma distancia mais perto do no
                    if (prevDir =='d')
                    {
                        //Como iremos expandir a arvore e criar dois novos buckets
                        //Marcamos o no atual como não tendo kbucket
                        curr.kc = 0;
                        // Criamos um no novo a esquerda e a direita cada um deles com um kbucket
                        curr.left = new TreeNode();
                        curr.left.createKBucket();
                        curr.right = new TreeNode();
                        curr.right.createKBucket();
                        // Agora vamos popular os novos buckets que criamos com os nos que estavam no anterios e adicionar o novo
                        if (j == 0)
                        {
                            this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i+1,7);

                        }
                        else
                        {
                            this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i,j-1);
                        }
                        // Depois disso marcamos o kbucket do no atual como null
                        curr.kbucket = null;
                        System.out.println("New Kbucket has size of " + curr.left.kc + "And " + curr.right.kc);
                    }
                    else
                    {
                        // Caso ele não venha da direção que está mais perto do proprio id e o bucket esta cheio ele tenta inserir no kbucket q ja existe
                        testLeastRecentlySeen(curr.kbucket, node);
                    }
                }
                else
                {
                    //Adiciona o no a o kbucket
                    curr.kc++;
                    curr.kbucket.add(node);
                }
            }
            else
            {

                boolean direction = (((this.myNodeId[i]>> j ) & 1) == 1) == (((node.nodeId[i] >> j) & 1) == 1);

                // Testa se o no
                if (direction)
                {
                    System.out.print('d');
                    if (j == 0)
                    {
                        insertRec(node, curr.right,i+1,7,'d');
                    }
                    else
                    {
                        insertRec(node, curr.right,i,j-1,'d');

                    }
                }
                else
                {
                    System.out.print('e');
                    if (j == 0)
                    {
                        insertRec(node, curr.left,i+1,7,'e');
                    }
                    else
                    {
                        insertRec(node, curr.left,i,j-1,'e');

                    }
                }
            }
        }
    }

    // Função recursiva

    // TOdo testar
    // Adiciona os nodes que estão na variavel kbucket para os novos buckets criados de acordo com a distancia
    // em relação ao id do Node ao qual pertence a routing table
    private void addToBuckets(TreeNode left, TreeNode right, ArrayList<KademliaNode> kbucket,KademliaNode node, int i ,int j)
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
    private void testLeastRecentlySeen(SortedArrayList<KademliaNode> kbucket, KademliaNode node)
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
        }
        else
        {
            testPing.setTime();
        }
    }

    // TODO testar
    // Função para achar o node mais perto da variavel 'nodeId'
    private KademliaNode findClosestNode(byte[] nodeId)
    {
        KademliaNode nodo;
        lock.lock();
        // Testa se tem um kbucket
        if (this.root.kc >= 2) {
            // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
            nodo = searchMapClosest(this.root.kbucket, nodeId);
        }
        else if (this.root.kc == 1)
        {
            nodo = null;
        }
        else
        {
            boolean direction = (((myNodeId[0] >> 7) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

            // Caso contrario continua percorrendo a arvore e chamando a função recursiva
            if (direction)
            {
                nodo =findClosestNodeRec(this.root.right, this.root, nodeId, 0,6,'d');

            }
            else
            {
                nodo = findClosestNodeRec(this.root.left, this.root, nodeId, 0,6,'e');
            }
        }// Chama a função recursiva para resolver o problema
        lock.unlock();
        return nodo;
    }

    //TODO Testar
    private KademliaNode findClosestNodeRec(TreeNode curr, TreeNode parent, byte[] nodeId, int i, int j,char d)
    {
        if (i < 20)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 2)
            {
                System.out.println("Procurando num Map");
                // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
                return searchMapClosest(curr.kbucket, nodeId);
            }
            else if (curr.kc == 1)
            {
                if(d == 'd')
                {
                    return findClosestNodeRec(parent.left,parent, nodeId, i,j,'e');
                }
                else
                {
                    return findClosestNodeRec(parent.right,parent, nodeId, i,j,'e');
                }
            }
            else
            {
                boolean direction = (((myNodeId[i] >> 7 ) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    if (j == 0)
                    {
                        return findClosestNodeRec(curr.right,curr, nodeId, i+1,7,'d');

                    }
                    else
                    {
                        return findClosestNodeRec(curr.right,curr,nodeId, i,j-1,'d');


                    }

                }
                else
                {
                    if (j == 0)
                    {
                        return findClosestNodeRec(curr.left,curr, nodeId, i+1,7,'e');

                    }
                    else
                    {
                        return findClosestNodeRec(curr.left,curr,nodeId, i,j-1,'e');


                    }
                }
            }
        }
        return null;
    }


    // TODO testar
    // Função que pesquisa o map pelo node mais perto da variavel 'nodeId'
    private KademliaNode searchMapClosest(ArrayList<KademliaNode> kbucket,byte[] nodeId)
    {
        // Iniciamos a distancia por 1
        // Iniciamos uma variavel para guardar o no mais perto para retorrmos
        // Percorremos a lista procurando a distancia mais perto
        System.out.println(kbucket);
        KademliaNode node = kbucket.get(0);
        BigInteger distance = calculateDistance(nodeId, node.nodeId);
        for (int i = 1 ; i <kbucket.size();i++)
        {
            KademliaNode bnode = kbucket.get(i);
            // Calcula a distancia relativamente ao no 'bnode'
            // Caso seja menor guardamos como o node mais perto e a menor distancia
            BigInteger newDistance = calculateDistance(nodeId, bnode.nodeId);
            System.out.println(newDistance.compareTo(distance));
            // Caso seja menor guardamos como o node mais perto e a menor distancia
            if (newDistance.compareTo(distance) >= 0) {
                distance = newDistance;
                node = bnode;
            }
        }
        return node;
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

        for (int i  = 0 ; i < 20000; i++)
        {
            krt.insert(new KademliaNode("localhost",kd.generateNodeId(),5000));
        }
        krt.printTree();
        /*
        byte p1[] = kd.generateNodeId();
        byte p2[] = kd.generateNodeId();

        System.out.println(krt.printId(p1));
        System.out.println(krt.printId(p2));
        krt.compareId(p1,p2);


         */
    }
}

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

    private Lock lock = new ReentrantLock();
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
    public void insert(KademliaNode node)
    {
        lock.lock();
        TreeNode curr = root;
        System.out.println("My node = " + this.printId(this.myNodeId));
        System.out.println("Other Node = " + this.printId(node.nodeId));
        System.out.print("path = ");
        // Função recursiva que ira percorrer a arvore
        insertRec(node, curr,0, 7, 'd');
        lock.unlock();
    }

    // Função recursiva
    public void insertRec(KademliaNode node, TreeNode curr,int i,int j, char prevDir )
    {
        if (i < 20)
        {
            // Testa se tem um kbucket no node curr
            if (curr.kc >= 1)
            {
                System.out.println("Kbucket has size of " + curr.kc);
                // Testa se o node curr esta na capacidade maxima
                if (curr.kbucket.size() >= this.k)
                {
                    // Testa se ele vem da direção que tem uma distancia mais perto do no
                    if (prevDir =='d')
                    {
                        //Como iremos expandir a arvore e criar dois novos buckets
                        //Marcamos o no atual como não tendo kbucket
                        curr.kc = 0;
                        // Criamos um no novo a esquerda e a direita cada um deles com um kbucket
                        curr.left = new TreeNode();
                        curr.left.createKBucket();
                        curr.right = new TreeNode();
                        curr.right.createKBucket();
                        // Agora vamos popular os novos buckets que criamos com os nos que estavam no anterios e adicionar o novo
                        if (j == 0)
                        {
                            this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i+1,7);

                        }
                        else
                        {
                            this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i,j-1);
                        }
                        // Depois disso marcamos o kbucket do no atual como null
                        curr.kbucket = null;
                        System.out.println("New Kbucket has size of " + curr.left.kc + "And " + curr.right.kc);
                    }
                    else
                    {
                        // Caso ele não venha da direção que está mais perto do proprio id e o bucket esta cheio ele tenta inserir no kbucket q ja existe
                        testLeastRecentlySeen(curr.kbucket, node);
                    }
                }
                else
                {
                    //Adiciona o no a o kbucket
                    curr.kc++;
                    curr.kbucket.add(node);
                }
            }
            else
            {

                boolean direction = (((this.myNodeId[i]>> j ) & 1) == 1) == (((node.nodeId[i] >> j) & 1) == 1);

                // Testa se o no
                if (direction)
                {
                    System.out.print('d');
                    if (j == 0)
                    {
                        insertRec(node, curr.right,i+1,7,'d');
                    }
                    else
                    {
                        insertRec(node, curr.right,i,j-1,'d');

                    }
                }
                else
                {
                    System.out.print('e');
                    if (j == 0)
                    {
                        insertRec(node, curr.left,i+1,7,'e');
                    }
                    else
                    {
                        insertRec(node, curr.left,i,j-1,'e');

                    }
                }
            }
        }
    }

    // Função recursiva

    // TOdo testar
    // Adiciona os nodes que estão na variavel kbucket para os novos buckets criados de acordo com a distancia
    // em relação ao id do Node ao qual pertence a routing table
    private void addToBuckets(TreeNode left, TreeNode right, ArrayList<KademliaNode> kbucket,KademliaNode node, int i ,int j)
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
    private void testLeastRecentlySeen(SortedArrayList<KademliaNode> kbucket, KademliaNode node)
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
        }
        else
        {
            testPing.setTime();
        }
    }

    // TODO testar
    // Função para achar o node mais perto da variavel 'nodeId'
    private KademliaNode findClosestNode(byte[] nodeId)
    {
        KademliaNode nodo;
        lock.lock();
        // Testa se tem um kbucket
        if (this.root.kc >= 2) {
            // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
            nodo = searchMapClosest(this.root.kbucket, nodeId);
        }
        else if (this.root.kc == 1)
        {
            nodo = null;
        }
        else
        {
            boolean direction = (((myNodeId[0] >> 7) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

            // Caso contrario continua percorrendo a arvore e chamando a função recursiva
            if (direction)
            {
                nodo =findClosestNodeRec(this.root.right, this.root, nodeId, 0,6,'d');

            }
            else
            {
                nodo = findClosestNodeRec(this.root.left, this.root, nodeId, 0,6,'e');
            }
        }// Chama a função recursiva para resolver o problema
        lock.unlock();
        return nodo;
    }

    //TODO Testar
    private KademliaNode findClosestNodeRec(TreeNode curr, TreeNode parent, byte[] nodeId, int i, int j,char d)
    {
        if (i < 20)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 2)
            {
                System.out.println("Procurando num Map");
                // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
                return searchMapClosest(curr.kbucket, nodeId);
            }
            else if (curr.kc == 1)
            {
                if(d == 'd')
                {
                    return findClosestNodeRec(parent.left,parent, nodeId, i,j,'e');
                }
                else
                {
                    return findClosestNodeRec(parent.right,parent, nodeId, i,j,'e');
                }
            }
            else
            {
                boolean direction = (((myNodeId[i] >> 7 ) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    if (j == 0)
                    {
                        return findClosestNodeRec(curr.right,curr, nodeId, i+1,7,'d');

                    }
                    else
                    {
                        return findClosestNodeRec(curr.right,curr,nodeId, i,j-1,'d');


                    }

                }
                else
                {
                    if (j == 0)
                    {
                        return findClosestNodeRec(curr.left,curr, nodeId, i+1,7,'e');

                    }
                    else
                    {
                        return findClosestNodeRec(curr.left,curr,nodeId, i,j-1,'e');


                    }
                }
            }
        }
        return null;
    }


    // TODO testar
    // Função que pesquisa o map pelo node mais perto da variavel 'nodeId'
    private KademliaNode searchMapClosest(ArrayList<KademliaNode> kbucket,byte[] nodeId)
    {
        // Iniciamos a distancia por 1
        // Iniciamos uma variavel para guardar o no mais perto para retorrmos
        // Percorremos a lista procurando a distancia mais perto
        System.out.println(kbucket);
        KademliaNode node = kbucket.get(0);
        BigInteger distance = calculateDistance(nodeId, node.nodeId);
        for (int i = 1 ; i <kbucket.size();i++)
        {
            KademliaNode bnode = kbucket.get(i);
            // Calcula a distancia relativamente ao no 'bnode'
            // Caso seja menor guardamos como o node mais perto e a menor distancia
            BigInteger newDistance = calculateDistance(nodeId, bnode.nodeId);
            System.out.println(newDistance.compareTo(distance));
            // Caso seja menor guardamos como o node mais perto e a menor distancia
            if (newDistance.compareTo(distance) >= 0) {
                distance = newDistance;
                node = bnode;
            }
        }
        return node;
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

        for (int i  = 0 ; i < 20000; i++)
        {
            krt.insert(new KademliaNode("localhost",kd.generateNodeId(),5000));
        }
        krt.printTree();
        /*
        byte p1[] = kd.generateNodeId();
        byte p2[] = kd.generateNodeId();

        System.out.println(krt.printId(p1));
        System.out.println(krt.printId(p2));
        krt.compareId(p1,p2);


         */
    }
}

