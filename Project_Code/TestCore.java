
import Components.*;
import Runnables.SimRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCore {
    
    public static void main(String[] args) throws InterruptedException{

        System.out.println("entering testing mode");

        // configure topology
        Map<String, List<String>> topology = new HashMap<String, List<String>>();
        List<String> hostIds = new ArrayList<String>();
        List<String> routerIds = new ArrayList<String>();
        Map<String, List<String>> destMap = new HashMap<String, List<String>>();

        hostIds.addAll(Arrays.asList("h11", "h12" , "h21", "h22", "h31", "h32", "h41", "h42"));
        routerIds.addAll(Arrays.asList("r1", "r2", "r3", "r4"));

        topology.put("h11", Arrays.asList("r1"));
        topology.put("h12", Arrays.asList("r1"));
        topology.put("h21", Arrays.asList("r2"));
        topology.put("h22", Arrays.asList("r2"));
        topology.put("h31", Arrays.asList("r3"));
        topology.put("h32", Arrays.asList("r3"));
        topology.put("h41", Arrays.asList("r4"));
        topology.put("h42", Arrays.asList("r4"));
        topology.put("r1", Arrays.asList("h11", "h12", "r2", "r3", "r4"));
        topology.put("r2", Arrays.asList("h21", "h22", "r1", "r3", "r4"));
        topology.put("r3", Arrays.asList("h31", "h32", "r1", "r2", "r4"));
        topology.put("r4", Arrays.asList("h41", "h42", "r1", "r2", "r3"));

        destMap.put("h11", Arrays.asList("h21", "h22", "h31", "h32", "h41", "h42"));
        destMap.put("h12", Arrays.asList("h21", "h22", "h31", "h32", "h41", "h42"));
        destMap.put("h21", Arrays.asList("h11", "h12", "h31", "h32", "h41", "h42"));
        destMap.put("h22", Arrays.asList("h11", "h12", "h31", "h32", "h41", "h42"));
        destMap.put("h31", Arrays.asList("h11", "h12", "h21", "h22", "h41", "h42"));
        destMap.put("h32", Arrays.asList("h11", "h12", "h21", "h22", "h41", "h42"));
        destMap.put("h41", Arrays.asList("h11", "h12", "h21", "h22", "h31", "h32"));
        destMap.put("h42", Arrays.asList("h11", "h12", "h21", "h22", "h31", "h32"));

        // mode for testing
        String mode = "auto";

        // create simulation tools
        SimUtil simUtil = new SimUtil(topology, hostIds, routerIds, destMap);

        // start simRun thread
        SimRun simRun = new SimRun(simUtil, mode, 200);
        Thread simThread = new Thread(simRun);
        simThread.start();
        System.out.println("test start...");
        simThread.join();

        

        

    }

}

