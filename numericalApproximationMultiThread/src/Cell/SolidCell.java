package Cell;

public class SolidCell extends Cell {



    public SolidCell(double value, double alpha) {
        this.value = value;
        this.oldValue = value;
        this.norm = alpha;
        this.isFluid = false;


    }


    public boolean isSolid(){
        return true;
    }

    public boolean isFluid(){
        return false;
    }

}
