package Components;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Router extends Node {

    private QManager qManager;
    private int sThres;
    private int lThres; // used by drop-tail

    public Router(String id, List<Node> neighbours, Queue<Packet> pktQueue, Lock qLock, 
    QManager qManager, int sThres, int lThres) {
        super(id, neighbours, pktQueue, qLock);
        this.qManager = qManager;
        this.sThres = sThres;
        this.lThres = lThres;
    }

    public void routePkt() throws InterruptedException {
        try{
            this.qLock.lock();
            while(this.pktQueue.isEmpty()){
                this.notEmpty.await();
            }
            Packet pkt = this.pktQueue.remove();
            this.qLock.unlock();
            // later might override this transmit
            super.transmit(pkt.getRoute()[pkt.getRouteIdx()], pkt);
            pkt.incRouteIdx();
        } catch (NoSuchElementException e){
            System.out.println("Router ID: " + this.getId() + " Queue Empty");
            this.qLock.unlock();
        }  
    }
    
}
