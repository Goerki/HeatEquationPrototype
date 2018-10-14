public class CalculationThread extends Thread {
    Space space;
    int xStart;
    int xEnd;
    int counter;

    public CalculationThread(Space space, int xStart, int xStop){
        this.space = space;
        this.xStart = xStart;
        this.xEnd = xStop;
        this.counter = 100;
    }


    public void setCounter(int counter){
        this.counter = counter;
    }


    public void run(){
        for (int i = 0; i< this.counter; i++) {
            this.space.calcSpaceFromTo(this.xStart, this.xEnd);
        }
    }
}
