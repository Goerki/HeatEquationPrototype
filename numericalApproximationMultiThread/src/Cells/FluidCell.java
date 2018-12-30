package Cells;

public class FluidCell extends Cell {

    public FluidCell(double value, double alpha) {
        this.value= value;
        this.norm = alpha;
        this.value= oldValue;
        this.isFluid = true;
    }


    public boolean isSolid(){
        return false;
    }

    public boolean isFluid(){
        return true;
    }
}
