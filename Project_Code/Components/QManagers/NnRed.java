package Components.QManagers;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class NnRed extends QManager{

    private long prevTime;
    private double prediction;
    private List<Double> qSizeQ;
    private List<Double> weights;
    private double lr;
    private boolean dropInd;

    public NnRed(String routerId) {
        super(routerId);
        this.weights = Arrays.asList(Math.random(), Math.random(), Math.random(), Math.random());
        this.prevTime = System.currentTimeMillis();
        this.qSizeQ = new ArrayList<Double>();
        this.prediction = -1;
        this.lr = 0.4;
        this.dropInd = false;
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
        for (int i=0; i<this.weights.size(); i++){
            double dW = lr * loss * this.prediction * (1 - this.prediction) * this.qSizeQ.get(i);
            // System.out.println("[NNRED] router id: " + super.getRouterId() + " dW" + i + ": " + dW);
            this.prevTime = System.currentTimeMillis();
            this.weights.set(i, this.weights.get(i) + dW);
        }
        System.out.println("[NNRED] router id: " + super.getRouterId() + " loss: " + loss);
        System.out.flush();
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
        // every 2 second, update queue sizes in queue, and make new predictions
        if ((int)(System.currentTimeMillis() - this.prevTime)/1000 >= 2){
            
            // System.out.println("[NNRED] router id: " + super.getRouterId() + " time: " + (int)(System.currentTimeMillis() - this.prevTime)/1000);
            // System.out.flush();
            // update prevTime
            this.prevTime = System.currentTimeMillis();
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

                //backward (use prev predicted and current queue size)
                if (this.prediction >= 0){
                    this.backward(this.scaler(qSize));
                }
                // forward
                double outSum = 0;
                for (int i=0; i<this.qSizeQ.size(); i++){
                    outSum += this.qSizeQ.get(i) * this.weights.get(i);
                }
                this.prediction = NnRed.sigmoid(outSum);
                System.out.println("[NNRED] router id: " + super.getRouterId() + " prediction: " + this.prediction * this.getMaxCap());
                System.out.flush();
                this.prevTime = System.currentTimeMillis();
                if (this.prediction > 1){
                    this.dropInd = true;
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
