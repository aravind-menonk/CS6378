package server;

import java.io.*;
import java.net.*;
import java.util.*;

import message.*;
import networkpartitions.*;

public class Server {
    private String id;
    private Character name;
    private Map<String, Socket> serverSocketMap;
    private Map<String, Socket> socketMap;
    private Map<String, DataInputStream> oInputStreampMap; 
    private Map<String, DataOutputStream> oOutputStreamMap; 
    private Map<String, DataInputStream> iInputStreampMap; 
    private Map<String, DataOutputStream> iOutputStreamMap;
    private ObjectX object;
    private NetworkPartitions networkPartitions;
    private Set<Character> currPartition;
    private int stepNumber;


    public NetworkPartitions getNetworkPartitions() {
        return networkPartitions;
    }

    public void incrementStepNumber(){
        this.stepNumber++;
    }

    public Set<Character> getCurrPartition() {
        return currPartition;
    }

    public String getId() {
        return id;
    }

    public ObjectX getObject() {
        return object;
    }

    public void setObject(
            ObjectX object) {
        this.object = object;
    }

    public Map<String, Socket> getServerSocketMap() {
        return serverSocketMap;
    }

    public Map<String, DataInputStream> getoInputStreampMap() {
        return oInputStreampMap;
    }

    public void setoInputStreampMap(
            Map<String, DataInputStream> oInputStreampMap) {
        this.oInputStreampMap = oInputStreampMap;
    }

    public Map<String, DataOutputStream> getoOutputStreamMap() {
        return oOutputStreamMap;
    }

    public void setoOutputStreamMap(
            Map<String, DataOutputStream> oOutputStreamMap) {
        this.oOutputStreamMap = oOutputStreamMap;
    }

    public Map<String, DataInputStream> getiInputStreampMap() {
        return iInputStreampMap;
    }

    public void setiInputStreampMap(
            Map<String, DataInputStream> iInputStreampMap) {
        this.iInputStreampMap = iInputStreampMap;
    }

    public Map<String, DataOutputStream> getiOutputStreamMap() {
        return iOutputStreamMap;
    }

    public void setiOutputStreamMap(
            Map<String, DataOutputStream> iOutputStreamMap) {
        this.iOutputStreamMap = iOutputStreamMap;
    }

    public Server(String id){
        this.networkPartitions = new NetworkPartitions();
        this.networkPartitions.initialiseServerNames();
        this.name = findServerName(id);
        this.id = id;
        this.serverSocketMap = new HashMap<>();
        this.oInputStreampMap = new HashMap<>();
        this.iInputStreampMap = new HashMap<>();
        this.socketMap = new HashMap<>();
        this.oOutputStreamMap = new HashMap<>();
        this.iOutputStreamMap = new HashMap<>();
        this.currPartition = new HashSet<>();
        this.stepNumber = 0;
        this.object = new ObjectX(8, 'A');
    }

    public Character findServerName(String serverId){
        for(Map.Entry<Character,String> entry : networkPartitions.getServerNameMap().entrySet()){
            if (entry.getValue().equals(serverId)){
                return entry.getKey();
            }
        }
        return null;
    }

    public void print(Set<Character> set){
        System.out.println("Current partition");
        for(Character c: set){
            System.out.print(c + " ");;
        }
        System.out.println();
    }

    public void processUpdates(){
        try{
            //Open connections to first partition
            List<Set<Character>> partitions = this.networkPartitions.getPartitionOne();
            this.currPartition = getServerCurrentPartition(partitions);
            print(currPartition);
            //Connect to every server in the partition
            openSockets();
            while(this.stepNumber < 2){
                Thread.sleep(1000);
            }
            partitions = this.networkPartitions.getPartitionTwo();
            this.currPartition = getServerCurrentPartition(partitions);
            print(currPartition);
            closeSockets();
            while(this.stepNumber < 4){
                Thread.sleep(1000);
            }
            partitions = this.networkPartitions.getPartitionThree();
            this.currPartition = getServerCurrentPartition(partitions);
            print(currPartition);
            closeSockets();
            while(this.stepNumber < 6){
                Thread.sleep(1000);
            }
            partitions = this.networkPartitions.getPartitionFour();
            this.currPartition = getServerCurrentPartition(partitions);
            if(currPartition != null){
                print(currPartition);
                openSockets();
                while(this.stepNumber < 8){
                    Thread.sleep(1000);
                }
                this.currPartition = null;
            }
            closeSockets();
        }catch(Exception e){
            //ignore
        }
    }

    public void closeSockets(){
        try{
            Set<String> serversToBeClosed = new HashSet<>();
            for(String serverId: this.oInputStreampMap.keySet()){
                Character serverName = this.networkPartitions.getServerName(serverId);
                if(this.currPartition == null ||  !this.currPartition.contains(serverName)){
                    closeSocket(serverId);
                    serversToBeClosed.add(serverId);
                }
            }
            for(String server: serversToBeClosed){
                this.oInputStreampMap.remove(server);
                this.oOutputStreamMap.remove(server);
                this.socketMap.remove(server);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Character getName() {
        return name;
    }

    public void closeSocket(String serverId) throws IOException{
        this.oInputStreampMap.get(serverId).close();
        this.oOutputStreamMap.get(serverId).close();
        this.socketMap.get(serverId).close();
    }

    public void openSockets() throws IOException{
        int port = 5056;
        if(this.currPartition == null){
            return;
        }
        for(Character serverName: this.currPartition){
            String server = networkPartitions.getServerNameMap().get(serverName);
            if(this.oInputStreampMap.containsKey(server) || this.name.equals(serverName)){
                System.out.println("Skipping connection to " + serverName + " already present.");
                continue;
            }
            Socket socket = new Socket(server, port);
            this.socketMap.put(server, socket);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            this.oInputStreampMap.put(server, input);
            this.oOutputStreamMap.put(server, out);
            System.out.println("Connected to.. " + serverName + " " + server);
        }
    }

    public Set<Character> getServerCurrentPartition(List<Set<Character>> partitions){
        for(Set<Character> element: partitions){
            if(element.contains(this.name)){
                return element;
            }
        }
        return null;
    }

    public boolean canUpdate(){
        //Check if current partition size is even
        if(currPartition.size() > (object.getReplicatedUnits() / 2)){
            System.out.println("Partition size greater than half replicated units. Proceeding to update...");
            return true;
        }
        else if(currPartition.size() == (object.getReplicatedUnits() / 2) && currPartition.contains(object.getDistinguishedSite())){
            System.out.println("Partition size equal to half replicated units, but current partition contains distinguished site. Proceeding to update...");
            return true;
        }
        System.out.println("Cannot update since required criteria is not met...");
        return false;
    }

    private void startServer() {
        int port = 5056;
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server started on port " + port);
                while (true) {
                    Socket s = serverSocket.accept();
                    String clientIp = s.getInetAddress().getHostAddress();
                    this.getServerSocketMap().put(clientIp, s);
                    DataInputStream input = new DataInputStream(s.getInputStream());
                    this.getiInputStreampMap().put(clientIp, input);
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());
                    this.getiOutputStreamMap().put(clientIp, out);
                    //One thread corresponding to each client
                    Thread t = new ClientThread(s, input, out, this, clientIp);	
                    t.start();
                }
            } catch (IOException e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        }).start();
    }

    public void printObject(){
        System.out.println("---------------------------------------------------");
        if(object.getDistinguishedSite() != null){
            System.out.println("VN: " + object.getVersionNumber() + " RU: " + object.getReplicatedUnits() + " DS: " + object.getDistinguishedSite());
        }
        else{
            System.out.println("VN: " + object.getVersionNumber() + " RU: " + object.getReplicatedUnits() + " DS: - ");
        }
        System.out.println("---------------------------------------------------");
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        //System.out.println("Started server");
        String serverId = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server id " + serverId);
        Server server = new Server(serverId);
        server.startServer();
        Thread.sleep(2000);
        server.processUpdates();
        System.exit(0);
    }
}
