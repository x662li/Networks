import java.util.Scanner;

import Components.*;
import Runnables.SimRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args){
        
        System.out.println("Welcome to Mike's network simulator!");
        
        // configure network based on user input
        Map<String, List<String>> topology = new HashMap<String, List<String>>();
        List<String> hostIds = new ArrayList<String>();
        List<String> routerIds = new ArrayList<String>();
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("Please input topology: <node type> <node id> <neighbour1 id> <neighbour2 id> ..., and 'finish'");
            while (true){
                String user_input = input.nextLine();
                if (user_input.equals("finish")) break;
                String[] topo_rec = user_input.split(" ");
                String nodeType = topo_rec[0];
                String nodeId = topo_rec[1];
                List<String> neighbours = Arrays.asList(Arrays.copyOfRange(topo_rec, 2, topo_rec.length));
                if (nodeType.equals("host")) {
                    hostIds.add(topo_rec[1]);
                } else if (nodeType.equals("router")) {
                    routerIds.add(topo_rec[1]);
                } else {
                    System.out.println("in correct type");
                    continue;
                }

                System.out.print("Record: node id: " + nodeId + ", neighbours: ");
                for (String item : neighbours){
                    System.out.print(item + ' ');
                }
                System.out.print("\n");
                
                topology.put(nodeId, neighbours);
            }

            System.out.println("please specify simulation mode, (manual or auto)");
            String mode = input.nextLine();

            // create simulation utility
            SimUtil simUtil = new SimUtil(topology, hostIds, routerIds, null);

            // start simRun thread (monitor queue delay, clear sink)
            SimRun simRun = new SimRun(simUtil, mode);
            Thread simThread = new Thread(simRun);
            simThread.start();
            System.out.println("Simulation start...");

            // auto mode: host generate packets automatically
            

            // manual mode: user input for packets
            System.out.println("To generate packet, please enter: <source id> <destination id>");
            while (true){
                String input_str = input.nextLine();
                if (input_str.equals("exit")) break;
                String[] pkt_info = input_str.split(" ");
                System.out.println("load packet, from: " + pkt_info[0] + " to: " + pkt_info[1]);
                simUtil.load_pkt(pkt_info[0], pkt_info[1]);
            }

            System.out.println("Simulation ends");

        } catch (Exception e) {
            System.out.println("Error in Main:");
            e.printStackTrace();
        } finally {
            // terminate all threads
        }

    }
}
