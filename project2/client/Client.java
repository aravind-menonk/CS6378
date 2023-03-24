package client;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;


import message.Message;
import message.Message.MessageType;

public class Client {
    private String id; // IP of the client
    private List<String> serverList; // List of 7 servers
    Map<String, String> serverNameMap; // Mapping the name of servers (like server 1, server 2 etc.) to the IP
    Map<String, String> clientNameMap; // Mapping the name of clients to the IP
    private Map<String, Socket> socketMap; // Mapping the IP of server to the respective sockets
    Map<String, DataInputStream> inputStreampMap; // Mapping the IP of server to the respective DataInputStream
    Map<String, DataOutputStream> outputStreamMap; // Mapping the IP of server to the respective DataOutputStream
    private Quorums quorums; // Contains all the quorums needed and functions related
    private int messagesSent; 
    private int messagesReceived;
    List<CriticalSectionAttempt> criticalSectionAttempt; // Class to track the stats when the client enters the critical section
    private double waitingTime; // Configurations to control waiting time before entering CS
    private double timeInCS; // Configurations to control time spent in CS
    
    public Client(String id){
            this.id = id;
            this.serverList = new ArrayList<>();
            this.socketMap = new HashMap<>();
            this.serverNameMap = new HashMap<>();
            this.clientNameMap = new HashMap<>();
            this.inputStreampMap = new HashMap<>();
            this.outputStreamMap = new HashMap<>();
            this.quorums = new Quorums();
            this.messagesReceived = 0;
            this.messagesSent = 0;
            this.criticalSectionAttempt = new ArrayList<>();
            this.waitingTime = 5;
            this.timeInCS = 0.3;

            for(int i = 0; i < 20; i++){
                this.criticalSectionAttempt.add(new CriticalSectionAttempt());
            }

            configureClientNameMap(this);
            //List of servers 
            this.serverList.add("10.176.69.32"); // Server 1
            this.serverList.add("10.176.69.33"); // Server 2
            this.serverList.add("10.176.69.34"); // Server 3
            this.serverList.add("10.176.69.35"); // Server 4
            this.serverList.add("10.176.69.36"); // Server 5
            this.serverList.add("10.176.69.37"); // Server 6
            this.serverList.add("10.176.69.38"); // Server 7

            for(int i = 0; i < 7; i++){
                StringBuilder sb = new StringBuilder();
                sb.append("Server ").append(i + 1);
                this.serverNameMap.put(serverList.get(i), sb.toString());
            }

            //Setup the quorums and print it
            this.quorums.configureQuorums();
            this.quorums.printQuorums(serverNameMap);
    }

    public synchronized void incrementMessagesReceived(){
        this.messagesReceived++;
    }

    public synchronized void incrementMessagesSent(){
        this.messagesSent++;
    }

    public Map<String, DataInputStream> getInputStreampMap() {
        return inputStreampMap;
    }

    public Map<String, DataOutputStream> getOutputStreamMap() {
        return outputStreamMap;
    }

    public void enterCriticalSection() throws InterruptedException{
        for(int i = 0; i < 20; i++){
            try{
                //int randomNumber = new Random().nextInt(5) + 5;
                Thread.sleep((long)(1000 * waitingTime));
                Message reqMessage = new Message(MessageType.REQUEST, id);
                this.criticalSectionAttempt.get(i).setAttemptNumber(i);
                //send message to all servers and wait for replies
                broadcastReqMessage(reqMessage, i);
                //send release to all servers
                Message releaseMessage = new Message(MessageType.RELEASE, id);
                broadcastRelMessage(releaseMessage, i);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /*
     * Send requests to the servers. If all the servers in one quorum sent GRANT replies, enter the critical section.
     */
    public void broadcastReqMessage(Message message, int attemptNumber){
        ExecutorService executorService = Executors.newFixedThreadPool(serverList.size());
        Set<String> repliedServers = new ConcurrentSkipListSet<>();
        long startTime = System.currentTimeMillis();
        for (String server : serverList) {
            Runnable task = new ServerRequestTask(server, repliedServers, message, this, attemptNumber);
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            while (true) {
                if (quorums.checkInQuorums(repliedServers)) {
                    executorService.shutdownNow();
                    this.criticalSectionAttempt.get(attemptNumber).setMessagesToEnter(this.criticalSectionAttempt.get(attemptNumber).getGrantsReceived());
                    long endTime = System.currentTimeMillis();
                    System.out.println("\n" + attemptNumber +". Entering ..." + this.clientNameMap.get(id) + " " + endTime + "\n");
                    Thread.sleep((long)(1000 * timeInCS));
                    this.criticalSectionAttempt.get(attemptNumber).setTimeElapsed(endTime - startTime);
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void broadcastRelMessage(Message message, int attemptNumber){
        for(String server: serverList){
            System.out.println("Sending release to " + serverNameMap.get(server) + " " + server);
            Message.sendMessage(message, server, this.outputStreamMap);
            this.incrementMessagesSent();
            this.criticalSectionAttempt.get(attemptNumber).incrementRequestsSent();
        }
    }

    /*
     * Server 0 is 10.176.69.75. Start a connection with the server 0 only towards the end 
     * when it requires to send completion notif.
     */
    public void sendCompleteNotif(Client client, String id){
        String completionServer = "10.176.69.75";
        Socket socket;
        try {
            socket = new Socket(completionServer, 5056);
            client.socketMap.put(completionServer, socket);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            client.inputStreampMap.put(completionServer, input);
            client.outputStreamMap.put(completionServer, out);
            System.out.println("Connected to Server 0.. " + completionServer);
            Message completeMessage = new Message(MessageType.COMPLETE, id);
            System.out.println("Sending completion message to Server 0");
            Message.sendMessage(completeMessage, completionServer , this.outputStreamMap);
            //this.incrementMessagesSent();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public static void main(String[] args) throws InterruptedException, IOException{
        String clientId = InetAddress.getLocalHost().getHostAddress();
        System.out.println("ClientId " + clientId);
        Client client = new Client(clientId);
        System.out.println("Opening connections...");
        openSockets(client);
        client.enterCriticalSection();
        client.sendCompleteNotif(client, client.id);
        System.out.println("Closing connections...");
        closeSockets(client);
        printStats(client);
	}

    public static void openSockets(Client client) throws IOException{
        int port = 5056;
        for(String server: client.serverList){
            Socket socket = new Socket(server, port);
            client.socketMap.put(server, socket);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            client.inputStreampMap.put(server, input);
            client.outputStreamMap.put(server, out);
            System.out.println("Connected to.. " + client.serverNameMap.get(server) + " " + server);
        }
    }

    public static void closeSockets(Client client){
        try{
            for(String server: client.serverList){
                client.inputStreampMap.get(server).close();
                client.outputStreamMap.get(server).close();
                client.socketMap.get(server).close();
                System.out.println("Closed.. " + server);
            } 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
     * Print the required stats.
     */
    public static void printStats(Client client){
        System.out.println();

        System.err.println("Total number of messages sent from " + client.id + " = " + client.messagesSent);
        System.err.println("Total number of messages received by " + client.id + " = " + client.messagesReceived);

        for(int i = 0; i < 20; i++){
            client.criticalSectionAttempt.get(i).print();
        }
    }

    public static void configureClientNameMap(Client client){
        client.clientNameMap.put("10.176.69.39", "Client 1");
        client.clientNameMap.put("10.176.69.40", "Client 2");
        client.clientNameMap.put("10.176.69.41", "Client 3");
        client.clientNameMap.put("10.176.69.42", "Client 4");
        client.clientNameMap.put("10.176.69.43", "Client 5");
    }
}
