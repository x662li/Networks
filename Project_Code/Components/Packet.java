package Components;

public class Packet {
    private int id;
    private String[] route;
    private String senderId;
    private String receiverId;
    private long timeSent;
    private int routeIdx;

    public Packet(int id, String[] route, String senderId, String receiverId) {
        this.id = id;
        this.route = route;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timeSent = 0;
        this.routeIdx = 0;
    }

    public int getId(){
        return this.id;
    }

    public String[] getRoute(){
        return this.route;
    }

    public String getSenderId(){
        return this.senderId;
    }

    public String getReceiverID(){
        return this.receiverId;
    }

    public void setTimeSent(long timeSent){
        this.timeSent = timeSent;
    }

    public int getRouteIdx(){
        return this.routeIdx;
    }
    
    public void incRouteIdx(){
        if (this.routeIdx < this.route.length - 1){
            this.routeIdx ++;
        }
    }
    

}
