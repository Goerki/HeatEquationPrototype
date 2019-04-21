package Heatequation.Cells;

import java.io.Serializable;

public class SolidCell extends Cell implements Serializable {



    public SolidCell(Material material, double value) {
        this.value = value;
        this.oldValue = value;
        this.alpha = material.getAlpha();
        this.isFluid = false;
        this.material = material;
        super.constantTemperature = -1;
        super.isForSolidCalculation = true;
    }



    public boolean isSolid(){
        return true;
    }

    public boolean isFluid(){
        return false;
    }

    public String toString(){
        return "Solid cell - T = " + this.value;
    }


}
