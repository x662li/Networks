package Components;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;

public class Node {
    private String id;
    private List<Node> neighbours;
    protected Queue<Packet> pktQueue;
    protected Lock qLock;
    protected Condition notEmpty;

    public Node(String id, List<Node> neighbours, Queue<Packet> pktQueue, Lock qLock){
        this.id = id;
        this.neighbours = neighbours;
        this.pktQueue = pktQueue;
        this.qLock = qLock;
        this.notEmpty = qLock.newCondition();
    }

    public String getId(){
        return this.id;
    }

    public List<Node> getNeighbours(){
        return this.neighbours;
    }

    public Queue<Packet> getPktQueue(){
        return this.pktQueue;
    }

    public void pushQueue(Packet pkt) {
        this.pktQueue.add(pkt);
    }

    public Lock getQLock() {
        return this.qLock;
    }

    public Node findNbr(String nodeId){
        for (final Node nbr : this.neighbours){
            if (nbr.getId().equals(nodeId)){
                return nbr;
            }
        }
        return null;
    }

    public void transmit(String destId, Packet pkt){
        try{
            Node destNode = this.findNbr(destId);
            if (destNode == null){
                throw new Exception("Cannot find node with id " + destId);
            } else {
                destNode.getQLock().lock();
                try{
                    destNode.pushQueue(pkt);
                } finally {
                    destNode.getQLock().unlock();
                }
            }
        } catch (Exception e){
            System.out.println("Transmission Error: " + e);
        }
    }

    public void readQueue() {
        this.qLock.lock();
        try{
            if (this.pktQueue.isEmpty()){
                System.out.println("packet queue is empty");
            } else {
                for (final Packet pkt : this.pktQueue){
                    System.out.println("Packet ID: " + pkt.getId() + ", from: " + pkt.getSenderId());
                }
            } 
        } finally {
            this.qLock.unlock();
        }
    }

}
