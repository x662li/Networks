package Components;

import java.util.List;
// import java.util.Queue;
// import java.util.concurrent.locks.Lock;

public class Router extends Node {

    private QManager qManager;
    private int maxQSize;
    
    public Router(String nodeId, List<String> neighbours){
        super("router", nodeId, neighbours);
        this.qManager = new QManager(this.getId());
        this.maxQSize = 15;
    }

    @Override
    public boolean pushQueue(Packet pkt){
        // check hard limit
        if (this.getQSize() > this.maxQSize){
            return false;
        }
        // queue management
        if (this.congesCtrl()){
            return super.pushQueue(pkt);
        }
        return false;
    }

    public boolean congesCtrl(){
        // return this.qManager.RED(this.getQSize());
        return this.qManager.dropTail(this.getQSize());
    }
    
    
}
