import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JFrame;

public class Map {
   
    public HashMap<String, LinkedList> graph;
    public static int numIntersections;
   
    public static ArrayList<Road> roads;
    public static HashMap<String, Intersection> intersectionMap;
    public static PriorityQueue<Intersection> unknownIntersectionsHeap;
    public static PriorityQueue<Road> kruskalsRoads;
    public static HashMap<String, HashSet<String>> intersectionSets;
    public static ArrayList<Road> MST;
    public static Intersection [] dijkstraPath;

    public static double minLat, maxLat, minLong, maxLong;
   
    //constructor for the Map
    public Map(int numVertices) {
       
        graph = new HashMap<String, LinkedList>();

        numIntersections = numVertices;
        roads = new ArrayList<Road>();
        intersectionMap = new HashMap<String, Intersection>();

        //comparator used in the Heap of Intersections
        Comparator<Intersection> comparator = new Comparator<Intersection>() {

            @Override
            public int compare(Intersection i1, Intersection i2) {
               
                if(i1.distance < i2.distance) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
       
        //heap of Unknown Intersections
        unknownIntersectionsHeap = new PriorityQueue<Intersection>(numVertices, comparator);
       
        //comparator used in Heap of Roads
        Comparator<Road> comparator2 = new Comparator<Road>() {

            @Override
            public int compare(Road r1, Road r2) {
               
                if(r1.distance < r2.distance) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        };
       
        //heap of roads
        kruskalsRoads = new PriorityQueue<Road>(numVertices*3, comparator2);
       
        //set the minimum and maximum latitudes and longitudes to appropriate starting integer values
        minLat = minLong = Integer.MAX_VALUE;
        maxLat = maxLong = Integer.MIN_VALUE;

       
    }
   
    //method that returns the number of intersections in the graph
    public int size() {
        return graph.size();
    }
   
    //determines path for Dikstra's Algorithm
    public static String formPath(String endID) {
       
        //get the intersection that has endID as its ID
        Intersection x = intersectionMap.get(endID);
       
        //path will contain the order of the nodes from the end to the start vertex
        String [] path = new String[intersectionMap.size()];
       
        int counter = 0;
       
       
        while(x.path != null) {
            path[counter] = x.IntersectionID;
            x = x.path;
            counter++;
        }
       
        path[counter] = x.IntersectionID;
       
        int totalPath = 0;
       
        for(int i = 0; i < path.length; i++) {
            if(path[i] == null) {
                totalPath = i;
                break;
            }
        }
       
        //dijkstraPath is used to graph the directions
        dijkstraPath = new Intersection [totalPath];
       
        for(int i = 0; i < totalPath; i++) {
            dijkstraPath[i] = intersectionMap.get(path[i]);
        }
       
        String finalPath = "";
       
        for(int i = counter ; i > -1; i--) {
            finalPath = finalPath + path[i] + "\n";
        }
       
        return finalPath;
    }
   
    //method to determine the total distance required to travel between the intersections
    public static double dijkstraPathLength() {
       
        //converting form meters to miles
        return dijkstraPath[0].distance * 0.000621371;
    }
   
    //from Dijkstra's Algorithm to get smallest unknown vertex
    public static Intersection smallestUnknownVertex() {
       
        //get the smallest intersection from the heap of intersections
        Intersection x = unknownIntersectionsHeap.remove();
       
        return intersectionMap.get(x.IntersectionID);
       
    }
   
   
    public void createSet() {
       
        //instantiate the HashMap that maps IntersectionID to HashSet of vertices connected to that intersection
        intersectionSets = new HashMap<String, HashSet<String>>();
       
        HashSet<String> intersections;
       
        //iterate over all the entries in the graph
        Iterator<Entry<String, LinkedList>> iterator = graph.entrySet().iterator();
       
        while (iterator.hasNext()) {
            HashMap.Entry<String, LinkedList> pair = (HashMap.Entry<String, LinkedList>) iterator.next();
           
           
            intersections = new HashSet<String>();
           
           
            intersections.add(pair.getKey());
           
            intersectionSets.put(pair.getKey(), intersections);
           
        }
       
       
    }
   
    //determines roads that make MST
    public void kruskals() {

        //make all the HashSets for the intersections
        createSet();
       
        //arraylist that will hold all the roads in the mst
        MST = new ArrayList<Road>();
       
        Road currentRoad;
       
        HashSet<String> u;
        HashSet<String> v;
       
        while(kruskalsRoads.size() > 0) {
           
            //get the road with the shortest distance from the heap of all roads
            currentRoad = kruskalsRoads.remove();
           
            u = intersectionSets.get(currentRoad.intersect1);
            v = intersectionSets.get(currentRoad.intersect2);
           
            //if the sets are not the same, accept the edge
            if(!u.equals(v)) {
               
                MST.add(currentRoad);
               
                //union the two sets
                u.addAll(v);
               
                for(String intersectionID: u) {
                    intersectionSets.put(intersectionID, u);
                }
            }
        }
    }
   
    //determines paths followed to get from start to finsih in the shortest distance
    public void dijkstra(String intersectionID) {
       
        Intersection start = intersectionMap.get(intersectionID);
       
        unknownIntersectionsHeap.remove(start);
       
        start.distance = 0;
       
        unknownIntersectionsHeap.add(start);
       
        double weight;
       
        int numUnknownVertices = intersectionMap.size();
       
        while(numUnknownVertices > 0) {
           
            Intersection x = smallestUnknownVertex();
           
            x.flag = true;
            numUnknownVertices--;
           
            LinkedList currentVertex = graph.get(x.IntersectionID);
           
            Edge currentRoad = currentVertex.head.edge;
            Intersection currentIntersection;
           
            while(currentRoad != null) {
               
                if(currentRoad.road.intersect1.equals(x.IntersectionID)) {
                    currentIntersection = intersectionMap.get(currentRoad.road.intersect2);
                }
                else {
                    currentIntersection = intersectionMap.get(currentRoad.road.intersect1);
                }
               
                //if the intersection is unknown
                if(currentIntersection.flag == false) {
                   
                    //find the weight to get from the current vertex to its adjacent one
                    weight = findWeight(x, currentIntersection);
                   
                    if(x.distance + weight < currentIntersection.distance) {
                       
                        //update the intersection by removing it from the heap
                        unknownIntersectionsHeap.remove(currentIntersection);
                       
                        //changing the values
                        currentIntersection.distance = x.distance + weight;
                        currentIntersection.path = x;
                       
                        //and adding it back into the heap
                        unknownIntersectionsHeap.add(currentIntersection);
                    }
                }
                //get to the next edge in the linked list
                currentRoad = currentRoad.next;
            }
        }
    }
        //method to find the weight to travel between 2 connected intersections
    public double findWeight(Intersection int1, Intersection int2) {
       
        //get the linked list for the first intersection
        LinkedList x = graph.get(int1.IntersectionID);
       
        //call find weight on that linked list
        return x.findWeight(int2);
    }
   
    //checks if 2 intersections are connected
    public boolean connected(Intersection int1, Intersection int2) {
       
        //get the linked list for the first intersection
        LinkedList x = graph.get(int1.IntersectionID);
       
        //call connected on the linked list
        return x.connected(int2);
       
    }
   
    //inserts intersections into graph
    public void insert(Intersection i) {
       
       
        //continually finds and updates the minimum and maximum latitude and longitude
        if(i.latitude < minLat) {
            minLat = i.latitude;
        }
       
        if(i.latitude > maxLat) {
            maxLat = i.latitude;
        }
       
        if(i.longitude < minLong) {
            minLong = i.longitude;
        }
       
        if(i.longitude > maxLong) {
            maxLong = i.longitude;
        }
       
        //add the intersection into the HashMap of intersections
        intersectionMap.put(i.IntersectionID, i);
       
        //add the intersection into the heap of unknown intersections
        unknownIntersectionsHeap.add(i);
       
        LinkedList x = new LinkedList();
       
        x.insert(i);
       
        graph.put(i.IntersectionID, x);
    }
   
    //inserts roads into graph
    public void insert(Road e) {
       
        //gets the linked list for each intersection in the road
        LinkedList int1 = graph.get(e.intersect1);
        LinkedList int2 = graph.get(e.intersect2);
       
        //inserts the road into each linked list
        int1.insert(e);
        int2.insert(e);
       
        //adds the road to the heap of roads
        kruskalsRoads.add(e);
       
        //adds the road to the arrayList of all roads
        roads.add(e);
    }
   
    //method that returns intersection that correlates to intersectionID
    public static Intersection intersectLookup(String intersectID) {
       
        return intersectionMap.get(intersectID);
       
    }
   
    //method that calculates the distance between two intersectionw
    public static double roadDist(Intersection int1, Intersection int2) {
       
        return calcDist(int1.latitude, int1.longitude, int2.latitude, int2.longitude);
       
    }
   
    //method calculates the distance between two pairs of longitude and latitude
    public static double calcDist(double lat1, double long1, double lat2, double long2) {
       
        int earthRadius = 6371000;
       
        lat1 = Math.toRadians(lat1);
        long1 = Math.toRadians(long1);
        lat2 = Math.toRadians(lat2);
        long2 = Math.toRadians(long2);
       
        double changeLat = lat2-lat1;
        double changeLong = long2-long1;
       
        double a = (Math.sin(changeLat/2) * Math.sin(changeLat/2)) + (Math.cos(lat1) * Math.cos(lat2) * Math.sin(changeLong/2) * Math.sin(changeLong/2));
       
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
       
        return earthRadius * c;
       
    }
