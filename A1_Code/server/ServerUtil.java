package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import java.lang.StringBuilder;

public class ServerUtil {
    private int nPort; // negotiation port for TCP
    private int rPort; // random port for UDP
    private int reqCode; // request code for authentication
    private Socket tcpSocket; // TCP socket
    private DatagramSocket udpSocket; // UDP socket
    private BufferedReader input; // TCP buffer reader
    private PrintWriter output; // TCP print writter
    // getter for nPort    
    public int getNPort() {
        return this.nPort;
    }
    // getter for rPort
    public int getRPort() {
        return this.rPort;
    }
    // setter for nPort
    public void setNPort(int portNum) {
        this.nPort = portNum;
    }
    // setter for rPort
    public void setRPort(int portNum) {
        this.rPort = portNum;
    }
    // constructor
    public ServerUtil(int reqCode){
        this.reqCode = reqCode;
    }
    // reverse string input from client
    public String ReverseString(String origStr){
        StringBuilder output = new StringBuilder();
        output.append(origStr);
        return output.reverse().toString();
    }
    // start server by creating server socket
    public ServerSocket startServer() {
        Random rand = new Random();
        ServerSocket servSocket = null;
        boolean validPort = false;
        do {
            int nPort = 1024 + rand.nextInt(65536 - 1024); // generate n_port
            try{
                servSocket = new ServerSocket(nPort); // create server socket at n_port
                validPort = true;
                this.nPort = nPort;
                System.out.println("SERVER_PORT=" + this.nPort); // print n_port
            } catch (BindException be){
            } catch (Exception e) {
                System.out.println("Server Error: " + e);
                return null;
            }
        } while(!validPort);
        return servSocket;
    }
    // create UDP socket
    public boolean createUDP() {
        Random rand = new Random();
        boolean validPort = false;
        do {
            int rPort = 1024 + rand.nextInt(65536 - 1024); // generate r_port
            try {
                this.udpSocket = new DatagramSocket(rPort, InetAddress.getLocalHost()); // create new UDP socket
                validPort = true;
                this.rPort = rPort;
                //System.out.println("UDP connected initiated at r_port: " + this.rPort);
            } catch (BindException be) {
            } catch (Exception e) {
                System.out.println(e);
                return validPort;
            }
        } while(!validPort);
        return true;
    }
    // TCP input output
    public String tcpIO(String task, String msg) {
        String out = "";
        try{
            if (task.equals("init")) {
                this.input = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream())); // buffer reader initiate
                this.output = new PrintWriter(this.tcpSocket.getOutputStream(), true); // print writer initiate
                out = "IO initiated";
            } else if(task.equals("output")) {
                output.println(msg); // output message from print writer
                out = "msg sent";
            } else if(task.equals("input")) {
                out = input.readLine(); // read message from buffer reader
            } else if(task.equals("close")) {
                this.input.close(); // close input
                this.output.close(); // close output
                out = "IO closed";
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("TCP IO Error: " + e);
        }
        return out;
    }
    // UDP input output
    public HashMap<String, Object> udpIO(String task, HashMap<String, Object> sendPacket) {
        HashMap<String, Object> outMap = new HashMap<>(); // initialize message dict
        try{
            if(task.equals("input")) {
                byte[] buffer = new byte[1024]; // initialize buffer to be 1024 byte
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // create packet object
                this.udpSocket.receive(packet); // receive packet from socket
                outMap.put("address", packet.getAddress()); // construct message dict
                outMap.put("port", packet.getPort());
                byte[] msgCopy = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), msgCopy, 0, packet.getLength()); // obtain msg with its actual length other than 1024
                outMap.put("msg", new String(msgCopy));
            } else if(task.equals("output")) {
                InetAddress address = (InetAddress) sendPacket.get("address"); // retrive client address from request
                int port = (int) sendPacket.get("port"); // retrive client port
                String msg = (String) sendPacket.get("msg");
                byte[] buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port); // create packet
                this.udpSocket.send(packet); // send packet to socket
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("UDP IO Error: " + e);
        }
        return outMap;
    }
    // run method for server
    public int run() {
        ServerSocket servSocket = startServer(); // initiate server at n_port
        while(true) {
            try {
                this.tcpSocket = servSocket.accept(); // wait for client
                tcpIO("init", null); // init TCP IO
                int clientReqCode = Integer.parseInt(tcpIO("input", null)); // check req_code
                if (clientReqCode != this.reqCode) {
                    tcpIO("output", "deny"); // req_code not match, send "deny"
                    tcpIO("close", "");
                } else {
                    tcpIO("output", "allow"); // req_code matches, send "allow"
                    if(createUDP()) { // init UDP socket
                        tcpIO("output", Integer.toString(this.rPort)); // send r_port
                        String msg;
                        while (true) {
                            HashMap<String, Object> mapReceived = udpIO("input", new HashMap<String, Object>()); // receive string
                            msg = (String) mapReceived.get("msg");
                            if (msg.equals("EXIT")) { // if recieve exit code, close UDP socket
                                // System.out.println("EXIT code received!");
                                this.udpSocket.close();
                                break;
                            }
                            // System.out.println("Message Received: " + msg);
                            String returnString = ReverseString(msg); // reverse message
                            mapReceived.put("msg", returnString); 
                            udpIO("output", mapReceived); // send reversed message
                        }
                    }
                    tcpIO("close", null); // exit code received, close tcp IO
                    this.tcpSocket.close(); // close tcp socket, ready for next client
                }
            } catch (Exception e) {
                System.out.println("Server Error: " + e);
            }
        }
    }

}