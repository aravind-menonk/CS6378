package client;

import java.io.*;
import java.net.*;
import java.util.*;

import message.*;
import message.Message.MessageType;
import networkpartitions.*;

public class Client {
    private String id; // Client IP
    private NetworkPartitions networkPartitions; // To get the network partitions at different steps
    private Map<String, Socket> socketMap; // Mapping the IP of server to the respective sockets
    private Map<String, DataInputStream> inputStreampMap; // Mapping the IP of server to the respective DataInputStream
    private Map<String, DataOutputStream> outputStreamMap; // Mapping the IP of server to the respective DataOutputStream

    public Client(String name){
        this.id = name;
        this.networkPartitions = new NetworkPartitions();
        //Relate the IP to the server names
        this.networkPartitions.initialiseServerNames();
        this.socketMap = new HashMap<>();
        this.inputStreampMap = new HashMap<>();
        this.outputStreamMap = new HashMap<>();
    }

    public static void main(String[] args) throws IOException{
        String clientId = InetAddress.getLocalHost().getHostAddress();
        System.out.println("ClientId " + clientId);
        Client client = new Client(clientId);
        //Open connections with all servers
        client.openSockets(client);
        
        try {
            //Send write to partition 1
            System.out.println("For partition one");
            client.sendMessagePartitionOne();
            Thread.sleep(8000);
            //Send write to partition 2
            System.out.println("For partition two");
            client.sendMessagePartitionTwo();
            Thread.sleep(8000);
            //Send write to partition 3
            System.out.println("For partition three");
            client.sendMessagePartitionThree();
            Thread.sleep(8000);
            //Send write to partition 4
            System.out.println("For partition four");
            client.sendMessagePartitionFour();
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Closing connections...");
        client.closeSockets(client);
    }

    //Open socket connections to the servers
    public void openSockets(Client client) throws IOException{
        int port = 5056;
        for(Character serverName: networkPartitions.getServerNameMap().keySet()){
            String server = networkPartitions.getServerNameMap().get(serverName);
            Socket socket = new Socket(server, port);
            client.socketMap.put(server, socket);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            client.inputStreampMap.put(server, input);
            outputStreamMap.put(server, out);
            System.out.println("Connected to.. " + serverName + " " + server);
        }
    }

    //Close socket connections to the servers
    public void closeSockets(Client client){
        try{
            for(Character serverName: networkPartitions.getServerNameMap().keySet()){
                String server = networkPartitions.getServerNameMap().get(serverName);
                client.inputStreampMap.get(server).close();
                outputStreamMap.get(server).close();
                client.socketMap.get(server).close();
                System.out.println("Closed.. " + server);
            } 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
     * Below methods are for getting the current partitions and send writes to 2 servers in one partition.
     * If there is only a single server in a partition, then send the write request twice to that server.
     */
    public void sendMessagePartitionOne(){
        Message m = new Message(MessageType.UPDATE, id, null);
        List<Set<Character>> partition = networkPartitions.getPartitionOne();
        sendMessagePartition(partition, m);
    }

    public void sendMessagePartitionTwo(){
        Message m = new Message(MessageType.UPDATE, id, null);
        List<Set<Character>> partition = networkPartitions.getPartitionTwo();
        sendMessagePartition(partition, m);
    }

    public void sendMessagePartitionThree(){
        Message m = new Message(MessageType.UPDATE, id, null);
        List<Set<Character>> partition = networkPartitions.getPartitionThree();
        sendMessagePartition(partition, m);
    }

    public void sendMessagePartitionFour(){
        Message m = new Message(MessageType.UPDATE, id, null);
        List<Set<Character>> partition = networkPartitions.getPartitionFour();
        sendMessagePartition(partition, m);
    }

    public void sendMessagePartition(List<Set<Character>> partition, Message m){
        for(Set<Character> element: partition){
            Iterator<Character> iterator = element.iterator();
            int counter = 0;
            if(element.size() == 1){
                Character serverName = iterator.next();
                for(int i = 0; i < 2; i++){
                    System.out.println("Sending... to " + serverName + " " + networkPartitions.getServerNameMap().get(serverName));
                    Message.sendMessage(m, networkPartitions.getServerNameMap().get(serverName), outputStreamMap);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                while(iterator.hasNext()){
                    Character serverName = iterator.next();
                    System.out.println("Sending... to " + serverName + " " + networkPartitions.getServerNameMap().get(serverName));
                    Message.sendMessage(m, networkPartitions.getServerNameMap().get(serverName), outputStreamMap);
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                    if (counter > 1){
                        break;
                    }
                }
            }
        }
    }
}
