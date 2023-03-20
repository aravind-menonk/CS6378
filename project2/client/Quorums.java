package client;

import java.util.*;

public class Quorums {
    private Set<String> quorum1;
    private Set<String> quorum2;
    private Set<String> quorum3;
    private Set<String> quorum4;
    private Set<String> quorum5;
    private Set<String> quorum6;
    private Set<String> quorum7;
    private Set<String> quorum8;
    private Set<String> quorum9;
    private Set<String> quorum10;
    private Set<String> quorum11;
    private Set<String> quorum12;
    private Set<String> quorum13;
    private Set<String> quorum14;
    private Set<String> quorum15;

    /*
         *  S1 S2 S4 Quorum1
            S1 S2 S5 Quorum2
            S1 S4 S5 Quorum3

            S1 S3 S6 Quorum4
            S1 S3 S7 Quorum5
            S1 S6 S7 Quorum6

            S2 S4 S3 S6 Quorum7
            S2 S4 S3 S7 Quorum8
            S2 S4 S6 S7 Quorum9

            S2 S5 S3 S7 Quorum10
            S2 S5 S2 S6 Quorum11
            S2 S5 S6 S7 Quorum12

            S4 S5 S3 S6 Quorum13
            S4 S5 S3 S7 Quorum14
            S4 S5 S6 S7 Quorum15
         */

    public Quorums(){
        quorum1 = new HashSet<>();
        quorum2 = new HashSet<>();
        quorum3 = new HashSet<>();
        quorum4 = new HashSet<>();
        quorum5 = new HashSet<>();
        quorum6 = new HashSet<>();
        quorum7 = new HashSet<>();
        quorum8 = new HashSet<>();
        quorum9 = new HashSet<>();
        quorum10 = new HashSet<>();
        quorum11 = new HashSet<>();
        quorum12 = new HashSet<>();
        quorum13 = new HashSet<>();
        quorum14 = new HashSet<>();
        quorum15 = new HashSet<>();
    }

    public void configureQuorums(){
        this.quorum1.add("10.176.69.32");
        this.quorum1.add("10.176.69.33");
        this.quorum1.add("10.176.69.35");

        this.quorum2.add("10.176.69.32");
        this.quorum2.add("10.176.69.33");
        this.quorum2.add("10.176.69.36");

        this.quorum3.add("10.176.69.32");
        this.quorum3.add("10.176.69.35");
        this.quorum3.add("10.176.69.36");

        this.quorum4.add("10.176.69.32");
        this.quorum4.add("10.176.69.34");
        this.quorum4.add("10.176.69.37");

        this.quorum5.add("10.176.69.32");
        this.quorum5.add("10.176.69.34");
        this.quorum5.add("10.176.69.38");

        this.quorum6.add("10.176.69.32");
        this.quorum6.add("10.176.69.37");
        this.quorum6.add("10.176.69.38");

        this.quorum7.add("10.176.69.33");
        this.quorum7.add("10.176.69.35");
        this.quorum7.add("10.176.69.34");
        this.quorum7.add("10.176.69.37");

        this.quorum8.add("10.176.69.33");
        this.quorum8.add("10.176.69.35");
        this.quorum8.add("10.176.69.34");
        this.quorum8.add("10.176.69.38");

        this.quorum9.add("10.176.69.33");
        this.quorum9.add("10.176.69.35");
        this.quorum9.add("10.176.69.37");
        this.quorum9.add("10.176.69.38");

        this.quorum10.add("10.176.69.33");
        this.quorum10.add("10.176.69.36");
        this.quorum10.add("10.176.69.34");
        this.quorum10.add("10.176.69.38");

        this.quorum11.add("10.176.69.33");
        this.quorum11.add("10.176.69.36");
        this.quorum11.add("10.176.69.34");
        this.quorum11.add("10.176.69.37");

        this.quorum12.add("10.176.69.33");
        this.quorum12.add("10.176.69.36");
        this.quorum12.add("10.176.69.37");
        this.quorum12.add("10.176.69.38");

        this.quorum13.add("10.176.69.35");
        this.quorum13.add("10.176.69.36");
        this.quorum13.add("10.176.69.34");
        this.quorum13.add("10.176.69.37");

        this.quorum14.add("10.176.69.35");
        this.quorum14.add("10.176.69.36");
        this.quorum14.add("10.176.69.34");
        this.quorum14.add("10.176.69.38");

        this.quorum15.add("10.176.69.35");
        this.quorum15.add("10.176.69.36");
        this.quorum15.add("10.176.69.37");
        this.quorum15.add("10.176.69.38");
    } 

    public void printQuorums(Map<String, String> serverNameMap){
        System.out.println("Quorum 1: ");
        for(String serverName: quorum1){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 2: ");
        for(String serverName: quorum2){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 3: ");
        for(String serverName: quorum3){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 4: ");
        for(String serverName: quorum4){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 5: ");
        for(String serverName: quorum5){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 6: ");
        for(String serverName: quorum6){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 7: ");
        for(String serverName: quorum7){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 8: ");
        for(String serverName: quorum8){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 9: ");
        for(String serverName: quorum9){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 10: ");
        for(String serverName: quorum10){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 11: ");
        for(String serverName: quorum11){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 12: ");
        for(String serverName: quorum12){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 13: ");
        for(String serverName: quorum13){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 14: ");
        for(String serverName: quorum14){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println("\nQuorum 15: ");
        for(String serverName: quorum15){
            System.out.print(serverNameMap.get(serverName) + "  ");
        }
        System.out.println();
    }

    public boolean checkInQuorums(Set<String> repliedServers){
        return repliedServers.containsAll(quorum1) || repliedServers.containsAll(quorum2) 
            || repliedServers.containsAll(quorum3) || repliedServers.containsAll(quorum4) || 
            repliedServers.containsAll(quorum5) || repliedServers.containsAll(quorum6) 
            || repliedServers.containsAll(quorum7) || repliedServers.containsAll(quorum8) || 
            repliedServers.containsAll(quorum9) || repliedServers.containsAll(quorum10) 
            || repliedServers.containsAll(quorum11) || repliedServers.containsAll(quorum12) || 
            repliedServers.containsAll(quorum13) || repliedServers.containsAll(quorum14) 
            || repliedServers.containsAll(quorum15);
    }
}
