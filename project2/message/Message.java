package message;

import java.io.*;
import java.util.*;

public class Message implements java.io.Serializable, Comparable<Message>{
    public enum MessageType {
        REQUEST,
        RELEASE,
        GRANT,
        COMPLETE
    }

    private String sourceId;
    private MessageType messageType;
    private final long timestamp;

    public Message(MessageType messageType, String sourceId){
        this.messageType = messageType;
        this.sourceId = sourceId;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getMessageType(){
        return messageType;
    }

    public String getSourceId(){
        return sourceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Message other) {
        return Long.compare(timestamp, other.timestamp);
    }

    public static Message receiveMessage(String source, Map<String, DataInputStream> inputStreampMap){
        try{
            DataInputStream inputStream = inputStreampMap.get(source);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Message message = (Message) objectInputStream.readObject();
            System.out.println("Received message from  " +  source + " " + message.getMessageType().toString());
            return message;
        }catch(Exception e){
            //e.printStackTrace();
        }
        return null;
    }

    public static void sendMessage(Message message, String destId, Map<String, DataOutputStream> outputStreamMap){
        try{
            //System.out.println("Sending.....");
            DataOutputStream outputStream = outputStreamMap.get(destId);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            //System.out.println("Sent....");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
     * To delete a message from the queue if needed.
     */
    public static Message findRequestMessage(Message releaseMessage, Queue<Message> queue){
        if(queue.isEmpty()){
            return null;
        }
        //System.out.println("Iterating through the queue....");
        String sourceId = releaseMessage.getSourceId();
        Iterator<Message> iteratorVals = queue.iterator();
        while(iteratorVals.hasNext()){
            Message m = iteratorVals.next();
            if(m.sourceId.equals(sourceId)){
                return m;
            }
        }
        return null;
    }
}