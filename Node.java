package huffmanCode;

public class Node implements Comparable<Node>{

    Byte data;  //存放数据的ASCII 'a' => 97
    int weight;  //权值 该数据出现的次数
    Node left;
    Node right;

    public Node(Byte data, int weight) {
        this.data = data;
        this.weight = weight;
    }

    //前序遍历
    public void preorderTraversal(){
        System.out.println(this);
        if (this.left != null){
            this.left.preorderTraversal();
        }
        if (this.right != null){
            this.right.preorderTraversal();
        }
    }

    public int compareTo(Node o) {
        return this.weight - o.weight;
    }

    public String toString() {
        return "Node{" + "data=" + data + ", weight=" + weight + '}';
    }
}

