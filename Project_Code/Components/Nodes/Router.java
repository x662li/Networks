package Components.Nodes;

import java.util.List;
// import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import Components.Packet;
import Components.QManagers.*;

public class Router extends Node {

    private QManager qManager;
    private int dropCount = 0;
    private Lock dropCountLock;
    
    public Router(String nodeId, List<String> neighbours){
        super("router", nodeId, neighbours);

        this.qManager = new DropTail(this.getId());
        // this.qManager = new Red(this.getId());
        // this.qManager = new NnRed(this.getId());

        this.dropCountLock = new ReentrantLock();
    }

    public double getAvgQSize(){
        return this.qManager.getAvgQSize();
    }

    public void resetDropCount(){
        try{
            dropCountLock.lock();
            this.dropCount = 0;
        } catch (Exception e) {
            System.out.println("Router id: " + this.getId() + " Exception when setting avgRateCount");
            e.printStackTrace();
        } finally {
            this.dropCountLock.unlock();
        }
    }

    public void incDropCount(){
        try{
            dropCountLock.lock();
            this.dropCount ++;
        } catch (Exception e) {
            System.out.println("Router id: " + this.getId() + " Exception when setting avgRateCount");
            e.printStackTrace();
        } finally {
            this.dropCountLock.unlock();
        }
    }

    public int getDropCount(){
        try{
            dropCountLock.lock();
            return this.dropCount;
        } catch (Exception e) {
            System.out.println("Router id: " + this.getId() + " Exception when getting avgRateCount");
            e.printStackTrace();
            return -1;
        } finally {
            this.dropCountLock.unlock();
        }
    }

    @Override
    public boolean pushQueue(Packet pkt){
        // queue management
        if (this.congesCtrl()){
            return super.pushQueue(pkt);
        }
        this.incDropCount();
        return false;
    }

    public boolean congesCtrl(){
        return this.qManager.check(this.getQSize());
    }
    
    
}
