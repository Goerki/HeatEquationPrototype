import Cells.Coordinates;

import java.util.List;

public class CalculationThread extends Thread {
    Space space;
    List<Coordinates> cells;
    int counter;

    public CalculationThread(Space space, List<Coordinates> cells, int steps){
        this.space = space;
        this.cells = cells;
        this.counter = 100;
    }


    public void setCounter(int counter){
        this.counter = counter;
    }


    public void run(){
        for (int i = 0; i< this.counter; i++) {
  //          this.space.calcSpaceFromTo(this.xStart, this.xEnd);
        }
    }
}
