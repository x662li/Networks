package Runnables;

import Components.*;
import Components.Nodes.Node;
import Components.Nodes.Router;

public class RouterRun implements Runnable {

    private Router router;
    private SimUtil simUtil;
    private boolean exit;

    public RouterRun(Router router, SimUtil simUtil){
        this.router = router;
        this.simUtil = simUtil;
        this.exit = false;
    }

    public void stopThread(){
        this.exit = true;
    }

    @Override
    public void run() {
        while(!this.exit){
            Packet pkt = router.popQueue();
            Node nextNode = simUtil.getNode(pkt.getNextId());
            if (!router.transmit(pkt, nextNode)){
                // System.out.println("Router id: " + router.getId() + ", packet drop detected, change source transmission rate");
                simUtil.changeRate(pkt.getsourceId(), 1);
            } else {
                if (pkt.getDestId().equals(pkt.getNextId())){
                    this.simUtil.changeRate(pkt.getsourceId(), 0); // increase source rate
                    pkt.compDelay();
                }
            }
            // System.out.println("router id: " + router.getId() + " queue length: " + router.getQSize());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[ROUTERRUN] id: " + this.router.getId() + " exit");
    }
    

    
    
}
