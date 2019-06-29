package Heatequation.Cells;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidCell extends Cell implements Serializable {
    private CellArea area;
    private double numberParticles;
    private double lastNumberParticles;
    private VirtualFluidCell borderCell;
    private Map<particleFlowSource, Double> particleFLow;
    private Map<particleFlowSource, Double> inertiaParticleFlow;
    private BigDecimal pressure;


    public Map<particleFlowSource, Double> getParticleFLow() {
        return particleFLow;
    }

    public Map<particleFlowSource, Double> getInertiaParticleFlow(){
        return inertiaParticleFlow;
    }

    public void calculateInertiaParticleFlow() {

        this.calculateInertiaParticleFlowInDirection(particleFlowSource.XPLUS1);
        this.calculateInertiaParticleFlowInDirection(particleFlowSource.YPLUS1);
        this.calculateInertiaParticleFlowInDirection(particleFlowSource.ZPLUS1);
    }

    private List<particleFlowSource> getBordercellDirections(){
        List<particleFlowSource> result = new ArrayList<>();
        if (!this.isBorderCell()){
            return result;
        }
        return this.borderCell.getDirections();
    }

    private void calculateInertiaParticleFlowInDirection(FluidCell.particleFlowSource positiveDirection) {
        double particleFlowInPositiveDirection = this.particleFLow.get(positiveDirection) +this.particleFLow.get(this.getOppositeDirection(positiveDirection));
        if (particleFlowInPositiveDirection == 0){
            return;
        }


        particleFlowInPositiveDirection *= -0.8;
        FluidCell.particleFlowSource targetDirection = this.getOppositeDirectionForParticleFlow(positiveDirection);
        if(this.directionExists(targetDirection)){

            this.addInertiaParticleFlow(particleFlowInPositiveDirection*0.8, targetDirection);
            particleFlowInPositiveDirection *= 0.2;
        }
        List<FluidCell.particleFlowSource> directionList =  this.neighborCellsOrthogonalTo(positiveDirection);
        for (FluidCell.particleFlowSource direction: directionList){
            this.addInertiaParticleFlow(particleFlowInPositiveDirection/directionList.size(), direction);
        }
    }

    private List<particleFlowSource> neighborCellsOrthogonalTo(particleFlowSource positiveDirection) {
        List<particleFlowSource> result = new ArrayList<>();
        switch (positiveDirection){
            case XPLUS1:{
                if (this.directionExists(particleFlowSource.YPLUS1)){
                    result.add(particleFlowSource.YPLUS1);
                    }
                if (this.directionExists(particleFlowSource.YMINUS1)){
                    result.add(particleFlowSource.YMINUS1);
                }
                if (this.directionExists(particleFlowSource.ZPLUS1)){
                    result.add(particleFlowSource.ZPLUS1);
                }
                if (this.directionExists(particleFlowSource.ZMINUS1)){
                    result.add(particleFlowSource.ZMINUS1);
                }
                return result;
            }
            case YPLUS1:{
                if (this.directionExists(particleFlowSource.XPLUS1)){
                    result.add(particleFlowSource.XPLUS1);
                }
                if (this.directionExists(particleFlowSource.XMINUS1)){
                    result.add(particleFlowSource.XMINUS1);
                }
                if (this.directionExists(particleFlowSource.ZPLUS1)){
                    result.add(particleFlowSource.ZPLUS1);
                }
                if (this.directionExists(particleFlowSource.ZMINUS1)){
                    result.add(particleFlowSource.ZMINUS1);
                }
                return result;
            }
            case ZPLUS1:{
                if (this.directionExists(particleFlowSource.YPLUS1)){
                    result.add(particleFlowSource.YPLUS1);
                }
                if (this.directionExists(particleFlowSource.YMINUS1)){
                    result.add(particleFlowSource.YMINUS1);
                }
                if (this.directionExists(particleFlowSource.XPLUS1)){
                    result.add(particleFlowSource.XPLUS1);
                }
                if (this.directionExists(particleFlowSource.XMINUS1)){
                    result.add(particleFlowSource.XMINUS1);
                }
                return result;
            }

        }
        return null;
    }

    private void addInertiaParticleFlow(double particleFlow, particleFlowSource direction) {
        Double newValue =  this.inertiaParticleFlow.get(direction)+particleFlow;
        this.inertiaParticleFlow.remove(direction);
        this.inertiaParticleFlow.put(direction, newValue);
    }

    private boolean directionExists(particleFlowSource oppositeDirection) {
        return this.neighborDirections.contains(oppositeDirection);
    }

    private particleFlowSource getOppositeDirectionForParticleFlow(particleFlowSource positiveDirection) {
        switch (positiveDirection){
            case XPLUS1:{
                if(Math.abs(this.particleFLow.get(particleFlowSource.XPLUS1)) < Math.abs(this.particleFLow.get(particleFlowSource.XMINUS1))){
                    return particleFlowSource.XPLUS1;
                } else {
                    return particleFlowSource.XMINUS1;
                }
            }
            case YPLUS1:{
                if(Math.abs(this.particleFLow.get(particleFlowSource.YPLUS1)) < Math.abs(this.particleFLow.get(particleFlowSource.YMINUS1))){
                    return particleFlowSource.YPLUS1;
                } else {
                    return particleFlowSource.YMINUS1;
                }
            }
            case ZPLUS1:{
                if(Math.abs(this.particleFLow.get(particleFlowSource.ZPLUS1)) < Math.abs(this.particleFLow.get(particleFlowSource.ZMINUS1))){
                    return particleFlowSource.ZPLUS1;
                } else {
                    return particleFlowSource.ZMINUS1;
                }
            }
        }
        return null;
    }

    private particleFlowSource getOppositeDirection(particleFlowSource direction){
        switch (direction){
            case XPLUS1: return particleFlowSource.XMINUS1;
            case XMINUS1: return particleFlowSource.XPLUS1;
            case YPLUS1: return particleFlowSource.YMINUS1;
            case YMINUS1: return particleFlowSource.YPLUS1;
            case ZPLUS1: return particleFlowSource.ZMINUS1;
            case ZMINUS1: return particleFlowSource.ZPLUS1;
        }
        return null;
    }

    public double getNumberParticlesOfSingleVirtualCell() {
        return this.numberParticles;
    }

    public void calcDiffussionFromBorderCell(double calcDiffusionForVirtualBorderCell) {
        this.addToNumberParticlesAndInnerEnergy(calcDiffusionForVirtualBorderCell * borderCell.getNumberBorders(), borderCell.getTemperature());
    }

    public boolean hasBorderCellOnBottom() {
        if (this.isBorderCell()){
            return this.borderCell.isOnBottom();

        }
        else {
            return false;
        }
    }


    public enum particleFlowSource{
        XPLUS1, XMINUS1, YPLUS1, YMINUS1, ZPLUS1, ZMINUS1
    };
    private List<particleFlowSource> neighborDirections;




    public FluidCell(double value, Material material, double pressure) {
        this.value= value;
        this.alpha = material.getViskosity();
        this.oldValue= value;
        this.isFluid = true;
        this.material= material;
        super.constantTemperature = -1;
        calculateNumberParticlesForTempAndPressure(value, pressure);
        super.isForSolidCalculation = false;

        particleFLow = new HashMap<>();
        inertiaParticleFlow = new HashMap<>();
        this.resetParticleFlow();
        this.resetInertiaParticleFlow();
        this.neighborDirections = new ArrayList<>();
        }

        public double getPressure(){
        return this.pressure.doubleValue();
        }

    public BigDecimal getPressureAsBigDecimal(){
        return this.pressure;
    }


        public double getNusseltNumber(){
            return this.material.getNusselt();
        }

        public void setBorderCell(int numberBorders, double temperature, double gasConstant, List<FluidCell.particleFlowSource> neighborDirections){
        if(numberBorders==0){
            return;
        }



        this.borderCell = new VirtualFluidCell(numberBorders, temperature, gasConstant, neighborDirections);
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


        /*


        FluidCell.particleFlowSource direction = Coordinates.getSourceForCoordinates(source, target);
        allCells.getCell(source).getAsFluidCell().addToNumberParticlesAndInnerEnergy(-amount, allCells.getCell(target).getLastValue(), Coordinates.getOppositeParticleFlowDirection(direction));
        allCells.getCell(target).getAsFluidCell().addToNumberParticlesAndInnerEnergy(amount, allCells.getCell(source).getLastValue(), direction);

         */

        public void calcDiffussionToBorderCell(double amount){
        if (this.isBorderCell()) {
            for (particleFlowSource source: this.getBordercellDirections()){
                this.addToNumberParticlesAndInnerEnergy(-amount, this.oldValue, source);
            }


            //this.addToNumberParticlesAndInnerEnergy(amount * borderCell.getNumberBorders(), borderCell.getTemperature());

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
        this.pressure = BigDecimal.valueOf(pressure);
        this.numberParticles = pressure*Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant/temperature;
        this.lastNumberParticles = numberParticles;
    }

    public void calculatePressure(double gasConstant, double volume){
        this.pressure = BigDecimal.valueOf(this.value*this.numberParticles*gasConstant/volume);
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
        Double newValue =  this.particleFLow.get(source)+numberParticles;
        this.particleFLow.remove(source);
        this.particleFLow.put(source, newValue);
    }

    public double getParticleFlowFromSource( particleFlowSource source){
       return this.particleFLow.get(source);
    }

    public void resetParticleFlow(){
        this.particleFLow.put(particleFlowSource.XPLUS1, 0.0);
        this.particleFLow.put(particleFlowSource.XMINUS1, 0.0);
        this.particleFLow.put(particleFlowSource.YPLUS1, 0.0);
        this.particleFLow.put(particleFlowSource.YMINUS1, 0.0);
        this.particleFLow.put(particleFlowSource.ZPLUS1, 0.0);
        this.particleFLow.put(particleFlowSource.ZMINUS1, 0.0);

    }

    public void resetInertiaParticleFlow(){
        this.inertiaParticleFlow.put(particleFlowSource.XPLUS1, 0.0);
        this.inertiaParticleFlow.put(particleFlowSource.XMINUS1, 0.0);
        this.inertiaParticleFlow.put(particleFlowSource.YPLUS1, 0.0);
        this.inertiaParticleFlow.put(particleFlowSource.YMINUS1, 0.0);
        this.inertiaParticleFlow.put(particleFlowSource.ZPLUS1, 0.0);
        this.inertiaParticleFlow.put(particleFlowSource.ZMINUS1, 0.0);
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

    public void applyPressure(double pressure, double gasConstant, double volume){

        this.oldValue = pressure*volume/gasConstant/this.lastNumberParticles;
        this.value = oldValue;
        this.pressure=BigDecimal.valueOf(pressure);
    }

    public String verifyPressure(double pressure, double gasConstant, double volume){

        double newTemp = pressure*volume/gasConstant/this.lastNumberParticles;
        return "current temp: " + this.value + " ideal it should be " + newTemp + " thats a difference of " + (this.value-newTemp);

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


    public void setNeighborDirections(List<particleFlowSource> neighborDirections) {
        this.neighborDirections = neighborDirections;
    }

}
