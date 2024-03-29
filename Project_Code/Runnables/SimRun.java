package Runnables;

import Components.*;
import Components.Nodes.Host;
import Components.Nodes.Router;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SimRun implements Runnable {

    private SimUtil simUtil;  
    private List<Thread> nodeThreads;
    private List<HostRun> hostRuns;
    private List<RouterRun> routerRuns;
    private List<String> dropInList;
    private String mode;
    private int simTime;

    public SimRun(SimUtil simUtil, String mode, int simTime){
        this.simUtil = simUtil;
        this.mode = mode;
        this.nodeThreads = new ArrayList<Thread>();
        this.hostRuns = new ArrayList<HostRun>();
        this.routerRuns = new ArrayList<RouterRun>();
        this.dropInList = Arrays.asList("h12", "h22");
        this.simTime = simTime;
    }

    @Override
    public void run() {

        // create threads
        for (Router router : this.simUtil.geRouterList()){
            RouterRun rRun = new RouterRun(router, this.simUtil);
            this.nodeThreads.add(new Thread(rRun));
            this.routerRuns.add(rRun);
        }
        for (Host host : this.simUtil.getHostList()){
            boolean found = false;
            HostRun hRun = null;
            for (String hid : this.dropInList){
                if (host.getId().equals(hid)){
                    found = true;
                    hRun = new HostRun(host, this.simUtil, this.mode, true);
                    break;
                }
            }
            if (!found){
                hRun = new HostRun(host, this.simUtil, this.mode, false);
            }
            this.nodeThreads.add(new Thread(hRun));
            this.hostRuns.add(hRun);
        }

        // start threads
        for (Thread thread : this.nodeThreads){
            thread.start();
        }

        int saveCount = 0;
        int timeElps = 0;

        while (timeElps < this.simTime){
            timeElps = (int) this.simUtil.getTime(System.currentTimeMillis()) / 1000;
            System.out.println("Time: " + timeElps);
            System.out.println("-----------------");

            int totDrop = this.simUtil.routerCheck(timeElps);
            this.simUtil.ArrivalCheck(timeElps, totDrop);
            // this.simUtil.routerCheck(timeElps);

            if (saveCount == 10){
                System.out.println("[SIMRUN] data saved to file");
                this.simUtil.saveFile();
                saveCount = 0;
            }
            saveCount ++;
            System.out.println("-----------------");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        for(HostRun hRun : this.hostRuns){
            hRun.stopThread();
        }
        for(RouterRun rRun : this.routerRuns){
            rRun.stopThread();
        }

        System.out.println("[SIMRUN] exits");

    }
    
}
