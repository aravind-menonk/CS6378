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
        boolean endReached = false;
        try{
            while(!endReached){
                try{
                    System.out.println("Waiting for message from.." + this.clientIp);
                    Message requestMessage = Message.receiveMessage(this.clientIp, server.inputStreampMap);
                    if(requestMessage.getMessageType().equals(MessageType.REQUEST)){
                        if(server.isLocked()){
                            System.out.println("Server locked... ");
                            server.requestQueue.add(requestMessage);
                            Message message = new Message(MessageType.QUEUED, server.id);
                            Message.sendMessage(message, this.clientIp, server.outputStreamMap);
                            System.out.println("Sent queued message..." + this.clientIp);
                        }else{
                            System.out.println("Server not locked... ");
                            server.lock(requestMessage);
                            Message message = new Message(MessageType.GRANT, server.id);
                            Message.sendMessage(message, this.clientIp, server.outputStreamMap);
                            System.out.println("Sent grant message..." + this.clientIp);
                            Message releaseMessage = Message.receiveMessage(this.clientIp, server.inputStreampMap);
                            if(releaseMessage.getMessageType().equals(MessageType.RELEASE)){
                                System.out.println("Received release message...");
                                server.unlock();
                            }
                        }
                    }
                    
                    if(requestMessage.getMessageType().equals(MessageType.RELEASE)){
                        System.out.println("Recieved release message from " + requestMessage.getSourceId());
                        System.out.println("Locked by.. " + server.lockedBy);
                        if(server.lockedBy.equals(requestMessage.getSourceId())){
                            server.unlock();
                        }
                        // Message message = Message.findRequestMessage(requestMessage, server.requestQueue);
                        // if(message != null)
                        //     server.requestQueue.remove(message);
                    }

                    if(requestMessage.getMessageType().equals(MessageType.COMPLETE)){
                        //wait until you get it from the server 0
                        if(server.id.equals("10.176.69.32")){
                            server.incrementCompletionMessages();
                            System.out.println("Number of completion messages  = " + server.completionMessage);
                        }
                        if(requestMessage.getSourceId().equals("10.176.69.32")){
                            System.exit(0);
                        }
                        // while(true)
                        //     Thread.sleep(10000);
                        // Message message = Message.findRequestMessage(requestMessage, server.requestQueue);
                        // server.requestQueue.remove(message);
                    }
                    
                    if(server.completionMessage == 5){
                        System.out.println("Ending the computation... Sending messages to other servers");
                        server.endComputation();
                        //end computation in the server
                        System.exit(0);
                    }
                }catch(Exception eo){
                    endReached = true;
                }
            }
        }catch(Exception e){
            //e.printStackTrace();
        }
    }
}
