package Runnables;

import Components.*;

public class HostRun implements Runnable {

    private Host host;
    private SimUtil simUtil;

    public HostRun(Host host, SimUtil simUtil){
        this.host = host;
        this.simUtil = simUtil;
    }

    @Override
    public void run() {
        while (true){
            Packet pkt = host.popLoader();
            Node nextNode = simUtil.getNode(pkt.getNextId());
            host.transmit(pkt, nextNode);
        }
        
    }
    
}
