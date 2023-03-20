package server;

import message.Message;
import message.Message.MessageType;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Server {
    String id;
    private boolean isLocked;
    List<String> clients;
    private Map<String, Socket> serverSocketMap;
    Map<String, DataInputStream> inputStreampMap;
    Map<String, DataOutputStream> outputStreamMap;
    Queue<Message> requestQueue;
    Map<String, Socket> socketMap;
    String lockedBy;
    int completionMessage;
    List<String> serverList;

    public String getId() {
        return id;
    }

    public Map<String, Socket> getServerSocketMap() {
        return serverSocketMap;
    }

    public Map<String, DataInputStream> getInputStreampMap() {
        return inputStreampMap;
    }

    public Map<String, DataOutputStream> getOutputStreamMap() {
        return outputStreamMap;
    }

    public synchronized void incrementCompletionMessages(){
        this.completionMessage++;
    }

    public synchronized void lock(Message message){
        System.out.println("Locking server... " + message.getSourceId());
        this.isLocked = true;
        this.lockedBy = message.getSourceId();
        System.out.println("Locked by ........ " + this.lockedBy);
        requestQueue.add(message);
    }

    public synchronized void unlock() {
        requestQueue.remove();
        System.out.println("Unlocking.....");
        if (!requestQueue.isEmpty()) {
            System.out.println("Request queue is not empty...");
            Message message = requestQueue.peek();
            //System.out.println("Queued message source: " + message.getSourceId());
            String clientId = message.getSourceId();
            if(isLocked()){
                this.lockedBy = clientId;
                System.out.println("Locked by ........ " + this.lockedBy);
            }
            Message replyMessage = new Message(MessageType.GRANT, this.id);
            System.out.println("Sending grant to " + clientId);
            Message.sendMessage(replyMessage, clientId, this.outputStreamMap);
        } else {
            System.out.println("Request queue is empty...");
            this.isLocked = false;
            this.lockedBy = null;
            System.out.println("Returning from unlocking");
        }
    }

    public boolean isLocked(){
        return this.isLocked;
    }

    public Queue<Message> getRequestQueue() {
        return requestQueue;
    }

    public Server(String id){
        this.id = id;
        this.requestQueue = new PriorityBlockingQueue<>();
        this.serverSocketMap = new HashMap<>();
        this.inputStreampMap = new HashMap<>();
        this.outputStreamMap = new HashMap<>();
        this.isLocked = false;
        this.completionMessage = 0;
        this.serverList = new ArrayList<>();
        this.socketMap = new HashMap<>();
        this.clients = new ArrayList<>();

        this.serverList.add("10.176.69.32");
        this.serverList.add("10.176.69.33");
        this.serverList.add("10.176.69.34");
        this.serverList.add("10.176.69.35");
        this.serverList.add("10.176.69.36");
        this.serverList.add("10.176.69.37");
        this.serverList.add("10.176.69.38");
    }

    public void endComputation(){
        //open connection from server 0 to all 6 other servers.
        int port = 5056;
        for(String otherServer: this.serverList){
            Socket socket = null;
            try {
                socket = new Socket(otherServer, port);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                this.clients.add(otherServer);
                this.inputStreampMap.put(otherServer, input);
                this.outputStreamMap.put(otherServer, out);
                this.socketMap.put(otherServer, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Connected to.. " + otherServer);
        }
        //send COMPLETE to all 6 servers and check if it is from server 0
        Message completeMessage = new Message(MessageType.COMPLETE, id);
        System.out.println("Sending completion message to other servers");
        for(String otherServer: this.serverList){
            Message.sendMessage(completeMessage, otherServer, this.outputStreamMap);
        }
    }

    public static void main(String[] args) throws IOException{
		ServerSocket serverSocket;
        System.out.println("Started server");
        String serverId = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server id " + serverId);
        Server server = new Server(serverId);
        try{
			serverSocket = new ServerSocket(5056);
			while(true){
				acceptClientRequest(serverSocket, server);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    public static void acceptClientRequest(ServerSocket serverSocket, Server server) {
		Socket s = null;
		try{
			s = serverSocket.accept();
			String clientIp = s.getInetAddress().getHostAddress();
            server.clients.add(clientIp);
            System.out.println("Recieved connection : " + s);
            server.getServerSocketMap().put(clientIp, s);
			DataInputStream input = new DataInputStream(s.getInputStream());
            server.getInputStreampMap().put(clientIp, input);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
            server.getOutputStreamMap().put(clientIp, out);
			System.out.println("Assigned thread for the client.");
			//One thread corresponding to each client
			Thread t = new ClientThread(s, input, out, server, clientIp);	
			t.start();
		}catch(Exception e){
			//e.printStackTrace();
		}
	}
}


