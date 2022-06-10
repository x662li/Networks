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
                System.out.println("Server socket started at n_port: " + this.nPort); // print n_port
            } catch (BindException be){
                System.out.println("Port " + nPort + " in use, try another port.");
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
                this.udpSocket = new DatagramSocket(rPort, InetAddress.getLocalHost());
                validPort = true;
                this.rPort = rPort;
                System.out.println("UDP connected initiated at r_port: " + this.rPort);
            } catch (BindException be) {
                System.out.println("Port " + rPort + " already in use, try another port");
            } catch (Exception e) {
                System.out.println(e);
                return validPort;
            }
        } while(!validPort);
        return true;
    }

    public String tcpIO(String task, String msg) {
        String out = "";
        try{
            if (task.equals("init")) {
                this.input = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));
                this.output = new PrintWriter(this.tcpSocket.getOutputStream(), true);
                out = "IO initiated";
            } else if(task.equals("output")) {
                output.println(msg);
                out = "msg sent";
            } else if(task.equals("input")) {
                out = input.readLine();
            } else if(task.equals("close")) {
                this.input.close();
                this.output.close();
                //this.tcpSocket.close();
                out = "IO closed";
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("TCP IO Error: " + e);
        }
        return out;
    }

    public HashMap<String, Object> udpIO(String task, HashMap<String, Object> sendPacket) {
        HashMap<String, Object> outMap = new HashMap<>();
        try{
            if(task.equals("input")) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.udpSocket.receive(packet);
                outMap.put("address", packet.getAddress());
                outMap.put("port", packet.getPort());
                byte[] msgCopy = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), msgCopy, 0, packet.getLength()); // obtain msg with its actual length other than 1024
                outMap.put("msg", new String(msgCopy));
            } else if(task.equals("output")) {
                InetAddress address = (InetAddress) sendPacket.get("address");
                int port = (int) sendPacket.get("port");
                String msg = (String) sendPacket.get("msg");
                byte[] buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                this.udpSocket.send(packet);
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("UDP IO Error: " + e);
        }
        return outMap;
    }

    public int run() {
        ServerSocket servSocket = startServer();
        while(true) {
            try {
                this.tcpSocket = servSocket.accept();
                System.out.println(tcpIO("init", ""));
                int clientReqCode = Integer.parseInt(tcpIO("input", null));
                if (clientReqCode != this.reqCode) {
                    tcpIO("output", "deny");
                    tcpIO("close", "");
                } else {
                    tcpIO("output", "allow");
                    if(createUDP()) {
                        tcpIO("output", Integer.toString(this.rPort));
                        String msg;
                        while (true) {
                            HashMap<String, Object> mapReceived = udpIO("input", new HashMap<String, Object>());
                            msg = (String) mapReceived.get("msg");
                            if (msg.equals("EXIT")) {
                                System.out.println("EXIT code received!");
                                this.udpSocket.close();
                                break;
                            }
                            System.out.println("Message Received: " + msg);
                            String returnString = "From Server: " + ReverseString(msg);
                            mapReceived.put("msg", returnString);
                            udpIO("output", mapReceived);
                        }
                    }
                    System.out.println(tcpIO("close", null));
                }
            } catch (Exception e) {
                System.out.println("Server Error: " + e);
            }
        }
    }



}