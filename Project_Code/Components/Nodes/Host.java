package Components.Nodes;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Components.Packet;

import java.util.concurrent.locks.Condition;

// Host, send packet, receive packet
public class Host extends Node{
    
    private Queue<Packet> pkt_loader; // packets waiting to be sent
    private Lock loaderLock;
    private final Condition loaderEmpty;
    private int sendRate;
    private Lock rateLock;

    public Host(String nodeId, List<String> neighbours){
        super("host", nodeId, neighbours);
        this.pkt_loader = new LinkedList<Packet>();
        this.loaderLock = new ReentrantLock();
        this.loaderEmpty = loaderLock.newCondition();
        this.sendRate = 1;
        this.rateLock = new ReentrantLock();
    }

    public int getRate(){
        try{
            rateLock.lock();
            return this.sendRate;
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when getting rate");
            e.printStackTrace();
            return -1;
        } finally {
            this.rateLock.unlock();
        }
    }

    public void setRate(int mode){
        try{
            rateLock.lock();
            if (mode == 1){
                this.sendRate = 1;
            } else {
                this.sendRate += 1;
            }
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when setting rate");
            e.printStackTrace();
        } finally {
            this.rateLock.unlock();
        }
    }

    public Queue<Packet> getLoader(){
        try{
            loaderLock.lock();
            return this.pkt_loader;
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when getting loader");
            e.printStackTrace();
            return null;
        } finally {
            this.loaderLock.unlock();
        }
    }

    public void addLoader(Packet pkt){
        loaderLock.lock();
        try{
            this.pkt_loader.add(pkt);
            this.loaderEmpty.signal();
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when adding to loader");
            e.printStackTrace();
        } finally {
            this.loaderLock.unlock();
        }  
    }

    public Packet popLoader(){
        loaderLock.lock();
        try{
            if (this.pkt_loader.isEmpty()){
                this.loaderEmpty.await();
            }
            return this.pkt_loader.remove();
        } catch (Exception e) {
            System.out.println("Host id: " + this.getId() + " Exception when poping from loader");
            e.printStackTrace();
            return null;
        } finally {
            this.loaderLock.unlock();
        }
    }

    @Override
    public boolean transmit(Packet pkt, Node nextNode){
        pkt.setTimeSent(System.currentTimeMillis());
        return super.transmit(pkt, nextNode);
    }


}
