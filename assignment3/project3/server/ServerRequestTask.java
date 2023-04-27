package server;

import message.Message;
import message.Message.MessageType;

public class ServerRequestTask implements Runnable{
    private String serverName;
    private ThreadSafeInteger numServersReplied;
    private Message message;
    private ClientThread clientThread;

    public ServerRequestTask(String serverName, ThreadSafeInteger numServersReplied, Message message, ClientThread clientThread) {
        this.serverName = serverName;
        this.numServersReplied = numServersReplied;
        this.message = message;
        this.clientThread = clientThread;
    }

    @Override
    public void run() {
        System.out.println("Sending vote request to " + clientThread.server.getNetworkPartitions().getServerName(serverName) + " "  + serverName);
        try {
            Message.sendMessage(message, serverName, clientThread.server.getoOutputStreamMap());
            Message m = Message.receiveMessage(serverName, this.clientThread.server.getoInputStreampMap());
            if(m.getMessageType().equals(MessageType.VOTE_RESPONSE)){
                Character name = this.clientThread.server.getNetworkPartitions().getServerName(m.getSourceId());
                System.out.println("Recieved vote response from " + name);
                numServersReplied.incrementAndGet();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }


}
