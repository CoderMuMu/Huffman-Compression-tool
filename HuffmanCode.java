package huffmanCode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class HuffmanCode {

    //哈夫曼编码表
    public static Map<Byte, String> codeTable = new HashMap<Byte, String>();
    //将字节数组转为Node结点集合
    public static List<Node> getNodes(byte[] bytes) {
        List<Node> nodes = new ArrayList<>();

        //遍历传入的bytes 放入map集合中
        Map<Byte, Integer> counts = new HashMap<>();
        for (byte b : bytes) {
            Integer count = counts.get(b);

            if (count == null) {
                counts.put(b, 1);
            } else {
                counts.put(b, ++count);
            }
        }

        //遍历map集合
        for (Map.Entry<Byte, Integer> entry : counts.entrySet()) {
            nodes.add(new Node(entry.getKey(), entry.getValue()));
        }
        return nodes;
    }

    //根据Node结点集合 创建Huffman树
    public static Node createHuffmanTree(List<Node> nodes) {
        //当数组只剩一个值时代表构建哈夫曼树完毕
        while (nodes.size() > 1) {
            //排序（升序）
            Collections.sort(nodes);

            //取出权值最小的两个结点
            Node leftNode = nodes.get(0);
            Node rightNode = nodes.get(1);

            //构建一个新的二叉树
            Node parentNode = new Node(null, leftNode.weight + rightNode.weight);
            parentNode.left = leftNode;
            parentNode.right = rightNode;

            //删除权值最小的两个结点
            nodes.remove(leftNode);
            nodes.remove(rightNode);
            nodes.add(parentNode);

        }

        return nodes.get(0);
    }

    public static Map<Byte, String> getCodeTable(Node node) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<Byte, String> codeTable = getCodeTable(node, "", stringBuilder);
        return codeTable;
     }

    public static Map<Byte, String> getCodeTable(Node node, String code, StringBuilder stringBuilder) {
        StringBuilder stringBuilder2 = new StringBuilder(stringBuilder);
        stringBuilder2.append(code);

        if (node != null) {
            if (node.data == null) { //非叶子节点
                //递归向左
                getCodeTable(node.left, "0", stringBuilder2);
                //递归向右
                getCodeTable(node.right, "1", stringBuilder2);
            } else {     //到叶子结点了
                //保存到map集合中
                codeTable.put(node.data, stringBuilder2.toString());

            }
        }

        return codeTable;
    }

    public static byte[] zip(byte[] bytes, Map<Byte, String> codeTable) {

        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : bytes) {
            stringBuilder.append(codeTable.get(b));

        }

        int lastLength = stringBuilder.length() % 8;
        int len;
        len = (stringBuilder.length() + 7) / 8;

        byte[] huffmanCodeBytes = new byte[len + 1];

        huffmanCodeBytes[0] = (byte) lastLength;

        for (int i = 0, index = 1; i < stringBuilder.length(); i += 8, index++) {
            String str;
            if (i + 8 > stringBuilder.length()) {
                str = stringBuilder.substring(i);
            } else {
                str = stringBuilder.substring(i,i+8);
            }
            huffmanCodeBytes[index] = (byte) Integer.parseInt(str, 2);
        }

        return huffmanCodeBytes;
    }

    public static byte[] huffmanZip(String str) {
        //获取字符串对应的字节数组
        byte[] bytes = str.getBytes();

        return huffmanZip(bytes);

    }

    public static byte[] huffmanZip(byte[] bytes) {
        //将字符数组转为Node结点集合
        List<Node> nodes = HuffmanCode.getNodes(bytes);
        //生成Huffman树
        Node huffmanCode = HuffmanCode.createHuffmanTree(nodes);
        //生成Huffman表
        Map<Byte, String> codeTable = HuffmanCode.getCodeTable(huffmanCode);
        //生成压缩后的Huffman数组
        byte[] zip = HuffmanCode.zip(bytes, codeTable);
        return zip;
    }


    public static byte[] decode(Map<Byte, String> huffmanCode, byte[] huffmanBytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < huffmanBytes.length; i++) {
          byte huffmanByte= huffmanBytes[i];
            boolean flag = (i == huffmanBytes.length - 1);
            stringBuilder.append(byteToBitString(!flag, huffmanByte, (int) huffmanBytes[0]));
        }
        HashMap<String, Byte> map = new HashMap<>();
        for (Map.Entry<Byte, String> entry : huffmanCode.entrySet()) {
            map.put(entry.getValue(),entry.getKey());
        }
        ArrayList<Byte> list = new ArrayList<>();
        int begin = 0;
        for (int i = 0; i < stringBuilder.length(); i++) {
            String key = stringBuilder.substring(begin,i+1);
            if (map.containsKey(key)) {
                list.add(map.get(key));
                begin = i + 1;
            }
        }
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    private static String byteToBitString(boolean flag, byte b, int lastLength) {
        int temp = b;
        temp |= 256;
        String s = Integer.toBinaryString(temp);

        if (!flag) {     //最后一位
            return s.substring(s.length() - lastLength);
        }
        //否则
        return s.substring(s.length() - 8);

    }

    public static void zipFile(String srcFile, String targetFile) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(srcFile);

        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);

        byte[] bytes = new byte[fileInputStream.available()];

        fileInputStream.read(bytes);

        byte[] bytes1 = HuffmanCode.huffmanZip(bytes);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        //传入压缩后的byte[]数组
        objectOutputStream.writeObject(bytes1);
        //传入编码表
        objectOutputStream.writeObject(HuffmanCode.codeTable);

        System.out.println("Compress successfully!");
        System.out.println("------------------------------");
    }

    //解压文件
    public static void unzipFile(String srcFile, String targetFile) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(srcFile);

        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);

        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        //压缩后的byte[]数组
        byte[] bytes = (byte[]) objectInputStream.readObject();

        //编码表
        Map<Byte, String> codeTable = (Map<Byte, String>) objectInputStream.readObject();
        //解码
        byte[] decode = HuffmanCode.decode(codeTable, bytes);
        fileOutputStream.write(decode);

        System.out.println("Decompress successfully!");
        System.out.println("------------------------------");

    }
}

