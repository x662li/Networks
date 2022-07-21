package Components;

import java.lang.Math;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class QManager {
    private String routerId;
    private final int sThres;
    private final int lThres;
    private double avg;
    private int count;
    private long qTime;
    private double wt;
    private double maxP;
    private Lock avgLock;

    public QManager(String routerId){
        this(5, 10, 0.01, 0.3);
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
        this.avgLock = new ReentrantLock();
    }

    public double getAvgQSize(){
        try{
            avgLock.lock();
            return this.avg;
        } catch (Exception e) {
            System.out.println("Router id: " + this.routerId + " Exception when getting avgQSize");
            e.printStackTrace();
            return (double) -1;
        } finally {
            this.avgLock.unlock();
        }
    }

    public void setAvgQSize(double avgSize){
        try{
            avgLock.lock();
            this.avg = avgSize;
        } catch (Exception e) {
            System.out.println("Router id: " + this.routerId + " Exception when setting avgQSize");
            e.printStackTrace();
        } finally {
            this.avgLock.unlock();
        }
    }

    public boolean calcAvg(int qSize){
        // compute exp moving avg
        double avg = this.getAvgQSize();
        if (qSize > 0){
            // calculate avg
            this.setAvgQSize((1-this.wt) * avg + this.wt * qSize);
            // System.out.println("[QM] router ID: " + this.routerId + ", avg qsize: " + this.getAvgQSize());
        } else if ((qSize == 0) && (this.qTime > 0)){
            // queue stays empty avg decay
            int m = (int) (System.currentTimeMillis() - this.qTime)/1000;
            this.setAvgQSize(Math.pow(1-this.wt, m) * avg);
        } else if ((qSize == 0) && (this.qTime < 0)){
            // first time empty
            this.qTime = System.currentTimeMillis();
            return true; // no need to control
        }
        return false;
    }

    public boolean dropTail(int qSize){
        // use hard buffer size limit, just record avg queue size
        this.calcAvg(qSize);
        return true;
    }

    public boolean RED(int qSize){
        // compute exp moving avg
        if (this.calcAvg(qSize)){
            return true;
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
