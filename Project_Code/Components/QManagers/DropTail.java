package Components.QManagers;

public class DropTail extends QManager{

    public DropTail(String routerId) {
        super(routerId);
    }

    @Override
    public boolean check(int qSize){
        // use hard buffer size limit, just record avg queue size
            super.calcAvg(qSize);
            if (qSize >= super.getMaxCap()){
                return false;
            } else {
                return true;
            }
    }
    
}
