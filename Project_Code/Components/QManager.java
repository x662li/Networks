package Components;

import java.lang.Math;

public class QManager {
    private String routerId;
    private final int sThres;
    private final int lThres;
    private double avg;
    private int count;
    private long qTime;
    private double wt;
    private double maxP;

    public QManager(String routerId){
        this(5, 10, 0.01, 0.5);
        this.routerId = routerId;
    }

    public QManager(int sThres, int lThres, double wt, double maxP){
        this.sThres = sThres;
        this.lThres = lThres;
        this.avg = 0;
        this.count = -1;
        this.qTime = -1;
        this.wt = wt;
        this.maxP = maxP;
    }

    public boolean dropTail(int qSize){
        if (qSize >= lThres){
            return false;
        }
        return true;
    }

    public boolean RED(int qSize){
        // compute exp moving avg
        if (qSize > 0){
            // calculate avg
            this.avg = (1-this.wt) * this.avg + this.wt * qSize;
            System.out.println("[RED] router ID: " + this.routerId + ", avg qsize: " + this.avg);
        } else if ((qSize == 0) && (this.qTime > 0)){
            // queue stays empty avg decay
            int m = (int) (System.currentTimeMillis() - this.qTime)/1000;
            this.avg = Math.pow(1-this.wt, m) * this.avg;
        } else if ((qSize == 0) && (this.qTime < 0)){
            // first time empty
            this.qTime = System.currentTimeMillis();
            return true; // no need to control
        }
        // congestion control
        if ((this.avg >= this.sThres) && (this.avg < this.lThres)){
            // drop with probability
            this.count ++;
            double pb = this.maxP * (this.avg - this.sThres) / (this.lThres - this.sThres);
            double pa = pb / (1 - Math.min(this.count * pb, 0.99));
            System.out.println("[RED] router ID: " + this.routerId + ", pa pb count: " + pa + ", " + pb + ", " + this.count);
            if (Math.random() <= pa){
                System.out.println("[RED] router ID: " + this.routerId + ", drop occured");
                this.count = 0;
                return false;
            } else {
                return true;
            }
        } else if (this.avg >= this.lThres){
            // drop all
            System.out.println("[RED] router ID: " + this.routerId + ", drop-all start");
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
