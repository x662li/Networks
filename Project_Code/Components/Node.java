package Components;

import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

// node defined in a topological level
public class Node {
    
    private String type;
    private String nodeId; // unique ID for nodes
    private List<String> neighbours; // neighbouring node IDs
    private Queue<Packet> pkt_buff; // loaded pkt before send
    private Lock buff_lock; // to lock queues
    private final Condition buff_empty;
    
    public Node(String type, String nodeId, List<String> neighbours){
        this.type = type;
        this.nodeId = nodeId;
        this.neighbours = neighbours;
        this.pkt_buff = new LinkedList<Packet>();
        this.buff_lock = new ReentrantLock();
        this.buff_empty = buff_lock.newCondition();
    }

    public String getType(){
        return this.type;
    }

    public String getId(){
        return this.nodeId;
    }
    
    public List<String> getNeighbours(){
        return this.neighbours;
    }

    public int getQSize(){
        this.buff_lock.lock();
        try{
            return this.pkt_buff.size();
        } catch (Exception e) {
            System.out.println("Node id: " + this.getId() + " Exception when getting queue");
            e.printStackTrace();
            return -1;
        } finally {
            this.buff_lock.unlock();
        }  
    }

    public List<Packet> getQueue(){
        this.buff_lock.lock();
        try{
            return new ArrayList<Packet>(this.pkt_buff);
        } catch (Exception e) {
            System.out.println("Node id: " + this.getId() + " Exception when getting queue");
            e.printStackTrace();
            return null;
        } finally {
            this.buff_lock.unlock();
        }  
    }

    public void pushQueue(Packet pkt){
        this.buff_lock.lock();
        try{
            this.pkt_buff.add(pkt);
            this.buff_empty.signal();
        } catch (Exception e) {
            System.out.println("Node id: " + this.getId() + " Exception when pushing queue");
            e.printStackTrace();
        } finally {
            this.buff_lock.unlock();
        }  
    }

    public Packet popQueue(){
        this.buff_lock.lock();
        try{
            if (this.pkt_buff.isEmpty()){
                this.buff_empty.await();
            }
            return this.pkt_buff.remove();   
        } catch (Exception e) {
            System.out.println("Node id: " + this.getId() + " Exception when poping queue");
            e.printStackTrace();
            return null;
        } finally {
            this.buff_lock.unlock();
        }
    }

    // return false if empty or error
    public boolean transmit(Packet pkt, Node nextNode) {
        try{
            pkt.incRouteIdx();
            nextNode.pushQueue(pkt);
            return true;
        } catch (Exception e) {
            System.out.println("Node id: " + this.getId() + " Exception when transmit");
            e.printStackTrace();
            return false;
        }
    }



    
}
