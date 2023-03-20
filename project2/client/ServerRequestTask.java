package client;

import java.util.*;

import message.Message;
import message.Message.MessageType;

public class ServerRequestTask implements Runnable {
        private String serverName;
        private Set<String> repliedServers;
        private Message message;
        private Client client;

        public ServerRequestTask(String serverName, Set<String> repliedServers, Message message, Client client) {
            this.serverName = serverName;
            this.repliedServers = repliedServers;
            this.message = message;
            this.client = client;
        }

        public Client getClient(){
            return this.client;
        }

        @Override
        public void run() {
            // Send the request message to the server and receive the reply
            System.out.println("Sending request to " + client.serverNameMap.get(serverName) + " "  + serverName);
            // Simulating server response delay
            try {
                Message.sendMessage(message, serverName, this.getClient().outputStreamMap);
                //wait for reply
                Message m = Message.receiveMessage(serverName, this.getClient().inputStreampMap);
                if(m.getMessageType().equals(MessageType.GRANT)){
                    System.out.println("Received grant from " + client.serverNameMap.get(serverName) + " " + serverName);
                    repliedServers.add(serverName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }