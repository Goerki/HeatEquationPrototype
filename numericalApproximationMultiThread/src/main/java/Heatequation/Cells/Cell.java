package Heatequation.Cells;


import java.io.Serializable;

public class Cell implements Serializable {
    protected double value;
    protected double oldValue;
    protected double alpha;
    protected boolean isFluid;
    public boolean isInitialized = false;
    public Material material;
    protected double constantTemperature;
    protected double heatFlow;
    protected boolean isForSolidCalculation;


    public boolean setConstantTemperature(double temp){
        if (this.isSolid()){
            this.constantTemperature=temp;
            this.value=temp;
            this.oldValue = temp;
            return true;
        }else{
            return false;
        }
    }

    public boolean setHeatFlow(double flow){
        if (this.isSolid()){
            this.heatFlow=flow;
            return true;
        }else{
            return false;
        }
    }

    public void setForSolidCalculation(boolean state){
        this.isForSolidCalculation=state;
    }

    public boolean isForSolidCalculation(){
        return this.isForSolidCalculation;
    }

    public double getHeatFlow(){return this.heatFlow;}

    public double getConstantTemperature(){return this.constantTemperature;}

    public double getAlpha(){
        return this.alpha;
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

    public FluidCell getAsFluidCell(){
        return null;
    }


    public void setValue(double newValue){
        this.value = newValue;
    }

    public void addToValue(double newValue){
        this.value += newValue;
    }

    public void setOldValue(){
        this.oldValue = this.value;
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

    public double getConductivity() {
        return this.material.getheatConductivity();
    }

    public double getCapacity() {
        return this.material.getHeatCapacity();
    }
}
