package client;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;


import message.Message;
import message.Message.MessageType;

public class Client {
    private String id;
    private List<String> serverList;
    Map<String, String> serverNameMap;
    private Map<String, Socket> socketMap;
    Map<String, DataInputStream> inputStreampMap;
    Map<String, DataOutputStream> outputStreamMap;
    private Set<String> quorumOne;
    
    public Client(String id){
            this.id = id;
            serverList = new ArrayList<>();
            socketMap = new HashMap<>();
            serverNameMap = new HashMap<>();
            inputStreampMap = new HashMap<>();
            outputStreamMap = new HashMap<>();
            quorumOne = new HashSet<>();

            serverList.add("10.176.69.32");
            serverList.add("10.176.69.33");
            serverList.add("10.176.69.34");
            serverList.add("10.176.69.35");
            serverList.add("10.176.69.36");
            serverList.add("10.176.69.37");
            serverList.add("10.176.69.38");

            for(int i = 0; i < 7; i++){
                StringBuilder sb = new StringBuilder();
                sb.append("Server ").append(i + 1);
                serverNameMap.put(serverList.get(i), sb.toString());
            }

            quorumOne.add("10.176.69.32");
            quorumOne.add("10.176.69.33");
            quorumOne.add("10.176.69.34");
    }

    public Map<String, DataInputStream> getInputStreampMap() {
        return inputStreampMap;
    }

    public Map<String, DataOutputStream> getOutputStreamMap() {
        return outputStreamMap;
    }

    public void enterCriticalSection() throws InterruptedException{
        for(int i = 0; i < 1; i++){
            try{
                int randomNumber = new Random().nextInt(5) + 5;
                Thread.sleep(1000 * randomNumber);
                Message reqMessage = new Message(MessageType.REQUEST, id);
                //send message to all servers
                broadcastReqMessage(reqMessage);
                //if we get reply
                System.out.println("Entering..." + System.currentTimeMillis());
                Thread.sleep(3000);
                //send release to all servers
                Message releaseMessage = new Message(MessageType.RELEASE, id);
                //send message to all servers
                broadcastRelMessage(releaseMessage);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void broadcastReqMessage(Message message){
        ExecutorService executorService = Executors.newFixedThreadPool(serverList.size());
        Set<String> repliedServers = new ConcurrentSkipListSet<>();
        for (String server : serverList) {
            Runnable task = new ServerRequestTask(server, repliedServers, message, this);
            executorService.execute(task);
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                if (repliedServers.containsAll(quorumOne)) {
                    System.out.println("Entering..." + System.currentTimeMillis());
                    Thread.sleep(3000);
                    executorService.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void broadcastRelMessage(Message message){
        for(String server: serverList){
            System.out.println("Sending release to " + serverNameMap.get(server) + " " + server);
            Message.sendMessage(message, server, this.outputStreamMap);
        }
    }

    public void sendCompleteNotif(String id){
        Message completeMessage = new Message(MessageType.COMPLETE, id);
        System.out.println("Sending completion message to Server 0");
        Message.sendMessage(completeMessage, "10.176.69.32", this.outputStreamMap);
        //Message.receiveMessage("10.176.69.32", this.inputStreampMap);
    }

    public static void main(String args[]) throws InterruptedException, IOException{
        String clientId = InetAddress.getLocalHost().getHostAddress();
        System.out.println("ClientId " + clientId);
        Client client = new Client(clientId);
        System.out.println("Opening connections...");
        openSockets(client);
        client.enterCriticalSection();
        Thread.sleep(5000);
        client.sendCompleteNotif(client.id);
        while(true)
            Thread.sleep(5000);
        //System.out.println("Closing connections...");
        //closeSockets(client);
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
}
