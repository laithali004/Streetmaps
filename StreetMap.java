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

public class StreetMap {
   
    public static void main(String [] args) throws FileNotFoundException {
       
        long startTime = System.currentTimeMillis();
       
        //get the name of the file
        File mapData = new File(args[0]);
       
        if(args[0].equals("ur.txt")) {
            StreetMapGUI.thickLines = true;
        }
       
       
        //first scanner finds the number of intersections in the entire graph
        Scanner scan = new Scanner(mapData);
       
        int numIntersects = 0;
       
        while(scan.nextLine().startsWith("i")) {
            numIntersects++;
        }

        scan.close();
       
        String intersectionID;
        double lat, longitude;
        Intersection v;
       
        //scan2 scans through all the data
        Scanner scan2 = new Scanner(mapData);
       
        //create the Map
        Map map = new Map(numIntersects);
       
        String currentLine = scan2.nextLine();
       
        String [] info;

        //INSERTING INTERSECTIONS
        while(currentLine.startsWith("i")) {
           
            info = currentLine.split("\t");
           
            intersectionID = info[1];
            lat = Double.parseDouble(info[2]);
            longitude = Double.parseDouble(info[3]);
           
            //create the new Intersection
            v = new Intersection();
            v.distance = Integer.MAX_VALUE;
            v.IntersectionID = intersectionID;
            v.latitude = lat;
            v.longitude = longitude;
            v.flag = false;
           
            currentLine = scan2.nextLine();
           
            //add the new intersection into the map
            map.insert(v);
        }
       
        String roadID, int1, int2;
       
        Intersection w, x;
       
        double distance;
       
        //INSERTING ROADS
        while(currentLine.startsWith("r")) {
           
            info = currentLine.split("\t");
           
            roadID = info[1];
           
            int1 = info[2];
            int2 = info[3];
           
            w = Map.intersectLookup(int1);
            x = Map.intersectLookup(int2);
           
            distance = Map.roadDist(w, x);
           
            //create and add the new road
            map.insert(new Road(roadID, int1, int2, distance));
           
            if(scan2.hasNextLine() == false) {
                break;
            }
           
            currentLine = scan2.nextLine();
           
        }
       

        String fileName;
       
        //used for title of JFrame
        if(args[0].equals("ur.txt")) {
            fileName = "U of R Campus";
        }
        else {
            if(args[0].equals("monroe.txt")) {
                fileName = "Monroe County";
            }
            else {
                if(args[0].equals("nys.txt")) {
                    fileName = "New York State";
                }
                else {
                    fileName = "Map";
                }
            }
        }
       
        boolean showMap = false;
        boolean dijkstras = false;
        boolean mwst = false;
       
        //default directions
        String directionsStart = "i0";
        String directionsEnd = "i1";
       
        //checks the command line arguments
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-show")) {
                showMap = true;
            }
           
            if(args[i].equals("-directions")) {
                dijkstras = true;
               
                directionsStart = args[i+1];
                directionsEnd = args[i+2];
            }
           
            if(args[i].equals("-meridianmap")) {
                mwst = true;
            }
           
        }
       
        //if directions are needed
        if(dijkstras == true) {
           
            map.dijkstra(directionsStart);
           
            System.out.println("\nThe shortest path from " + directionsStart + " to " + directionsEnd + " is: ");
            System.out.println(Map.formPath(directionsEnd));
           
            System.out.println("Length of the path from " + directionsStart + " to " + directionsEnd + " is: " + Map.dijkstraPathLength() + " miles.");
        }
       
        if(mwst == true) {
           
            map.kruskals();
           
            System.out.println("\nRoads Taken to Create Minimum Weight Spanning Tree for " + fileName + ":\n");
           
            for(Road r : Map.MST) {
                System.out.println(r.roadID);
            }
           
        }
       
        //if the GUI is needed
        if(showMap == true) {
       
            JFrame frame = new JFrame("Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
           
            frame.getContentPane().add(new StreetMapGUI(Map.roads, Map.intersectionMap, Map.minLat, Map.maxLat, Map.minLong, Map.maxLong));
            frame.pack();
            frame.setVisible(true);
        }
       
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime-startTime;
       
        System.out.println("\n\nTime required to process map: " + elapsedTime/1000 + " seconds.");
       
       
        scan2.close();
    }

}


 class Map {
   
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
    class LinkedList {
   
        public int size;
        public Node head;
       
   
        public LinkedList() {
            head = new Node();
            size = 0;
        }
       
        //method that returns the size of the linked list
        public int size() {
            return size;
        }
       
        //finds weight between 2 intersections, current intersection of linked list and intersections adjacent to it
        public double findWeight(Intersection int2) {
   
            Edge x2 = head.edge;
           
           
            while(x2 != null) {
               
                if(x2.road.intersect1.equals(int2.IntersectionID) || x2.road.intersect2.equals(int2.IntersectionID)) {
                    return x2.road.distance;
                }
               
                x2 = x2.next;
            }
           
            return -1;
           
        }
       
        //Inserts an intersection into linked list
        public void insert(Intersection intersect) {
           
            if(head.intersection == null) {
                head.intersection = intersect;
            }
           
            size++;
        }
       
        public boolean connected(Intersection int2) {
           
            Edge x2 = head.edge;
           
            //travel down the linked list
            while(x2 != null) {
               
                if(x2.road.intersect1.equals(int2.IntersectionID) || x2.road.intersect2.equals(int2.IntersectionID)) {
                    return true;
                }
               
                x2 = x2.next;
            }
           
            return false;
           
        }
       
        //checks if 2 intersections are connected
        public boolean contains(Intersection i) {
           
            Node x = head;
           
            while(x != null) {
               
                if(x.intersection.equals(i)) {
                    return true;
                }
               
                x = x.next;
            }
           
            return false;
           
        }
       
        //inserts road into linkedlist
        public void insert(Road road) {
           
            Edge xEdge = new Edge();
            xEdge.road = road;
           
            //insert at the front of the list (after the head)
            xEdge.next = head.edge;
            head.edge = xEdge;
           
   
        }
   
    }
}
  class Edge {
   
    Road road;
    Edge next;

}
  class Intersection {
   
    String IntersectionID;
    double distance;
    double longitude;
    double latitude;
    boolean flag;
    Intersection path;

}
 class Node {
   
    Intersection intersection;
    Node next;
    Edge edge;

}
 class Road {
   
    String roadID;
    String intersect1;
    String intersect2;
    double distance;
   
    //constructor
    public Road(String road, String int1, String int2, double dist) {
        roadID = road;
        intersect1 = int1;
        intersect2 = int2;
        distance = dist;
    }

}
 class StreetMapGUI extends JPanel{
   
   
   
   
    public static ArrayList<Road> roads;
    public static HashMap<String, Intersection> intersectionMap;
    public static boolean thickLines = false;
   
    public static double minLat, minLong, maxLat, maxLong;
    public static double xScale, yScale;
   
    public StreetMapGUI(ArrayList<Road> roads, HashMap<String, Intersection> intersectMap, double minimumLat, double maximumLat, double minimumLong, double maximumLong) {
       
        StreetMapGUI.roads = roads;
        StreetMapGUI.intersectionMap = intersectMap;
       
        minLat = minimumLat;
        maxLat = maximumLat;
        minLong = minimumLong;
        maxLong = maximumLong;
       
        setPreferredSize(new Dimension(800, 800));
       
    }
   
    //paint component
    public void paintComponent(Graphics page) {
       
        //use 2D graphics to display lines with double values for coordinates
        Graphics2D page2 = (Graphics2D) page;
        super.paintComponent(page2);
       
        page2.setColor(Color.BLACK);
       
        //increase the thickness of the lines if the map is of the University of Rochester
        if(thickLines) {
            page2.setStroke(new BasicStroke(3));
        }
       
        //set the scales
        //adapted from Lab TAs
        xScale = this.getWidth() / (maxLong - minLong);
        yScale = this.getHeight() / (maxLat - minLat);
       
        Intersection int1, int2;
       
        double x1, y1, x2, y2;
       
        //GRAPHING ALL THE ROADS
        for(Road r : roads) {
           
            scale();
           
            int1 = intersectionMap.get(r.intersect1);
            int2 = intersectionMap.get(r.intersect2);
           
            x1 = int1.longitude;
            y1 = int1.latitude;
            x2 = int2.longitude;
            y2 = int2.latitude;
       
            page2.draw(new Line2D.Double((x1-minLong) * xScale, getHeight() - ((y1 - minLat) * yScale),
                    (x2-minLong) * xScale, getHeight() - ((y2 - minLat) * yScale)));
           
        }
       
        //GRAPHING THE DIRECTIONS USING DIJKSTRA'S ALGORITHM
        if(Map.dijkstraPath != null) {
           
            page2.setColor(Color.BLUE);
           
            for(int i = 0; i < Map.dijkstraPath.length - 1; i++) {
               
                x1 = Map.dijkstraPath[i].longitude;
                y1 = Map.dijkstraPath[i].latitude;
                x2 = Map.dijkstraPath[i+1].longitude;
                y2 = Map.dijkstraPath[i+1].latitude;
               
                page2.draw(new Line2D.Double((x1-minLong) * xScale, getHeight() - ((y1 - minLat) * yScale),
                        (x2-minLong) * xScale, getHeight() - ((y2 - minLat) * yScale)));

            }
           
           
        }
       
        //GRAPHING THE MERIDIAN MAP
        if(Map.MST != null) {
            for(Road r : Map.MST) {
               
                page2.setColor(Color.GREEN);
               
                int1 = intersectionMap.get(r.intersect1);
                int2 = intersectionMap.get(r.intersect2);
               
                x1 = int1.longitude;
                y1 = int1.latitude;
                x2 = int2.longitude;
                y2 = int2.latitude;
           
                page2.draw(new Line2D.Double((x1-minLong) * xScale, getHeight() - ((y1 - minLat) * yScale),
                        (x2-minLong) * xScale, getHeight() - ((y2 - minLat) * yScale)));
               
            }
        }
   
    }
   
    //METHOD USED TO RESCALE THE PANEL
    public void scale() {
       
        xScale = this.getWidth() / (maxLong - minLong);
        yScale = this.getHeight() / (maxLat - minLat);
       
    }
   
   

}