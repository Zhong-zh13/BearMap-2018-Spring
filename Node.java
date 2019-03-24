import java.util.*;

public class Node {
    private long id;
    private double lat;
    private double lon;
    HashMap<Long,Double> neighbours;
    private HashSet<String> WayBelongingTo = new HashSet<>();
    private String LocationName;
    boolean connected;

    public Node(long ID, double LON, double LAT){
        id = ID;
        lon = LON;
        lat = LAT;
        WayBelongingTo.add("unknown road");// default name of the node to be "unknown road"
        LocationName = "unknown location";
        neighbours = new HashMap<>();
        connected = false;
    }

    void addNeighbour(long v, double dist){
        neighbours.put(v,dist);
    }

    long id(){
        return this.id;
    }

    void addWayName(String e){
        WayBelongingTo.remove("unknown road");
        WayBelongingTo.add(e);
    }
    void setLocationName(String name){
        this.LocationName = name;
    }

    String getLocation(){
        return LocationName;
    }

    double lon(){
        return this.lon;
    }

    double lat(){
        return this.lat;
    }

    Set<Long> neighbour(){
        return this.neighbours.keySet();
    }
}
