package Cells;

import java.io.Serializable;

public class FluidCell extends Cell implements Serializable {
    private CellArea area;
    private double numberParticles;
    private double lastNumberParticles;
    private VirtualFluidCell borderCell;
    public double gasGleichungsTest;



    public FluidCell(double value, Material material, double pressure) {
        this.value= value;
        this.alpha = material.getViskosity();
        this.oldValue= value;
        this.isFluid = true;
        this.material= material;
        super.constantTemperature = -1;
        calculateNumberParticlesForTempAndPressure(value, pressure);
        super.isForSolidCalculation = false;

        this.gasGleichungsTest = this.value * this.numberParticles;
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

        public boolean isBorderCell(){
            return this.borderCell != null;
        }


        public void calcDiffussionToBorderCell(double amount){
        if (this.isBorderCell()) {
            this.addToAbsoluteNumberParticles(-amount * borderCell.getNumberBorders(), 10);
            this.addToAbsoluteNumberParticles(amount * borderCell.getNumberBorders(), borderCell.getTemperature());

            this.gasGleichungsTest = this.value * this.numberParticles;
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
        this.addToNumberParticlesForTemperatureCalculation(-amount, 10);
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
        this.gasGleichungsTest = this.value * this.numberParticles;
        this.oldValue = value;
        this.lastNumberParticles = numberParticles;
    }

    public void addSelfToNumberParticlesForTemperatureCalculation(){
        this.value = this.lastNumberParticles*this.oldValue;
    }

    public void addToNumberParticlesForTemperatureCalculation(double particles, double temperatureParticles) {
        if (particles > 0) {
            this.value += particles * temperatureParticles;
            }
        this.numberParticles += particles;
        this.gasGleichungsTest = this.value * this.numberParticles;
    }

    public void addToAbsoluteNumberParticles(double particles, double temperatureParticles) {
        if (particles > 0) {
            this.value = (this.lastNumberParticles*this.oldValue + particles * temperatureParticles)/(this.lastNumberParticles+particles);
            this.oldValue = this.value;
        }
        this.numberParticles += particles;
        this.lastNumberParticles = this.numberParticles;
    }

    public void setOldValue(){
        this.oldValue = this.value;
        this.lastNumberParticles = this.numberParticles;
    }

    public double getLastNumberParticles() {
        return lastNumberParticles;
    }

    public String toString(){
        return "Fluid cell - T = " + this.value;
    }
}
