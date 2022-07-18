package Components;


public class QManager {
    private final int sThres;
    private final int lThres;

    public QManager(){
        this(5, 10);
    }

    public QManager(int sThres, int lThres){
        this.sThres = sThres;
        this.lThres = lThres;
    }

    public boolean dropTail(int qSize){
        if (qSize > lThres){
            return false;
        }
        return true;
    }

    public boolean RED(int qSize){
        return true;
    }

    public boolean nnRED(int qSize){
        return true;
    }



}
