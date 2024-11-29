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

public class LinkedList {
   
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
