package Runnables;

import Components.*;

import java.util.List;
import java.util.ArrayList;

public class SimRun implements Runnable {

    private SimUtil simUtil;  
    private List<Thread> nodeThreads;

    public SimRun(SimUtil simUtil){
        this.simUtil = simUtil;
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
            HostRun hRun = new HostRun(host, this.simUtil);
            this.nodeThreads.add(new Thread(hRun));
        }

        // start threads
        for (Thread thread : this.nodeThreads){
            thread.start();
        }

        while (true){
            System.out.println("-----------------");
            this.simUtil.recQueSize();
            this.simUtil.compDelay();
            System.out.println("-----------------");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    
}
