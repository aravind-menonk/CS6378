package message;

import java.io.*;
import java.util.*;

public class Message implements java.io.Serializable{
    public enum MessageType {
        UPDATE,
        COMMIT,
        VOTE_REQUEST,
        VOTE_RESPONSE,
        ABORT
    }

    private String sourceId;
    private MessageType messageType;
    private ObjectX object;

    public Message(MessageType type, String sourceId, ObjectX object){
        this.messageType = type;
        this.sourceId = sourceId;
        this.object = object;
    }

    public String getSourceId() {
        return sourceId;
    }
    public void setSourceId(
            String sourceId) {
        this.sourceId = sourceId;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(
            MessageType messageType) {
        this.messageType = messageType;
    }
    public ObjectX getObject() {
        return object;
    }
    public void setObject(
            ObjectX object) {
        this.object = object;
    }

    public static void sendMessage(Message message, String destId, Map<String, DataOutputStream> outputStreamMap){
        try{
            //System.out.println("Sending....." + destId);
            DataOutputStream outputStream = outputStreamMap.get(destId);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            //System.out.println("Sent....");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Message receiveMessage(String source, Map<String, DataInputStream> inputStreampMap){
        try{
            DataInputStream inputStream = inputStreampMap.get(source);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Message message = (Message) objectInputStream.readObject();
            //System.out.println("Received message from  " +  source + " " + message.getMessageType().toString());
            return message;
        }catch(Exception e){
            //Ignore
        }
        return null;
    }
    
}
