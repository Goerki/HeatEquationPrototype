package Cells;

public class FluidCell extends Cell {

    public FluidCell(double value, Material material) {
        this.value= value;
        this.norm = material.viskosity;
        this.value= oldValue;
        this.isFluid = true;
        this.material= material;
    }


    public boolean isSolid(){
        return false;
    }

    public boolean isFluid(){
        return true;
    }
}
