package Components;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Router extends Node {

    private QManager qManager;
    
    public Router(String nodeId, List<String> neighbours){
        super("router", nodeId, neighbours);
        this.qManager = new QManager();
    }

    @Override
    public boolean pushQueue(Packet pkt){
        // queue management
        if (this.queueManage()){
            return super.pushQueue(pkt);
        }
        return false;
    }

    public boolean queueManage(){
        return this.qManager.dropTail(this.getQSize());
    }
    
    
}
