package Cells;

public class SolidCell extends Cell {



    public SolidCell(Material material) {
        this.value = value;
        this.oldValue = value;
        this.norm = material.alpha;
        this.isFluid = false;
        this.material = material;


    }


    public boolean isSolid(){
        return true;
    }

    public boolean isFluid(){
        return false;
    }

}
