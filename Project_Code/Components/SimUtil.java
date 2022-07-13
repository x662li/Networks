package Components;

import java.util.Map;


import java.util.List;
import java.util.ArrayList;

public class SimUtil {
    // generate pkt, record queue size, compute queuing delay
    private List<Host> hostList;
    private List<Router> routerList;
    private Map<String, List<String>> topology;
    private int pktId;

    public SimUtil(Map<String, List<String>> topology, List<String> hostIds, List<String> routerIds){
        this.topology = topology;
        this.hostList = new ArrayList<Host>();
        this.routerList = new ArrayList<Router>();
        this.createNodes(hostIds, routerIds);
        this.pktId = 0;
    }

    public List<Host> getHostList(){
        return this.hostList;
    }

    public List<Router> geRouterList(){
        return this.routerList;
    }

    public Node getNode(String nodeId){
        for (Host host : this.hostList){
            if (host.getId().equals(nodeId)){
                return host;
            }
        }
        for (Router router : this.routerList){
            if (router.getId().equals(nodeId)){
                return router;
            }
        }
        return null;
    }

    public void createNodes(List<String> hostIds, List<String> routerIds){
        
        for(Map.Entry<String, List<String>> rec : topology.entrySet()){
            
            String nodeId = rec.getKey();
            boolean found = false;

            for (String hid : hostIds){
                if (nodeId.equals(hid)){
                    this.hostList.add(new Host(nodeId, rec.getValue()));
                    System.out.println("Host added, id: " + nodeId);
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (String rid : routerIds){
                    if (nodeId.equals(rid)){
                        this.routerList.add(new Router(nodeId, rec.getValue()));
                        System.out.println("Router added, id: " + nodeId);
                        break;
                    }
                }
            }
        }
    }

    public boolean load_pkt (String sourId, String destId){
        Packet pkt = this.generatePkt(this.pktId, sourId, destId);
        this.pktId ++;
        for (Host host : this.hostList){
            if (host.getId().equals(sourId)){
                host.addLoader(pkt);
                return true;
            }
        }
        return false;
    } 

    // routing algorithm
    public String[] routing(String sourId, String destId){
        // replace with path finding
        
        String[] route = {"r1", destId};
        return route;
    }

    public Packet generatePkt(int pktId, String sourId, String destId){
        String[] route = this.routing(sourId, destId);
        Packet pkt = new Packet(pktId, route, sourId, destId);
        return pkt;
    }

    public void recQueSize(){
        for (Router router : this.routerList){
            System.out.println("Router ID: " + router.getId() + ", Queue size: " + router.getQSize());
        }
    }

    public void compDelay(){
        for (Host host : this.hostList){
            System.out.println("Host ID: " + host.getId() + ", packet arraived: ");
            if (host.getQSize() > 0) {
                List<Packet> queue = host.getQueue();
                for (Packet pkt : queue){
                    System.out.println("ID: " + pkt.getId());
                }
            } else {
                System.out.println("None");
            }
        }
    }



}
