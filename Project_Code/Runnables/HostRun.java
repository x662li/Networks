package Runnables;

import Components.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HostRun implements Runnable {

    private Host host;
    private SimUtil simUtil;
    private String mode;
    private int pktCnt;

    public HostRun(Host host, SimUtil simUtil, String mode){
        this.host = host;
        this.simUtil = simUtil;
        this.mode = mode;
        this.pktCnt = 0;
    }

    public List<Packet> pktGen(int rate, List<String> hostIds, Random rand){
        List<Packet> pktList = new ArrayList<Packet>();
        String hostName = host.getId();
        for (int i=0; i<rate; i++){
            String pktId = hostName + "-" + this.pktCnt;
            String desId = hostIds.get(rand.nextInt(hostIds.size()));
            pktList.add(simUtil.generatePkt(pktId, hostName, desId));
            this.pktCnt ++;
        }
        return pktList;
    }

    @Override
    public void run() {
        if (this.mode.equals("manual")){
            while (true){
                Packet pkt = host.popLoader();
                Node nextNode = simUtil.getNode(pkt.getNextId());
                if (!host.transmit(pkt, nextNode)){
                    // change transmission rate
                    System.out.println("Host id: " + host.getId() + ", packet drop detected, change transmission rate");
                    host.setRate(1);
                }
            }
        } else {
            List<String> hostIds = simUtil.getDestMap(host.getId());
            Random rand = new Random();
            while(true) {
                List<Packet> pktList = this.pktGen(host.getRate(), hostIds, rand);
                for (Packet pkt : pktList){
                    Node nextNode = simUtil.getNode(pkt.getNextId());
                    if (!host.transmit(pkt, nextNode)){
                        // change transmission rate
                        // System.out.println("Host id: " + host.getId() + ", packet drop detected, change transmission rate");
                        host.setRate(1);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
