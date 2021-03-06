
import Components.*;
import Runnables.SimRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    
    public static void main(String[] args) throws InterruptedException{

        System.out.println("entering testing mode");

        // configure topology
        Map<String, List<String>> topology = new HashMap<String, List<String>>();
        List<String> hostIds = new ArrayList<String>();
        List<String> routerIds = new ArrayList<String>();
        Map<String, List<String>> destMap = new HashMap<String, List<String>>();
        // List<List<String>> routes = new ArrayList<List<String>>();

        hostIds.addAll(Arrays.asList("h1", "h2" , "h3", "h4", "h5", "h6", "h7", "h8"));
        routerIds.addAll(Arrays.asList("r1", "r2"));

        topology.put("h1", Arrays.asList("r1"));
        topology.put("h2", Arrays.asList("r1"));
        topology.put("h3", Arrays.asList("r1"));
        topology.put("h4", Arrays.asList("r1"));

        topology.put("r1", Arrays.asList("h1", "h2", "h3", "h4", "r2"));

        topology.put("h5", Arrays.asList("r2"));
        topology.put("h6", Arrays.asList("r2"));
        topology.put("h7", Arrays.asList("r2"));
        topology.put("h8", Arrays.asList("r2"));

        topology.put("r2", Arrays.asList("r1", "h5", "h6", "h7", "h8"));

        destMap.put("h1", Arrays.asList("h5", "h6", "h7", "h8"));
        destMap.put("h2", Arrays.asList("h5", "h6", "h7", "h8"));
        destMap.put("h3", Arrays.asList("h5", "h6", "h7", "h8"));
        destMap.put("h4", Arrays.asList("h5", "h6", "h7", "h8"));

        destMap.put("h5", Arrays.asList("h1", "h2", "h3", "h4"));
        destMap.put("h6", Arrays.asList("h1", "h2", "h3", "h4"));
        destMap.put("h7", Arrays.asList("h1", "h2", "h3", "h4"));
        destMap.put("h8", Arrays.asList("h1", "h2", "h3", "h4"));

        // routes.add(Arrays.asList("h1", "r1", "r2", "h4"));
        // routes.add(Arrays.asList("h1", "r1", "r2", "h4"));
        // routes.add(Arrays.asList("h1", "r1", "r2", "h4"));
        
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
