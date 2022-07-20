package Components;

import java.util.Map;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.CSVWriter;
import java.io.File;

public class SimUtil {
    // generate pkt, record queue size, compute queuing delay
    private List<Host> hostList;
    private List<Router> routerList;
    private List<String> hostIds;
    private Map<String, List<String>> topology;
    private int pktCnt = 0;
    private final long startTime;
    private Map<String, List<String>> destMap;
    private Map<String, List<String[]>> qSizeRec;

    public SimUtil(Map<String, List<String>> topology, List<String> hostIds, List<String> routerIds, Map<String, List<String>> destMap){
        this.topology = topology;
        this.hostList = new ArrayList<Host>();
        this.routerList = new ArrayList<Router>();
        this.hostIds = hostIds;
        this.createNodes(hostIds, routerIds);
        this.pktCnt = 0;
        this.destMap = destMap;
        this.startTime = System.currentTimeMillis();
        this.qSizeRec = new HashMap<String, List<String[]>>();
        for (Router router : this.routerList){
            this.qSizeRec.put(router.getId(), new ArrayList<String[]>());
        }
    }

    public long getTime(long curTime){
        return curTime - this.startTime;
    }

    public List<String> getDestMap(String hostId){
        return this.destMap.get(hostId);
    }

    public List<String> getHostIds(){
        return this.hostIds;
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
        String pktId = Integer.toString(this.pktCnt);
        Packet pkt = this.generatePkt(pktId, sourId, destId);
        this.pktCnt ++;
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
        
        if (sourId.equals("h1") || sourId.equals("h2") || sourId.equals("h3")){
            return new String[] {"r1", "r2", destId};
        } else {
            return new String[] {"r2", "r1", destId};
        }

        // return new String[] {"r1", destId};
    }

    public Packet generatePkt(String pktId, String sourId, String destId){
        String[] route = this.routing(sourId, destId);
        Packet pkt = new Packet(pktId, route, sourId, destId);
        return pkt;
    }

    public void changeRate(String hostId, int rate){
        try{
            Host host = (Host) this.getNode(hostId);
            host.setRate(rate);
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    public void recQueSize(){
        for (Router router : this.routerList){
            int timeElps = (int) this.getTime(System.currentTimeMillis()) / 1000;
            int qSize = router.getQSize();
            String rid = router.getId();
            System.out.println("Time: " + timeElps + ", Router ID: " + rid + ", Queue size: " + qSize);
            this.qSizeRec.get(rid).add(new String[] {Integer.toString(timeElps), Integer.toString(qSize)});
        }
    }

    public void ArrivalCheck(){
        for (Host host : this.hostList){
            System.out.println("Host ID: " + host.getId() + ", packet arraived: ");
            if (host.getQSize() > 0) {
                List<Packet> queue = host.getQueue();
                for (Packet pkt : queue){
                    System.out.println("ID: " + pkt.getId());
                    this.changeRate(pkt.getsourceId(), 0); // increase source rate
                }
                host.clearQueue();
            } else {
                System.out.println("None");
            }
        }
    }

    public void writeFile(String filePath, List<String[]> records, String[] header){
        try{
            File file = new File(filePath);
            FileWriter writer = new FileWriter(file, true);
            CSVWriter csvWriter = new CSVWriter(writer);
            if (file.createNewFile()){
                csvWriter.writeNext(header);
            }
            csvWriter.writeAll(records);
            csvWriter.close();
        } catch (IOException e){
            System.out.println("Error when writing files");
            e.printStackTrace();
        }
    }
    
    public void saveQSize(){
        for (String rid : this.qSizeRec.keySet()){
            String fPath = "./" + rid + "_qSize.csv";
            String[] header = new String[] {"time", "q_size"};
            this.writeFile(fPath, this.qSizeRec.get(rid), header);
        }
    }


}
