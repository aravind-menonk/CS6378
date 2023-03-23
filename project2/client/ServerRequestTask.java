package client;

import java.util.*;

import message.Message;
import message.Message.MessageType;

public class ServerRequestTask implements Runnable {
        private String serverName;
        private Set<String> repliedServers;
        private Message message;
        private Client client;
        private int attemptNumber;

        public ServerRequestTask(String serverName, Set<String> repliedServers, Message message, Client client, int attemptNumber) {
            this.serverName = serverName;
            this.repliedServers = repliedServers;
            this.message = message;
            this.client = client;
            this.attemptNumber = attemptNumber;
        }

        public Client getClient(){
            return this.client;
        }

        @Override
        public void run() {
            // Send the request message to a server and receive the reply
            System.out.println("Sending request to " + client.serverNameMap.get(serverName) + " "  + serverName + " " + message.getTimestamp());
            try {
                Message.sendMessage(message, serverName, this.getClient().outputStreamMap);
                this.client.incrementMessagesSent();
                this.client.criticalSectionAttempt.get(attemptNumber).incrementRequestsSent();
                //wait for reply from the server
                Message m = Message.receiveMessage(serverName, this.getClient().inputStreampMap);
                this.client.incrementMessagesReceived();
                if(m.getMessageType().equals(MessageType.GRANT)){
                    System.out.println("Received grant from " + client.serverNameMap.get(serverName) + " " + serverName);
                    repliedServers.add(serverName);
                    this.client.criticalSectionAttempt.get(attemptNumber).incrementGrantsRecevied();
                }
            } catch (Exception e) {
                //ignore
            }
        }
    }