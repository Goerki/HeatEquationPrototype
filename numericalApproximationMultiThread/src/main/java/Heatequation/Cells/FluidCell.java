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
    private Map<Coordinates.direction, Double> particleFLow;
    private Map<Coordinates.direction, Double> inertiaParticleFlow;
    private BigDecimal pressure;


    public Map<Coordinates.direction, Double> getParticleFLow() {
        return particleFLow;
    }

    public Map<Coordinates.direction, Double> getInertiaParticleFlow(){
        return inertiaParticleFlow;
    }

    public void calculateInertiaParticleFlow() {

        this.calculateInertiaParticleFlowInDirection(Coordinates.direction.XPLUS1);
        this.calculateInertiaParticleFlowInDirection(Coordinates.direction.YPLUS1);
        this.calculateInertiaParticleFlowInDirection(Coordinates.direction.ZPLUS1);
    }

    private List<Coordinates.direction> getBordercellDirections(){
        List<Coordinates.direction> result = new ArrayList<>();
        if (!this.isBorderCell()){
            return result;
        }
        return this.borderCell.getDirections();
    }

    private void calculateInertiaParticleFlowInDirection(Coordinates.direction positiveDirection) {
        double particleFlowInPositiveDirection = this.particleFLow.get(positiveDirection) +this.particleFLow.get(this.getOppositeDirection(positiveDirection));
        if (particleFlowInPositiveDirection == 0){
            return;
        }


        particleFlowInPositiveDirection *= -0.8;
        Coordinates.direction targetDirection = this.getOppositeDirectionForParticleFlow(positiveDirection);
        if(this.directionExists(targetDirection)){

            this.addInertiaParticleFlow(particleFlowInPositiveDirection*0.8, targetDirection);
            particleFlowInPositiveDirection *= 0.2;
        }
        List<Coordinates.direction> directionList =  this.neighborCellsOrthogonalTo(positiveDirection);
        for (Coordinates.direction direction: directionList){
            this.addInertiaParticleFlow(particleFlowInPositiveDirection/directionList.size(), direction);
        }
    }

    private List<Coordinates.direction> neighborCellsOrthogonalTo(Coordinates.direction positiveDirection) {
        List<Coordinates.direction> result = new ArrayList<>();
        switch (positiveDirection){
            case XPLUS1:{
                if (this.directionExists(Coordinates.direction.YPLUS1)){
                    result.add(Coordinates.direction.YPLUS1);
                    }
                if (this.directionExists(Coordinates.direction.YMINUS1)){
                    result.add(Coordinates.direction.YMINUS1);
                }
                if (this.directionExists(Coordinates.direction.ZPLUS1)){
                    result.add(Coordinates.direction.ZPLUS1);
                }
                if (this.directionExists(Coordinates.direction.ZMINUS1)){
                    result.add(Coordinates.direction.ZMINUS1);
                }
                return result;
            }
            case YPLUS1:{
                if (this.directionExists(Coordinates.direction.XPLUS1)){
                    result.add(Coordinates.direction.XPLUS1);
                }
                if (this.directionExists(Coordinates.direction.XMINUS1)){
                    result.add(Coordinates.direction.XMINUS1);
                }
                if (this.directionExists(Coordinates.direction.ZPLUS1)){
                    result.add(Coordinates.direction.ZPLUS1);
                }
                if (this.directionExists(Coordinates.direction.ZMINUS1)){
                    result.add(Coordinates.direction.ZMINUS1);
                }
                return result;
            }
            case ZPLUS1:{
                if (this.directionExists(Coordinates.direction.YPLUS1)){
                    result.add(Coordinates.direction.YPLUS1);
                }
                if (this.directionExists(Coordinates.direction.YMINUS1)){
                    result.add(Coordinates.direction.YMINUS1);
                }
                if (this.directionExists(Coordinates.direction.XPLUS1)){
                    result.add(Coordinates.direction.XPLUS1);
                }
                if (this.directionExists(Coordinates.direction.XMINUS1)){
                    result.add(Coordinates.direction.XMINUS1);
                }
                return result;
            }

        }
        return null;
    }

    private void addInertiaParticleFlow(double particleFlow, Coordinates.direction direction) {
        Double newValue =  this.inertiaParticleFlow.get(direction)+particleFlow;
        this.inertiaParticleFlow.remove(direction);
        this.inertiaParticleFlow.put(direction, newValue);
    }

    private boolean directionExists(Coordinates.direction oppositeDirection) {
        return this.neighborDirections.contains(oppositeDirection);
    }

    private Coordinates.direction getOppositeDirectionForParticleFlow(Coordinates.direction positiveDirection) {
        switch (positiveDirection){
            case XPLUS1:{
                if(Math.abs(this.particleFLow.get(Coordinates.direction.XPLUS1)) < Math.abs(this.particleFLow.get(Coordinates.direction.XMINUS1))){
                    return Coordinates.direction.XPLUS1;
                } else {
                    return Coordinates.direction.XMINUS1;
                }
            }
            case YPLUS1:{
                if(Math.abs(this.particleFLow.get(Coordinates.direction.YPLUS1)) < Math.abs(this.particleFLow.get(Coordinates.direction.YMINUS1))){
                    return Coordinates.direction.YPLUS1;
                } else {
                    return Coordinates.direction.YMINUS1;
                }
            }
            case ZPLUS1:{
                if(Math.abs(this.particleFLow.get(Coordinates.direction.ZPLUS1)) < Math.abs(this.particleFLow.get(Coordinates.direction.ZMINUS1))){
                    return Coordinates.direction.ZPLUS1;
                } else {
                    return Coordinates.direction.ZMINUS1;
                }
            }
        }
        return null;
    }

    private Coordinates.direction getOppositeDirection(Coordinates.direction direction){
        switch (direction){
            case XPLUS1: return Coordinates.direction.XMINUS1;
            case XMINUS1: return Coordinates.direction.XPLUS1;
            case YPLUS1: return Coordinates.direction.YMINUS1;
            case YMINUS1: return Coordinates.direction.YPLUS1;
            case ZPLUS1: return Coordinates.direction.ZMINUS1;
            case ZMINUS1: return Coordinates.direction.ZPLUS1;
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

    public void particleFlowFromEachBorderCell(double amount) {

        for(Coordinates.direction direction: this.borderCell.getDirections()){
            this.addToNumberParticlesAndTemperature(amount, this.borderCell.getTemperature(), direction);

        }
    }

    public double getParticleFlowSum() {
        double result = 0;
        result += Math.abs(this.particleFLow.get(Coordinates.direction.XPLUS1) - this.particleFLow.get(Coordinates.direction.XMINUS1));
        result += Math.abs(this.particleFLow.get(Coordinates.direction.YPLUS1) - this.particleFLow.get(Coordinates.direction.YMINUS1));
        result += Math.abs(this.particleFLow.get(Coordinates.direction.ZPLUS1) - this.particleFLow.get(Coordinates.direction.ZMINUS1));
        return result;
    }


    private List<Coordinates.direction> neighborDirections;




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

        public void setBorderCell(int numberBorders, double temperature, double gasConstant, List<Coordinates.direction> neighborDirections){
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


        Coordinates.direction direction = Coordinates.getSourceForCoordinates(source, target);
        allCells.getCell(source).getAsFluidCell().addToNumberParticlesAndInnerEnergy(-amount, allCells.getCell(target).getLastValue(), Coordinates.getOppositeParticleFlowDirection(direction));
        allCells.getCell(target).getAsFluidCell().addToNumberParticlesAndInnerEnergy(amount, allCells.getCell(source).getLastValue(), direction);

         */

        public void calcDiffussionToBorderCell(double amount){
        if (this.isBorderCell()) {
            for (Coordinates.direction source: this.getBordercellDirections()){
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

    private void addToParticleFlow(double numberParticles, Coordinates.direction source){
        Double newValue =  this.particleFLow.get(source)+numberParticles;
        this.particleFLow.remove(source);
        this.particleFLow.put(source, newValue);
    }

    public double getParticleFlowFromSource( Coordinates.direction source){
       return this.particleFLow.get(source);
    }

    public void resetParticleFlow(){
        this.particleFLow.put(Coordinates.direction.XPLUS1, 0.0);
        this.particleFLow.put(Coordinates.direction.XMINUS1, 0.0);
        this.particleFLow.put(Coordinates.direction.YPLUS1, 0.0);
        this.particleFLow.put(Coordinates.direction.YMINUS1, 0.0);
        this.particleFLow.put(Coordinates.direction.ZPLUS1, 0.0);
        this.particleFLow.put(Coordinates.direction.ZMINUS1, 0.0);

    }

    public void resetInertiaParticleFlow(){
        this.inertiaParticleFlow.put(Coordinates.direction.XPLUS1, 0.0);
        this.inertiaParticleFlow.put(Coordinates.direction.XMINUS1, 0.0);
        this.inertiaParticleFlow.put(Coordinates.direction.YPLUS1, 0.0);
        this.inertiaParticleFlow.put(Coordinates.direction.YMINUS1, 0.0);
        this.inertiaParticleFlow.put(Coordinates.direction.ZPLUS1, 0.0);
        this.inertiaParticleFlow.put(Coordinates.direction.ZMINUS1, 0.0);
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

    /**
     * adds the number of particles to the number of particles and adds the energy of the particle flow to the inner energy in the value property (you need to normilize them afterwards)
     * @param particles the number of particles that will be added as flow
     * @param temperatureParticles the temperature of the paricles
     * @param source the source, where the particles are coming from
     */
    public void addToNumberParticlesAndInnerEnergy(double particles, double temperatureParticles, Coordinates.direction source) {
        if (particles > 0) {
            this.value +=  particles * temperatureParticles;
        } else {
            this.value += particles*this.oldValue;
        }
        this.numberParticles += particles;
        this.addToParticleFlow(particles, source);
    }


    /**
     * adds the number of particles to the number of particles and calculates the new temerature as the average value of the ld and the new particles temperatures
     * @param particles the number of particles that will be added as flow
     * @param temperatureParticles the temperature of the paricles
     * @param source the source, where the particles are coming from
     */
    public void addToNumberParticlesAndTemperature(double particles, double temperatureParticles, Coordinates.direction source) {
        this.value *= this.numberParticles;
        if (particles > 0) {
            this.value +=  particles * temperatureParticles;
        } else {
            this.value += particles*this.oldValue;
        }
        this.numberParticles += particles;
        this.value /= this.numberParticles;
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


    public void setNeighborDirections(List<Coordinates.direction> neighborDirections) {
        this.neighborDirections = neighborDirections;
    }

    public double getEnergy() {
        return this.value*this.numberParticles;
    }
}
