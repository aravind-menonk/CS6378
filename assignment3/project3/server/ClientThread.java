package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import message.Message;
import message.ObjectX;
import message.Message.MessageType;

public class ClientThread extends Thread{
    Server server;
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

    @Override
	public void run(){
        try{
            while(true){
                Message message = Message.receiveMessage(this.clientIp, server.getiInputStreampMap());
                //Received vote request from server
                if(message.getMessageType().equals(MessageType.VOTE_REQUEST)){
                    Character name = this.server.getNetworkPartitions().getServerName(message.getSourceId());
                    System.out.println("Recieved vote request from " + name);
                    Message m = new Message(MessageType.VOTE_RESPONSE, server.getId(), null);
                    //send the vote response to the source IP
                    System.out.println("Sending vote response to " + name + " " + message.getSourceId());
                    Message.sendMessage(m, message.getSourceId(), server.getiOutputStreamMap());
                    //wait for the commit from source IP
                    //System.out.println("Waiting for commit....");
                    m = Message.receiveMessage(this.clientIp, server.getiInputStreampMap());
                    //Commit
                    if(m.getMessageType().equals(MessageType.COMMIT)){
                        System.out.println("Received Commit from server " + name);
                        server.setObject(message.getObject());
                    }
                    if(m.getMessageType().equals(MessageType.ABORT)){
                        System.out.println("Received Abort from server " + name);
                        //Do nothing
                    }
                }
                //Update from Client
                if(message.getMessageType().equals(MessageType.UPDATE) && message.getObject() == null){
                    System.out.println("Received update request from client.");
                    ObjectX obOriginal = server.getObject();
                    ObjectX ob = new ObjectX();
                    ThreadSafeInteger numServersReplied = new ThreadSafeInteger(1);
                    ob.setVersionNumber(obOriginal.getVersionNumber() + 1);
                    ob.setReplicatedUnits(server.getCurrPartition().size());
                    if(server.getCurrPartition().size() % 2 == 0){
                            ob.setDistinguishedSite(getDistinguishedSite());
                    }else{
                            ob.setDistinguishedSite(null);
                    }
                    if(server.getCurrPartition().size() > 1){
                        Message m = new Message(MessageType.VOTE_REQUEST, server.getId(), ob);
                        //Send vote request and receive vote response from all servers in the partition
                        ExecutorService executorService = Executors.newFixedThreadPool(server.getCurrPartition().size() - 1);
                        for (Character serverName : server.getCurrPartition()) {
                            if(serverName.equals(this.server.getName())){
                                continue;
                            }
                            Runnable task = new ServerRequestTask(server.getNetworkPartitions().getServerNameMap().get(serverName), numServersReplied, m, this);
                            executorService.execute(task);
                        }
                        executorService.shutdown();
                        while(true){
                            Thread.sleep(5000);
                            if(numServersReplied.get() == this.server.getCurrPartition().size()){
                                break;
                            }
                        }
                        executorService.shutdownNow();
                    }
                    boolean canUpdate = server.canUpdate();
                    if(server.getCurrPartition().size() > 1){
                        if(canUpdate){
                            server.setObject(ob);
                            Message m = new Message(MessageType.COMMIT, server.getId(), null);
                            for(Character serverName: server.getCurrPartition()){
                                if(serverName.equals(this.server.getName())){
                                    continue;
                                }   
                                System.out.println("Sending commit to server " + serverName);
                                Message.sendMessage(m, server.getNetworkPartitions().getServerNameMap().get(serverName), server.getoOutputStreamMap());
                            }
                        }else{
                            Message m = new Message(MessageType.ABORT, server.getId(), null);
                            for(Character serverName: server.getCurrPartition()){
                                if(serverName.equals(this.server.getName())){
                                    continue;
                                }
                                System.out.println("Sending abort to server " + serverName);
                                Message.sendMessage(m, server.getNetworkPartitions().getServerNameMap().get(serverName), server.getoOutputStreamMap());
                            }
                        }
                    }else{
                        if(canUpdate){
                            server.setObject(ob);
                        }
                    }
                }
                server.printObject();
                //update stepNumber
                server.incrementStepNumber();
            }
        }catch(Exception e){
            //ignore
        }
    }


    public char getDistinguishedSite(){
        char alphabeticallyFirst = 'H';
        for (char c : server.getCurrPartition().toArray(new Character[0])) {
            if (c < alphabeticallyFirst) {
                alphabeticallyFirst = c;
            }
        }
        return alphabeticallyFirst;
    }
}
