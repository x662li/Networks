package Components;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

// Host, send packet, receive packet
public class Host extends Node{
    
    private Queue<Packet> pkt_loader; // packets waiting to be sent
    private Lock loader_lock;
    private final Condition loader_empty;

    public Host(String nodeId, List<String> neighbours){
        super("host", nodeId, neighbours);
        this.pkt_loader = new LinkedList<Packet>();
        this.loader_lock = new ReentrantLock();
        this.loader_empty = loader_lock.newCondition();
    }

    public Queue<Packet> getLoader(){
        try{
            loader_lock.lock();
            return this.pkt_loader;
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when getting loader");
            e.printStackTrace();
            return null;
        } finally {
            this.loader_lock.unlock();
        }
    }

    public void addLoader(Packet pkt){
        loader_lock.lock();
        try{
            this.pkt_loader.add(pkt);
            this.loader_empty.signal();
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when adding to loader");
            e.printStackTrace();
        } finally {
            this.loader_lock.unlock();
        }  
    }

    public Packet popLoader(){
        loader_lock.lock();
        try{
            if (this.pkt_loader.isEmpty()){
                this.loader_empty.await();
            }
            return this.pkt_loader.remove();
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when poping from loader");
            e.printStackTrace();
            return null;
        } finally {
            this.loader_lock.unlock();
        }
    }


}
