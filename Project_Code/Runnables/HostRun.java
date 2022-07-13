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
            if (!host.transmit(pkt, nextNode)){
                // change transmission rate
                System.out.println("Host id: " + host.getId() + ", packet drop detected, change transmission rate");
            }
        }
        
    }
    
}
