package server;

import message.Message;
import message.Message.MessageType;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Server {
    String id; // IP of the server
    private boolean isLocked; // state of server
    List<String> clients; // List of clients connected
    private Map<String, Socket> serverSocketMap; // Mapping the IP of client to the respective sockets
    Map<String, DataInputStream> inputStreampMap; // Mapping the IP of client to the respective DataInputStream
    Map<String, DataOutputStream> outputStreamMap; // Mapping the IP of client to the respective DataOutputStream
    Queue<Message> requestQueue; // Request queue to maintain client requests. It is a priority blocking queue which is thread safe
    Map<String, Socket> socketMap; // socket details for the other servers to send completion notification
    String lockedBy; // Client IP which has currently locked the system
    int completionMessage; // count of completion messages received so far
    List<String> serverList; // List of servers to send the completion notif from server 0
    private int messagesSent;
    private int messagesReceived;

    public synchronized void incrementMessagesReceived(){
        this.messagesReceived++;
    }

    public synchronized void incrementMessagesSent(){
        this.messagesSent++;
    }

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
        // this.isLocked = true;
        this.lockedBy = message.getSourceId();
        //System.out.println("Locked by ........ " + this.lockedBy);
        requestQueue.add(message);
    }

    public synchronized void unlock() {
        /*
         * The one which has locked the server might not be at the top of the queue. Possible that due to
         * network delay, another request with smaller time stamp has arrived after it has granted to a different server.
         * Hence, we cant use requestQueue.remove() directly.
         */
        Message m = Message.findRequestMessage(this.lockedBy, requestQueue);
        if(m != null){
            requestQueue.remove(m);
            System.out.println("Unlocking.....Removed from queue " + m.getSourceId());
        }
        if (!requestQueue.isEmpty()) {
            System.out.println("Request queue is not empty...");
            Message message = requestQueue.peek();
            //System.out.println("Queued message source: " + message.getSourceId());
            String clientId = message.getSourceId();
            if(this.isLocked){
                this.lockedBy = clientId;
                System.out.println("Locked by ........ " + this.lockedBy);
            }
            Message replyMessage = new Message(MessageType.GRANT, this.id);
            System.out.println("Sending grant to " + clientId);
            Message.sendMessage(replyMessage, clientId, this.outputStreamMap);
            this.incrementMessagesSent();
        } else {
            System.out.println("Request queue is empty...");
            this.isLocked = false;
            this.lockedBy = null;
            //System.out.println("Returning from unlocking");
        }
    }

    /*
     * If server is unlocked, proceed to lock the server and return true.
     * Else return false.
     */
    public synchronized boolean tryLock(){
        // return this.isLocked;
        if(this.isLocked){
            return false;
        }else{
            this.isLocked = true;
            return true;
        }
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
        this.messagesReceived = 0;
        this.messagesSent = 0;

        //List of servers to send the completion notification to from Server 0.
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
            //this.incrementMessagesSent();
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

    public void printStats(){
        System.out.println();
        System.err.println("Total number of messages sent from " + this.id + " = " + this.messagesSent);
        System.err.println("Total number of messages received to " + this.id + " = " + this.messagesReceived);
    }
}


