package Heatequation.Cells;

import java.io.Serializable;
import java.util.Map;

public class FluidCell extends Cell implements Serializable {
    private CellArea area;
    private double numberParticles;
    private double lastNumberParticles;
    private VirtualFluidCell borderCell;
    public Double[] particleFLow;
    public enum particleFlowSource{
        XPLUS1, XMINUS1, YPLUS1, YMINUS1, ZPLUS1, ZMINUS1
    };




    public FluidCell(double value, Material material, double pressure) {
        this.value= value;
        this.alpha = material.getViskosity();
        this.oldValue= value;
        this.isFluid = true;
        this.material= material;
        super.constantTemperature = -1;
        calculateNumberParticlesForTempAndPressure(value, pressure);
        super.isForSolidCalculation = false;

        particleFLow = new Double[6];
        this.resetParticleFlow();
        }

        public double getNusseltNumber(){
            return this.material.getNusselt();
        }

        public void setBorderCell(int numberBorders, double temperature){
        if(numberBorders==0){
            return;
        }



        this.borderCell = new VirtualFluidCell(numberBorders, temperature);
        }

        public int getNumberOfVirtualBorders(){
        if(this.isBorderCell()){
            return this.borderCell.getNumberBorders();
        }
         else {
             return 0;
        }
        }
        public boolean isBorderCell(){
            return this.borderCell != null;
        }


        public void calcDiffussionToBorderCell(double amount){
        if (this.isBorderCell()) {
            this.addToNumberParticlesAndInnerEnergy(-amount * borderCell.getNumberBorders(), this.oldValue);
            this.addToNumberParticlesAndInnerEnergy(amount * borderCell.getNumberBorders(), borderCell.getTemperature());

        } else{
            return;
        }
    }

    public void setBorderCellOnTop(){
        this.borderCell.setOnTop(true);
    }

    public boolean hasBorderCellOnTop(){
        if (this.isBorderCell()) {
            return this.borderCell.isOnTop();
        } else{
            return false;
        }
    }

    public void calcConvectionOverBorder(double amount){
        this.addToNumberParticlesAndInnerEnergy(-amount, this.oldValue);
    }

    public boolean isSolid(){
        return false;
    }

    public boolean isFluid(){
        return true;
    }

    public void setCellArea(CellArea cellArea){
        this.area = cellArea;
    }

    public CellArea getArea() {
        return area;
    }

    public FluidCell getAsFluidCell(){
        return this;
    }

    private void calculateNumberParticlesForTempAndPressure(double temperature, double pressure){
        this.numberParticles = pressure*Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant/temperature;
        this.lastNumberParticles = numberParticles;
    }

    public double getNumberParticles() {
        return numberParticles;
    }

    public void setNumberParticles(double value) {
        this.numberParticles = value;
    }

    public void normalizeNumberParticlesAndTemperature(){
        this.value /= this.numberParticles;
        this.oldValue = value;
        this.lastNumberParticles = numberParticles;
        //this.lastNumberParticles = Cells.cellSize*Cells.cellSize*Cells.cellSize*1/Cells.gasConstant/this.value;
        //this.numberParticles=lastNumberParticles;

    }

    public void initializeNormalization(double ownParticleFlow){
        this.numberParticles -= ownParticleFlow;
        this.value = (this.numberParticles)*this.oldValue;


    }

    private void addToParticleFlow(double numberParticles, particleFlowSource source){
        switch (source){
            case XPLUS1: {
                this.particleFLow[0] += numberParticles;
                return;
            }
            case XMINUS1: {
                this.particleFLow[1] += numberParticles;
                return;
            }
            case YPLUS1: {
                this.particleFLow[2] += numberParticles;
                return;
            }
            case YMINUS1: {
                this.particleFLow[3] += numberParticles;
                return;
            }
            case ZPLUS1: {
                this.particleFLow[4] += numberParticles;
                return;
            }
            case ZMINUS1:{
                this.particleFLow[5] += numberParticles;
                return;
            }
        }
    }

    public double getParticleFlowFromSource( particleFlowSource source){
        switch (source){
            case XPLUS1: {
                return this.particleFLow[0];
            }
            case XMINUS1:return this.particleFLow[1];
            case YPLUS1: return this.particleFLow[2];
            case YMINUS1:return this.particleFLow[3];
            case ZPLUS1: return this.particleFLow[4];
            case ZMINUS1:return this.particleFLow[5];
        }
        return 0;
    }

    public void resetParticleFlow(){
        this.particleFLow[0]=0.;
        this.particleFLow[1]=0.;
        this.particleFLow[2]=0.;
        this.particleFLow[3]=0.;
        this.particleFLow[4]=0.;
        this.particleFLow[5]=0.;
    }


    public void addToNumberParticlesForTemperatureCalculationDuringNormalization(double particles, double temperatureParticles) {
        /*if (particles > 0) {
            this.value += particles * temperatureParticles;
            this.numberParticles += particles;
           } else {
            this.value += particles*this.oldValue;
            this.numberParticles += particles;
        }
        */
        this.value += particles * temperatureParticles;
        this.numberParticles += particles;

    }

    public void addToNumberParticlesForTemperatureCalculationFromVirtualBorderCell(double particles) {
        if (particles > 0) {
            this.value += particles * borderCell.getTemperature();
        } else {
            this.value += particles*this.value;
        }
        this.numberParticles += particles;

       // this.addToParticleFlow(particles, source);
    }


        public void addToAbsoluteNumberParticles(double particles, double temperatureParticles) {
        if (particles > 0) {
            double oldEnergy = this.lastNumberParticles*this.oldValue;
            double incomingEnergy =  particles * temperatureParticles;
            double sum= oldEnergy + incomingEnergy;
            double newParticles = this.lastNumberParticles+particles;
            this.value = (this.numberParticles*this.value + particles * temperatureParticles)/(this.lastNumberParticles+particles);

        }
        this.numberParticles += particles;
    }
    public void addToNumberParticlesAndInnerEnergy(double particles, double temperatureParticles) {
        if (particles > 0) {
              this.value +=  particles * temperatureParticles;
        } else {
            this.value += particles*this.oldValue;
        }
        this.numberParticles += particles;
    }

    public void addToNumberParticlesAndInnerEnergy(double particles, double temperatureParticles, particleFlowSource source) {
        if (particles > 0) {
            this.value +=  particles * temperatureParticles;
        } else {
            this.value += particles*this.oldValue;
        }
        this.numberParticles += particles;
        this.addToParticleFlow(particles, source);
    }

    public void setOldValue(){
        this.oldValue = this.value;
        this.lastNumberParticles = this.numberParticles;
    }

    public double getLastNumberParticles() {
        return lastNumberParticles;
    }

    public double getTemperatureOfBorderCell(){
        return this.borderCell.getTemperature();
    }

    public String toString(){
        return "Fluid cell - T = " + this.value;
    }

    public double getPressureOfBorderCell() {
        return this.borderCell.getPressure();
    }
}
