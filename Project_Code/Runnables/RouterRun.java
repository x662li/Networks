package Runnables;

import Components.*;

public class RouterRun implements Runnable {

    private Router router;
    private SimUtil simUtil;

    public RouterRun(Router router, SimUtil simUtil){
        this.router = router;
        this.simUtil = simUtil;
    }

    @Override
    public void run() {
        while(true){
            Packet pkt = router.popQueue();
            Node nextNode = simUtil.getNode(pkt.getNextId());
            if (!router.transmit(pkt, nextNode)){
                // System.out.println("Router id: " + router.getId() + ", packet drop detected, change source transmission rate");
                simUtil.changeRate(pkt.getsourceId(), 1);
            }
            // System.out.println("router id: " + router.getId() + " queue length: " + router.getQSize());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    

    
    
}
