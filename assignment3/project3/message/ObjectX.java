package message;
/*
 * Class for storing the version number, Replicated units and the Distinguised sites.
 */
public class ObjectX implements java.io.Serializable {
    private int versionNumber;
    private int replicatedUnits;
    private Character distinguishedSite;

    public ObjectX(){
        this.distinguishedSite = null;
        this.replicatedUnits = 0;
        this.versionNumber = 0;
    }
    public ObjectX(int replicatedUnits, Character distinguishedSite){
        this.versionNumber = 1;
        this.replicatedUnits = replicatedUnits;
        this.distinguishedSite = distinguishedSite;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(
            int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getReplicatedUnits() {
        return replicatedUnits;
    }

    public void setReplicatedUnits(
            int replicatedUnits) {
        this.replicatedUnits = replicatedUnits;
    }

    public Character getDistinguishedSite() {
        return distinguishedSite;
    }
    
    public void setDistinguishedSite(
            Character distinguishedSite) {
        this.distinguishedSite = distinguishedSite;
    }
}
