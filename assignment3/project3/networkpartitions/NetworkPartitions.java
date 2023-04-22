package networkpartitions;

import java.util.*;

public class NetworkPartitions {
    private Map<Character, String> serverNameMap;

    public Map<Character, String> getServerNameMap() {
        return serverNameMap;
    }

    public void setServerNameMap(
            Map<Character, String> serverNameMap) {
        this.serverNameMap = serverNameMap;
    }

    public NetworkPartitions(){
        this.serverNameMap = new HashMap<>();
    }

    public void print(List<Set<Character>> partition){
        System.out.print("Partition is ");
        for(Set<Character> set: partition){
            for(Character c: set){
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    public List<Set<Character>> getPartitionOne(){
        List<Set<Character>> partitionOne = new ArrayList<>();
        Set<Character> element = new HashSet<>(Arrays.asList('A', 'B','C','D','E','F','G','H'));
        partitionOne.add(element);
        print(partitionOne);
        return partitionOne;
    }

    public List<Set<Character>> getPartitionTwo(){
        List<Set<Character>> partitionTwo = new ArrayList<>();
        Set<Character> element = new HashSet<>(Arrays.asList('A', 'B','C','D'));
        partitionTwo.add(element);
        element = new HashSet<>(Arrays.asList('E','F','G','H'));
        partitionTwo.add(element);
        print(partitionTwo);
        return partitionTwo;
    }

    public List<Set<Character>> getPartitionThree(){
        List<Set<Character>> partitionThree = new ArrayList<>();
        Set<Character> element = new HashSet<>(Arrays.asList('A'));
        partitionThree.add(element);
        element = new HashSet<>(Arrays.asList('B','C','D'));
        partitionThree.add(element);
        element = new HashSet<>(Arrays.asList('E','F','G'));
        partitionThree.add(element);
        element = new HashSet<>(Arrays.asList('H'));
        partitionThree.add(element);
        print(partitionThree);
        return partitionThree;
    }

    public List<Set<Character>> getPartitionFour(){
        List<Set<Character>> partitionFour = new ArrayList<>();
        Set<Character> element = new HashSet<>(Arrays.asList('B','C','D','E','F','G'));
        partitionFour.add(element);
        print(partitionFour);
        return partitionFour;
    }

    public void initialiseServerNames(){
        this.serverNameMap.put('A', "10.176.69.32");
        this.serverNameMap.put('B', "10.176.69.33");
        this.serverNameMap.put('C', "10.176.69.34");
        this.serverNameMap.put('D', "10.176.69.35");
        this.serverNameMap.put('E', "10.176.69.36");
        this.serverNameMap.put('F', "10.176.69.37");
        this.serverNameMap.put('G', "10.176.69.38");
        this.serverNameMap.put('H', "10.176.69.39");
    }

    public Character getServerName(String serverId){
        for(Character serverName: serverNameMap.keySet()){
            if(serverNameMap.get(serverName).equals(serverId)){
                return serverName;
            }
        }
        return null;
    }
}
