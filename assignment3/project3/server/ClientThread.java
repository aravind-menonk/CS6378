package server;

import java.io.*;
import java.net.*;
import message.Message;
import message.ObjectX;

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

    @Override
	public void run(){
        try{
            while(true){
                Message message = Message.receiveMessage(this.clientIp, server.getiInputStreampMap());
                //update self
                if(message.getObject() == null){
                    System.out.println("Received update request from client.");
                    ObjectX ob = server.getObject();
                    //check if can update
                    if(server.canUpdate()){
                       ob.setVersionNumber(ob.getVersionNumber() + 1);
                       ob.setReplicatedUnits(server.getCurrPartition().size());
                       if(server.getCurrPartition().size() % 2 == 0){
                            ob.setDistinguishedSite(getDistinguishedSite());
                       }else{
                            ob.setDistinguishedSite(null);
                       }
                       server.setObject(ob);
                    }
                    //Send to all servers in the partition
                    Message m = new Message(server.getId(), ob);
                    for(Character serverName: server.getCurrPartition()){
                        if(serverName.equals(this.server.getName())){
                            continue;
                        }
                        Message.sendMessage(m, server.getNetworkPartitions().getServerNameMap().get(serverName), server.getoOutputStreamMap());
                    }
                }else{
                    System.out.println("Received update request from server " + server.getNetworkPartitions().getServerName(message.getSourceId()));
                    if(server.canUpdate()){
                        server.setObject(message.getObject());
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
