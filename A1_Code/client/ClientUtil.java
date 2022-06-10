package client;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientUtil {
    private String serverAddress; // server address
    private int nPort; // negotiation port for TCP
    private int rPort; // random port for UDP
    private int reqCode; // request code for authentication
    private List<String> msgQueue; // message queue to store messages
    private Socket tcpSocket; // TCP socket
    private DatagramSocket udpSocket; // UDP socket
    private BufferedReader input; // TCP buffer reader
    private PrintWriter output; // UDP print writer
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
    public ClientUtil(String serverAddress, int nPort, int reqCode, List<String> msgQueue) {
        this.serverAddress = serverAddress;
        this.nPort = nPort;
        this.reqCode = reqCode;
        this.msgQueue = msgQueue;
    }
    // TCP input output
    public String tcpIO(String task, String msg) {
        String out = "";
        try{
            if (task.equals("init")) {
                this.input = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream())); // initialize buffer reader
                this.output = new PrintWriter(this.tcpSocket.getOutputStream(), true); // initialize print writer
            } else if(task.equals("input")) {
                out = input.readLine(); // read from socket
                // System.out.println(out);
            } else if(task.equals("output")) {
                output.println(msg); // send to socket
            } else if(task.equals("close")) {
                this.input.close(); // close input
                this.output.close(); // close output
                out = "TCP socket closed";
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("TCP IO Error: " + e);
        }
        return out;
    }
    // UDP input output
    public String udpIO(String task, String msg) {
        String out = "";
        try {
            byte[] buffer = new byte[1024]; // initalize buffer with size 1024 byte
            if(task.equals("input")){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // create packet object
                this.udpSocket.receive(packet); // receive packet from socket
                out = new String(buffer, 0, packet.getLength());
            } else if(task.equals("output")) {
                buffer = msg.getBytes(); // store message to buffer
                InetAddress address = InetAddress.getByName(this.serverAddress); // get server address
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, this.rPort); // create packet with server address, r_port
                this.udpSocket.send(packet); // send packet
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("UDP IO Error: " + e);
        }
        return out;
    }
    // run method for client
    public int run() {
        try{
            this.tcpSocket = new Socket(this.serverAddress, this.nPort); // creat TCP socket
            tcpIO("init", null); // initialize TCP IO
            tcpIO("output", Integer.toString(this.reqCode)); // send req_code for authentication
            if (tcpIO("input", "").equals("allow")){
                this.rPort = Integer.parseInt(tcpIO("input", "")); // access allowed, receive r_port
            } else {
                System.out.println("req_code does not match, exit"); // authentication failed, close TCP socket
                this.tcpSocket.close();
                return 1;
            }
            this.udpSocket = new DatagramSocket(); // initialize UDP socket
            for (String msg: msgQueue) {
                udpIO("output", msg); // send message
                if (msg.equals("EXIT")) {
                    this.udpSocket.close(); // close UDP socket after exit code sent
                    break;
                }
                System.out.println(udpIO("input", "")); // print response
            }
            tcpIO("close", null); // close TCP IO
            this.tcpSocket.close(); // close TCP socket

        } catch (Exception e) {
            System.out.println("Client Error: " + e);
        }
        return 0;
    }

}