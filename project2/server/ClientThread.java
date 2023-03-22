package server;

import java.io.*;
import java.net.*;
import message.Message;
import message.Message.MessageType;

public class ClientThread extends Thread{
    private Server server;
    final String clientIp;
    final DataInputStream input;
	final DataOutputStream out;
	final Socket socket;

    public ClientThread(Socket socket, DataInputStream input, DataOutputStream out, Server server, String clientIp){
		this.socket = socket;
		this.input = input;
		this.out = out;
        this.server = server;
        this.clientIp = clientIp;
	}

    public String getClientIp(){
        return this.clientIp;
    }

    @Override
	public void run(){
        try{
            while(true){
                    Message message = Message.receiveMessage(this.clientIp, server.inputStreampMap);
                    if(message.getMessageType().equals(MessageType.REQUEST)){
                        // if(server.id.equals("10.176.69.32")){
                        //     server.requestQueue.add(requestMessage);
                        //     continue;
                        // }
                        this.server.incrementMessagesReceived();
                        handleRequestMessage(message);
                    }
                    
                    if(message.getMessageType().equals(MessageType.RELEASE)){
                        this.server.incrementMessagesReceived();
                        handleReleaseMessage(message);
                    }

                    if(message.getMessageType().equals(MessageType.COMPLETE)){
                        handleCompleteMessage(message);
                    }
                    
                    /*
                     * When server 0 receives 5 completion messages, it means that the clients have finished processing.
                     * End the computation.
                     */
                    if(server.completionMessage == 5){
                        System.out.println("Ending the computation... Sending messages to other servers");
                        server.endComputation();
                        //end computation in the server
                        this.server.printStats();
                        closeSockets(this.server);
                        System.exit(0);
                    }
            }
        }catch(Exception e){
            //e.printStackTrace();
        }
    }

    /*
     * Add to the request queue if its locked. Else send grant to the client and wait for the release from the client.
     */
    public void handleRequestMessage(Message requestMessage){
        if(server.isLocked()){
            System.out.println("Server locked... ");
            server.requestQueue.add(requestMessage);
        }else{
            System.out.println("Server not locked... ");
            server.lock(requestMessage);
            Message message = new Message(MessageType.GRANT, server.id);
            Message.sendMessage(message, this.clientIp, server.outputStreamMap);
            this.server.incrementMessagesSent();
            System.out.println("Sent grant message..." + this.clientIp);
            Message releaseMessage = Message.receiveMessage(this.clientIp, server.inputStreampMap);
            this.server.incrementMessagesReceived();
            if(releaseMessage.getMessageType().equals(MessageType.RELEASE)){
                handleReleaseMessage(releaseMessage);
            }
        }
    }

    /*
     * Unlock the server if the release message is from the client who has currently locked the server.
     * Else, remove the request from the request queue since the client has already entered the CS.
     */
    public void handleReleaseMessage(Message releaseMessage){
        System.out.println("Recieved release message from " + releaseMessage.getSourceId());
        System.out.println("Locked by.. " + server.lockedBy);
        if(server.lockedBy != null && server.lockedBy.equals(releaseMessage.getSourceId())){
            server.unlock();
        }
        // System.out.println("Finding the request message to be deleted....");
        Message message = Message.findRequestMessage(releaseMessage, server.requestQueue);
        if(message != null){
            server.requestQueue.remove(message);
            System.out.println("Removed request from queue..." + message.getSourceId());
        }
    }

    /*
     * If it is server 0 getting the completion message from the client, increment the number of completion messages.
     * If server 0 is the one sending the completion message, close the connections and stop the computation.
     */
    public void handleCompleteMessage(Message completeMessage){
        if(server.id.equals("10.176.69.75")){
            server.incrementCompletionMessages();
            System.out.println("Number of completion messages  = " + server.completionMessage);
        }
        if(completeMessage.getSourceId().equals("10.176.69.75")){
            closeSockets(this.server);
            this.server.printStats();
            System.exit(0);
        }
    }

    public static void closeSockets(Server server){
        try{
            for(String name: server.clients){
                closeSocket(name, server);
            } 
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void closeSocket(String name, Server server){
        try{
            System.out.println("Closed.. " + server);
            server.inputStreampMap.get(name).close();
            server.outputStreamMap.get(name).close();
            server.socketMap.get(name).close();
            server.getServerSocketMap().get(name).close();
        }catch(Exception e){
            //do nothing
        } 
    }
}
