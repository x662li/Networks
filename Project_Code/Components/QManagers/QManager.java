package Components.QManagers;

import java.lang.Math;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class QManager {
    private String routerId;
    private final int maxCap;

    private double avg;
    private Lock avgLock;
    private long qTime;
    private double wt;

    public QManager(String routerId){
        this.routerId = routerId;
        
        this.maxCap = 15;
        this.wt = 0.01;

        this.avg = 0;
        this.avgLock = new ReentrantLock();
        this.qTime = -1;
    }

    public String getRouterId(){
        return this.routerId;
    }

    public int getMaxCap(){
        return this.maxCap;
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
        } else if ((qSize == 0) && (qTime > 0)){
            // queue stays empty avg decay
            int m = (int) (System.currentTimeMillis() - qTime)/1000;
            this.setAvgQSize(Math.pow(1 - this.wt, m) * avg);
        } else if ((qSize == 0) && (this.qTime < 0)){
            // first time empty
            this.qTime = System.currentTimeMillis();
            return true; // no need to control
        }
        return false;
    }

    public boolean check(int qSize){
        return true;
    }

}
