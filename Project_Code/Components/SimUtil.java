package Components;

import java.util.HashMap;
import java.util.List;

public class SimUtil {
    // generate pkt, record queue size, compute queuing delay
    private String mode;
    private List<Router> routerList;
    private HashMap<String, List<String>> topology;

    public SimUtil(String mode, List<Router>routerList, HashMap<String, List<String>> topology){
        this.mode = mode;
        this.routerList = routerList;
        this.topology = topology;
    }

    public String[] routing(String sourId, String destId){
        // replace with path finding
        String[] route = {"ID1", "ID2", "ID3"}; 
        return route;
    }

    public Packet generatePkt(String sourId, String destId){
        String[] route = this.routing(sourId, destId);
        int pktId = 1;
        Packet pkt = new Packet(pktId, route, sourId, destId);
        return pkt;
    }

    public void recQueSize(){
        for (Router router : this.routerList){
            router.getQLock().lock();
            System.out.println("Router ID: " + router.getId() + ", Queue size: " + router.getPktQueue().size());
            router.getQLock().unlock();
        }
    }

    public float compDelay(){
        // compute delay (RTT)
        return (float) 1.0;
    }

    // userinput 

}
