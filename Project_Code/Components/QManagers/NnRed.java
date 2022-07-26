package Components.QManagers;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NnRed extends QManager{

    private long prevTime;
    private double prediction;
    private List<Double> qSizeQ;
    private List<Double> weights;
    private double lr;
    private boolean dropInd;
    private double loss;
    private Lock lossLock;

    public NnRed(String routerId) {
        super(routerId);
        this.weights = Arrays.asList((double)-1.2377534738270426, (double)-0.006727822989079063, (double)0.022625802464786342, (double)2.3845358768500113);
        // this.weights = Arrays.asList(Math.random(), Math.random(), Math.random(), Math.random());
        this.prevTime = System.currentTimeMillis();
        this.qSizeQ = new ArrayList<Double>();
        this.prediction = -1;
        this.lr = 0.6;
        this.dropInd = false;
        this.loss = 0;
        this.lossLock = new ReentrantLock();
    }

    @Override
    public double getLoss(){
        try{
            this.lossLock.lock();
            return this.loss;
        } catch (Exception e) {
            System.out.println("Exception when getting loss");
            e.printStackTrace();
            return (double) -1;
        } finally {
            this.lossLock.unlock();
        }
    }

    public void setLoss(double loss){
        try{
            this.lossLock.lock();
            this.loss = loss;
        } catch (Exception e) {
            System.out.println("Exception when setting loss");
            e.printStackTrace();
        } finally {
            this.lossLock.unlock();
        }
    }

    public static double sigmoid(double input){
        return ((double)1 / ((double)1 + Math.exp(-input)));
    }

    public double scaler(int input){
        return (double)input / (double)super.getMaxCap();
    }

    public void backward(double acSize){
        // compute loss and update paramters
        double loss = acSize - this.prediction;
        this.setLoss(loss);
        for (int i=0; i<this.weights.size(); i++){
            double dW = lr * loss * this.prediction * (1 - this.prediction) * this.qSizeQ.get(i);
            // System.out.println("[NNRED] router id: " + super.getRouterId() + " dW" + i + ": " + dW);
            this.prevTime = System.currentTimeMillis();
            this.weights.set(i, this.weights.get(i) + dW);
        }
        // System.out.println("[NNRED] router id: " + super.getRouterId() + " loss: " + loss);
        // System.out.flush();
        this.prevTime = System.currentTimeMillis();
    }

    @Override
    public boolean check(int qSize){
        // record avg queue size
        this.calcAvg(qSize);
        // if queue size exceeds max capacity, retur false;
        if (qSize >= super.getMaxCap()){
            return false;
        }
        // every 1 second, update queue sizes in queue, and make new predictions
        if ((int)(System.currentTimeMillis() - this.prevTime)/1000 >= 1){
            // System.out.println("[NNRED] router id: " + super.getRouterId() + " time: " + (int)(System.currentTimeMillis() - this.prevTime)/1000);
            // System.out.flush();

            // update prevTime
            this.prevTime = System.currentTimeMillis();
            //backward (use prev predicted and current queue size)
            if (this.prediction >= 0){
                this.backward(this.scaler(qSize));
            }
            // update last 4 queue sizes
            if (this.qSizeQ.size() < 4){
                // System.out.println("[NNRED] router id: " + super.getRouterId() + " initial push queue: " + this.scaler(qSize));
                // System.out.flush();
                this.prevTime = System.currentTimeMillis();
                this.qSizeQ.add(this.scaler(qSize));
            } else {
                // System.out.println("[NNRED] router id: " + super.getRouterId() + " push queue: " + this.scaler(qSize));
                // System.out.flush();
                this.prevTime = System.currentTimeMillis();
                this.qSizeQ.remove(0);
                this.qSizeQ.add(this.scaler(qSize));

                // forward
                double outSum = 0;
                for (int i=0; i<this.qSizeQ.size(); i++){
                    outSum += this.qSizeQ.get(i) * this.weights.get(i);
                }
                this.prediction = NnRed.sigmoid(outSum);
                // System.out.println("[NNRED] router id: " + super.getRouterId() + " prediction: " + this.prediction * this.getMaxCap());
                // System.out.flush();
                this.prevTime = System.currentTimeMillis();
                if (this.prediction >= 0.75){
                    this.dropInd = true;
                } else {
                    this.dropInd = false;
                }
            }
        }
        
        if (this.dropInd){
            return false;
        } else {
            return true;
        }

    }
    
}
