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
            serverAddress = args[0]; // server address from cmdline
            nPort = Integer.parseInt(args[1]); // n_port
            if (!(nPort > 0)) {
                throw new Exception("invalid n_port");
            }
            reqCode = Integer.parseInt(args[2]); // req_code
            for (int i = 3; i < args.length; i++) {
                msgQueue.add(args[i]); // push message to message queue
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Input Error: number of inputs does not match");
            return;
        }
        catch (Exception e) {
            System.out.println("Input Error: " + e);
            return;
        }

        ClientUtil client = new ClientUtil(serverAddress, nPort, reqCode, msgQueue); // create ClientUtil object
        client.run(); // run client
    }
}