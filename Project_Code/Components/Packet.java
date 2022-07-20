package Components;

public class Packet {
    private String id;
    private String[] route;
    private String sourceId;
    private String destId;
    private long timeSent;
    private int routeIdx;

    public Packet(){
        this("", new String[0], "", "");
    }
    
    public Packet(String id, String[] route, String sourceId, String destId) {
        this.id = id;
        this.route = route;
        this.sourceId = sourceId;
        this.destId = destId;
        this.timeSent = 0;
        this.routeIdx = 0;
    }

    public String getId(){
        return this.id;
    }

    public String getNextId(){
        return this.route[this.routeIdx];
    }

    public String getsourceId(){
        return this.sourceId;
    }

    public String getDestId(){
        return this.destId;
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
