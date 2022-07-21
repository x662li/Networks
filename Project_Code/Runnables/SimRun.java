package Runnables;

import Components.*;

import java.util.List;
import java.util.ArrayList;

public class SimRun implements Runnable {

    private SimUtil simUtil;  
    private List<Thread> nodeThreads;
    private String mode;

    public SimRun(SimUtil simUtil, String mode){
        this.simUtil = simUtil;
        this.mode = mode;
        this.nodeThreads = new ArrayList<Thread>();
    }

    @Override
    public void run() {

        // create threads
        for (Router router : this.simUtil.geRouterList()){
            RouterRun rRun = new RouterRun(router, this.simUtil);
            this.nodeThreads.add(new Thread(rRun));
        }
        for (Host host : this.simUtil.getHostList()){
            HostRun hRun = new HostRun(host, this.simUtil, this.mode);
            this.nodeThreads.add(new Thread(hRun));
        }

        // start threads
        for (Thread thread : this.nodeThreads){
            thread.start();
        }

        int saveCount = 0;

        while (true){
            int timeElps = (int) this.simUtil.getTime(System.currentTimeMillis()) / 1000;
            System.out.println("Time: " + timeElps);
            System.out.println("-----------------");
            int totDrop = this.simUtil.routerCheck(timeElps);
            this.simUtil.ArrivalCheck(timeElps, totDrop);
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

    }
    
}
