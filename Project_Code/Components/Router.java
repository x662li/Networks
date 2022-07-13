package Components;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Router extends Node {

    // private aqmAlgo;
    
    public Router(String nodeId, List<String> neighbours){
        super("router", nodeId, neighbours);
        // this aqmAlgo = aqmAlgo;
    }

    @Override
    public boolean pushQueue(Packet pkt){
        // queue management
        if (this.queueManage(3)){
            return super.pushQueue(pkt);
        }
        return false;
    }

    public boolean queueManage(int thres){
        if (this.getQSize() > thres){
            return false;
        } else {
            return true;
        }
    }
    
    
}
