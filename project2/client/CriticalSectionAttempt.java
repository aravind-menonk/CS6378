package client;
/*
 * Class to track the stats when the client enters the critical section
 */
public class CriticalSectionAttempt {
    private int attemptNumber;
    private int requestsSent;
    private int grantsReceived;
    private long timeElapsed;
    private int messagesToEnter;

    public CriticalSectionAttempt(){
        this.attemptNumber = 0;
        this.requestsSent = 0;
        this.grantsReceived = 0;
        this.timeElapsed = 0;
        this.messagesToEnter = 0;
    }

    public int getMessagesToEnter() {
        return messagesToEnter;
    }

    public void setMessagesToEnter(
            int messagesToEnter) {
        this.messagesToEnter = messagesToEnter;
    }

    public int getRequestsSent() {
        return requestsSent;
    }
    public void setRequestsSent(
            int requestsSent) {
        this.requestsSent = requestsSent;
    }

    public synchronized void incrementRequestsSent(){
        this.requestsSent++;
    }

    public int getGrantsReceived() {
        return grantsReceived;
    }
    public void setGrantsReceived(
            int grantsReceived) {
        this.grantsReceived = grantsReceived;
    }

    public synchronized void incrementGrantsRecevied(){
        this.grantsReceived++;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }
    public void setTimeElapsed(
        long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }
    public void setAttemptNumber(
            int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public void print(){
        System.out.println("For attempt number : " + (this.attemptNumber + 1) + ", Messages sent : " + this.requestsSent 
            + ". Messages received to enter CS : " + this.messagesToEnter + ". Time elapsed : " + this.timeElapsed);
    }

}
