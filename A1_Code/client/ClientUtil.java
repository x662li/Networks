package client;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientUtil {

    private String serverAddress;
    private int nPort;
    private int rPort;
    private int reqCode;
    private List<String> msgQueue;
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private BufferedReader input;
    private PrintWriter output;

    public int getNPort() {
        return this.nPort;
    }

    public int getRPort() {
        return this.rPort;
    }

    public void setNPort(int portNum) {
        this.nPort = portNum;
    }

    public void setRPort(int portNum) {
        this.rPort = portNum;
    }

    public ClientUtil(String serverAddress, int nPort, int reqCode, List<String> msgQueue) {
        this.serverAddress = serverAddress;
        this.nPort = nPort;
        this.reqCode = reqCode;
        this.msgQueue = msgQueue;
    }

    public String tcpIO(String task, String msg) {
        String out = "";
        try{
            if (task.equals("init")) {
                this.input = new BufferedReader(new InputStreamReader(this.tcpSocket.getInputStream()));
                this.output = new PrintWriter(this.tcpSocket.getOutputStream(), true);
            } else if(task.equals("input")) {
                out = input.readLine();
                System.out.println(out);
            } else if(task.equals("output")) {
                output.println(msg);
            } else if(task.equals("close")) {
                this.input.close();
                this.output.close();
                this.tcpSocket.close();
                out = "TCP socket closed";
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("TCP IO Error: " + e);
        }
        return out;
    }

    public String udpIO(String task, String msg) {
        String out = "";
        try {
            byte[] buffer = new byte[1024];
            if(task.equals("input")){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.udpSocket.receive(packet);
                out = new String(buffer, 0, packet.getLength());
            } else if(task.equals("output")) {
                buffer = msg.getBytes();
                InetAddress address = InetAddress.getByName(this.serverAddress);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, this.rPort);
                this.udpSocket.send(packet);
            } else {
                throw new Exception("Error: invalid task name");
            }
        } catch (Exception e) {
            System.out.println("UDP IO Error: " + e);
        }
        return out;
    }

    public int run() {
        try{
            this.tcpSocket = new Socket(this.serverAddress, this.nPort);
            tcpIO("init", null);
            tcpIO("output", Integer.toString(this.reqCode));
            if (tcpIO("input", "").equals("allow")){
                this.rPort = Integer.parseInt(tcpIO("input", ""));
            } else {
                System.out.println("Authentication failed, exit");
                return 1;
            }

            this.udpSocket = new DatagramSocket();
            for (String msg: msgQueue) {
                udpIO("output", msg);
                if (msg.equals("EXIT")) {
                    this.udpSocket.close();
                    break;
                }
                System.out.println(udpIO("input", ""));
            }
            System.out.println(tcpIO("close", null));

        } catch (Exception e) {
            System.out.println("Client Error: " + e);
        }
        return 0;
    }

}