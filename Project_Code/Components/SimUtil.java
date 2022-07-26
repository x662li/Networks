package Components;

import java.util.Map;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.opencsv.CSVWriter;

import Components.Nodes.Host;
import Components.Nodes.Node;
import Components.Nodes.Router;

import java.io.File;

public class SimUtil {
    // generate pkt, record queue size, compute queuing delay
    private List<Host> hostList;
    private List<Router> routerList;
    private List<String> hostIds;
    private Map<String, List<String>> topology;
    private int pktCnt;
    private final long startTime;
    private Map<String, List<String>> destMap;
    private Map<String, List<String[]>> qSizeRec;
    private List<String[]> avgDelayRec;
    private List<String[]> dropRateRec; 
    private int totPkt;
    private int pktArrived;

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
        this.avgDelayRec = new ArrayList<String[]>();
        this.dropRateRec = new ArrayList<String[]>();
        this.totPkt = 0;
        this.pktArrived = 0;
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

        // test symmetrical
        // if (Arrays.asList("h1", "h2", "h3", "h4").contains(sourId)){
        //     return new String[] {"r1", "r2", destId};
        // } else {
        //     return new String[] {"r2", "r1", destId};
        // }

        // test core
        List<String> r1Lan = Arrays.asList("h11", "h12");
        List<String> r2Lan = Arrays.asList("h21", "h22");
        List<String> r3Lan = Arrays.asList("h31", "h32");
        List<String> r4Lan = Arrays.asList("h41", "h42");
        if (r1Lan.contains(sourId)){
            if (r2Lan.contains(destId)){
                return new String[] {"r1", "r2", destId};
            }
            if (r3Lan.contains(destId)){
                return new String[] {"r1", "r3", destId};
            }
            if (r4Lan.contains(destId)){
                return new String[] {"r1", "r4", destId};
            }
        }
        if (r2Lan.contains(sourId)){
            if (r1Lan.contains(destId)){
                return new String[] {"r2", "r1", destId};
            }
            if (r3Lan.contains(destId)){
                return new String[] {"r2", "r3", destId};
            }
            if (r4Lan.contains(destId)){
                return new String[] {"r2", "r4", destId};
            }
        }
        if (r3Lan.contains(sourId)){
            if (r1Lan.contains(destId)){
                return new String[] {"r3", "r1", destId};
            }
            if (r2Lan.contains(destId)){
                return new String[] {"r3", "r2", destId};
            }
            if (r4Lan.contains(destId)){
                return new String[] {"r3", "r4", destId};
            }
        }
        if (r4Lan.contains(sourId)){
            if (r1Lan.contains(destId)){
                return new String[] {"r4", "r1", destId};
            }
            if (r2Lan.contains(destId)){
                return new String[] {"r4", "r2", destId};
            }
            if (r3Lan.contains(destId)){
                return new String[] {"r4", "r3", destId};
            }
        }
        return new String[] {"invalid"};
        
    }

    public Packet generatePkt(String pktId, String sourId, String destId){
        String[] route = this.routing(sourId, destId);
        Packet pkt = new Packet(pktId, route, sourId, destId);
        this.totPkt ++;
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

    public int routerCheck(int timeElps){
        int totDrop = 0;
        for (Router router : this.routerList){
            int qSize = router.getQSize();
            String rid = router.getId();
            double avgQSize = router.getAvgQSize();
            // double loss = router.getLoss();
            totDrop += router.getDropCount();
            router.resetDropCount();
            System.out.println("router ID: " + rid + ", queue size: " + qSize);
            this.qSizeRec.get(rid).add(new String[] {Integer.toString(timeElps), Integer.toString(qSize), Double.toString(avgQSize)});
        }
        return totDrop;
    }

    public void ArrivalCheck(int timeElps ,int totDrop){
        List<Long> delays = new ArrayList<Long>();
        for (Host host : this.hostList){
            // System.out.println("Host ID: " + host.getId() + ", packet arraived: ");
            if (host.getQSize() > 0) {
                List<Packet> queue = host.getQueue();
                for (Packet pkt : queue){
                    // System.out.println("ID: " + pkt.getId());
                    delays.add(pkt.getDelay());
                    this.pktArrived ++;
                }
                host.clearQueue();
            } else {
                // System.out.println("None");
            }
        }
        // compute avg delay for this round
        if (delays.size() > 0){
            Long sumDelay = (long) 0;
            for (Long dly : delays){
                sumDelay += dly;
            }
            Long avgDelay = sumDelay / delays.size();
            this.avgDelayRec.add(new String[] {Integer.toString(timeElps), Long.toString(avgDelay)});
            double dropRate = (double)totDrop / (double)(totDrop + delays.size());
            this.dropRateRec.add(new String[] {Integer.toString(timeElps), Double.toString(dropRate)});
            System.out.println("totDrop: " + totDrop + ", delay size: " + delays.size());
            System.out.println("avg delay: " + avgDelay);
            System.out.println("packet dropping rate: " + dropRate);
        } else {
            Long avgDelay = (long) 0;
            this.avgDelayRec.add(new String[] {Integer.toString(timeElps), Long.toString(avgDelay)});
            double dropRate = 0;
            if (totDrop > 0){
                dropRate = totDrop/(totDrop + delays.size());
            }
            this.dropRateRec.add(new String[] {Integer.toString(timeElps), Double.toString(dropRate)});
            System.out.println("avg delay: " + avgDelay);
            System.out.println("packet dropping rate: " + dropRate);
        }
        System.out.println("tot pkt generated: " + this.totPkt + ", tot pkt arrived: " + this.pktArrived);
    }

    public void writeFile(String filePath, List<String[]> records, String[] header){
        try{
            File file = new File(filePath);
            FileWriter writer;
            CSVWriter csvWriter;
            if (!file.exists()){
                writer = new FileWriter(file, true);
                csvWriter = new CSVWriter(writer);
                System.out.println("new file: " + filePath + " created, add header");
                csvWriter.writeNext(header);
            } else {
                writer = new FileWriter(file, true);
                csvWriter = new CSVWriter(writer);
            }
            csvWriter.writeAll(records);
            csvWriter.close();
        } catch (IOException e){
            System.out.println("Error when writing files");
            e.printStackTrace();
        }
    }
    
    public void saveFile(){
        for (String rid : this.qSizeRec.keySet()){
            String qfPath = "./data/" + rid + "_qSize.csv";
            String[] header = new String[] {"time", "q_size", "avg_q_size"};
            this.writeFile(qfPath, this.qSizeRec.get(rid), header);
            this.qSizeRec.get(rid).clear();
        }
        String delayPath = "./data/avg_delay.csv";
        String dropPath = "./data/drop_rate.csv";
        String[] headerDelay = new String[] {"time", "avg_delay"};
        String[] headerDrop = new String[] {"time", "drop_rate"};
        this.writeFile(delayPath, this.avgDelayRec, headerDelay);
        this.writeFile(dropPath, this.dropRateRec, headerDrop);
        this.avgDelayRec.clear();
        this.dropRateRec.clear();
    }


}
