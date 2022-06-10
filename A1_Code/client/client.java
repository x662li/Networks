package client;

import java.util.ArrayList;
import java.util.List;

public class client {

    public static void main(String[] args) {

        String serverAddress;
        int nPort;
        int reqCode;
        List<String> msgQueue = new ArrayList<String>();
        try{
            serverAddress = args[0];
            if ((!serverAddress.equals("localhost"))  && (!serverAddress.equals("ubuntu2004-002.student.cs.uwaterloo.ca")) && (!serverAddress.equals("ubuntu2004-004.student.cs.uwaterloo.ca"))) {
                throw new Exception("invalid server address");
            }
            nPort = Integer.parseInt(args[1]);
            if (!(nPort > 0)) {
                throw new Exception("invalid n_port");
            }
            reqCode = Integer.parseInt(args[2]);
            for (int i = 3; i < args.length; i++) {
                msgQueue.add(args[i]);
            }


        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Input Error: number of inputs does not match");
            return;
        }
        catch (Exception e) {
            System.out.println("Input Error: " + e);
            return;
        }

        ClientUtil client = new ClientUtil(serverAddress, nPort, reqCode, msgQueue);
        client.run();
    }
}