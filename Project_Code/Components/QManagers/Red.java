package Components.QManagers;

import java.lang.Math;

public class Red extends QManager{

    private int count;
    private double maxP;
    private int sThres;
    private int lThres;

    public Red(String routerId) {
        super(routerId);
        this.sThres = 10;
        this.lThres = 15;
        this.maxP = 0.4;
        this.count = -1;
    }

    @Override
    public boolean check(int qSize){
        // compute exp moving avg
        if (this.calcAvg(qSize)){
            return true;
        }
        // if exceeds max capacity, return false
        if (qSize >= super.getMaxCap()){
            return false;
        }
        // congestion control
        double avg = this.getAvgQSize();
        if ((avg >= this.sThres) && (avg < this.lThres)){
            // drop with probability
            this.count ++;
            double pb = this.maxP * (avg - this.sThres) / (this.lThres - this.sThres);
            double pa = pb / (1 - Math.min(this.count * pb, 0.99));
            // System.out.println("[RED] router ID: " + this.routerId + ", pa pb count: " + pa + ", " + pb + ", " + this.count);
            if (Math.random() <= pa){
                // System.out.println("[RED] router ID: " + this.routerId + ", drop occured");
                this.count = 0;
                return false;
            } else {
                return true;
            }
        } else if (avg >= this.lThres){
            // drop all
            // System.out.println("[RED] router ID: " + this.routerId + ", drop-all start");
            this.count = 0;
            return false;
        } else {
            // not reach thres, send
            if (this.count >= 0){
                this.count = -1;
            }
            return true;
        }
    }
    
}
