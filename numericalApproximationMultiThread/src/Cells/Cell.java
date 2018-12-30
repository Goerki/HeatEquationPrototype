package Cells;

public class Cell {
    protected double value;
    protected double oldValue;
    protected double norm;
    protected boolean isFluid;
    public String material;
    public boolean isInitialized = false;

    public double getAlpha(){
        return this.norm;
    }

    public double getValue(){
        return this.value;
    }

    public double getLastValue(){
        return this.oldValue;
    }

    public double getLastFluidValue(){
        if (this.isFluid){
            return this.oldValue;
        }
        else {
            return 0;
        }
    }

    public void setValue(double newValue){
        this.oldValue = this.value;
        this.value = newValue;
    }

    public double getDoubleValue(){
        double result = this.value;
        return result;
    }
    public boolean isFluid(){
        return this.isFluid;
    }

    public boolean isSolid(){
        return !this.isFluid;
    }
}
