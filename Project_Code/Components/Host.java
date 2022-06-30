package Components;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class Host extends Node{

    private int windSize;
    private boolean flowCtrl = false;
    private int pktCount = 0;

    public Host(String id, List<Node> neighbours, Queue<Packet> pktQueue, ReentrantLock qLock, 
    int windSize, boolean flowCtrl) {
        super(id, neighbours, pktQueue, qLock);
        this.windSize = windSize;
        this.flowCtrl = flowCtrl;
    }

    @Override
    public void transmit(String nextId, Packet pkt){
        if (flowCtrl) {
            // flow control not implemented
            System.out.println("flow control not implemented");
            super.transmit(nextId, pkt);
        } else {
            super.transmit(nextId, pkt);
        }
    }

    public void sendPkt(Packet pkt){
        String nextId = pkt.getRoute()[pkt.getRouteIdx()];
        pkt.setTimeSent(System.nanoTime());
        pkt.incRouteIdx();
        this.transmit(nextId, pkt);
    }



    
    

}