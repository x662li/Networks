package Runnables;

import Components.*;
import Components.Nodes.Host;
import Components.Nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HostRun implements Runnable {

    private Host host;
    private SimUtil simUtil;
    private String mode;
    private int pktCnt;
    private boolean dropIn;
    private boolean exit;

    public HostRun(Host host, SimUtil simUtil, String mode, boolean dropIn){
        this.host = host;
        this.simUtil = simUtil;
        this.mode = mode;
        this.pktCnt = 0;
        this.dropIn = dropIn;
        this.exit = false;
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

    public void stopThread(){
        this.exit = true;
    }

    @Override
    public void run() {
        if (this.mode.equals("manual")){
            while (!this.exit){
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
            int dropInCnt = 0;
            while(!this.exit) {
                // if (this.dropIn){
                //     System.out.println("[HOSTRUN] drop-in host id: " + this.host.getId() + " start");
                //     System.out.flush();
                // }
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
                    // sleep 1 - 5 sec randomly
                    int sleepTime;
                    if (this.dropIn){
                        if (dropInCnt < 10){
                            sleepTime = 500;
                            dropInCnt ++;
                        } else {
                            dropInCnt = 0;
                            sleepTime = (30 + (int)(Math.random() * 30)) * 1000;
                        }
                    } else {
                        sleepTime = (1 + (int)(Math.random() * 5)) * 1000;
                    }
                    // System.out.println("[HOSTRUN] id: " + this.host.getId() + " sleep time: " + sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("[HOSTRUN] id: " + this.host.getId() + " exit");
        }
    }
    
}
