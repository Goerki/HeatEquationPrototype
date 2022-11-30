package Heatequation;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Heatequation.Cells.*;
import org.apache.commons.math3.analysis.function.Log;

public class Space {
    public Cells allCells;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    int numberCellsForSolidCalculation;
    CalculationThread[] calculationThreads;
    MainThread mainThread;
    int numberThreads;
    boolean isInitialized = false;
    SystemOfEquations fluidEquations;
    List<CellArea> areas;
    private double deltaT;
    private double cellLength;
    private int numberSteps;
    private int numberCalculatedSteps;
    public HeatequationLogger logger;
    double startingValue;
    Coordinates logCoords;
    public VisualizationCells[] history;
    private double baseAmplificationFactor;


    private void setLoggerCell(){
        this.logCoords = new Coordinates(3, 5, 3);
    }


    public Space(int sizeX, int sizeY, int sizeZ, double startValue, Material material, int numberThreads, double baseAmplificationFactor){
        //this.logger = new HeatequationLogger("C:\\Users\\thoni\\Documents\\heatEquationLogs\\heatequation.log");
        this.logger = new HeatequationLogger("C:\\Users\\TGyoergy\\Desktop\\TGyoergy\\Privat\\Uni\\diplomarbeit\\logs\\heatequation.log");
        //this.logger.addToLoglevel(HeatequationLogger.LogLevel.DEBUG);
        this.logger.addToLoglevel(HeatequationLogger.LogLevel.ERROR);
        this.logger.addToLoglevel(HeatequationLogger.LogLevel.INFO);
        //this.logger.addToLoglevel(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS);
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "\n\n\n\n ======================\n\nSpace started");
        this.sizeX= sizeX;
        this.sizeY= sizeY;
        this.sizeZ= sizeZ;
        this.allCells = new Cells(sizeX, sizeY, sizeZ, startValue, material, this.logger);
        this.numberThreads=numberThreads;
        this.cellLength = 1;
        this.startingValue = startValue;
        this.setLoggerCell();
        this.initHistory(20);
        this.baseAmplificationFactor=baseAmplificationFactor;
    }

    public SaveFile createSaveFile(){
        return new SaveFile(this.allCells.getAllCellsAsArray(), this.sizeX, this.sizeY, this.sizeZ, this.numberCellsForSolidCalculation,
                this.numberThreads,
                this.deltaT,
                this.cellLength,
                this.numberSteps,
                this.numberCalculatedSteps
        );
    }

    public Space(SaveFile file, double baseAmplificationFactor) {
        //this.logger = new HeatequationLogger("C:\\Users\\thoni\\Documents\\heatEquationLogs\\heatequation.log");
        this.logger = new HeatequationLogger("C:\\Users\\TGyoergy\\Desktop\\TGyoergy\\Privat\\Uni\\diplomarbeit\\logs\\heatequation.log");
        //this.logger.addToLoglevel(HeatequationLogger.LogLevel.DEBUG);
        //this.logger.addToLoglevel(HeatequationLogger.LogLevel.ERROR);
        this.logger.addToLoglevel(HeatequationLogger.LogLevel.INFO);
        //this.logger.addToLoglevel(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS);
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "\n\n\n\n ======================\n\nSpace started");
        this.sizeX = file.sizeX;
        this.sizeY = file.sizeY;
        this.sizeZ = file.sizeZ;
        this.allCells = new Cells(file.cells, sizeX, sizeY, sizeZ, this.logger);
        this.numberThreads = file.numberThreads;
        this.cellLength = file.cellLength;
        this.startingValue = 0.0;
        this.setLoggerCell();
        this.isInitialized = false;
        this.initHistory(20);
        this.baseAmplificationFactor=baseAmplificationFactor;
    }


    private void initHistory(int size) {
        history = new VisualizationCells[size];
        for (int i=0; i<size; i++){
            history[i] = null;
        }
    }

    public double getCellLength() {
        return cellLength;
    }

    public String createCube(int x1, int y1, int z1, int x2, int y2, int z2, Material material){
        if (material.isSolid()){
            return this.allCells.makeCubeSolidCells(x1, y1, z1, x2, y2, z2, material);
        } if (material.isFluid()){
            return this.allCells.makeCubeFluidCells(x1,y1,z1,x2,y2,z2,material);
        }
        else{
            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "please check material definition in json file");
            return "please check material definition in json file";
        }

    }
    public double getMinimumTemperature(){
        double minimum = allCells.getMinimumTemperature();
        for (VisualizationCells hist: this.history){
            if (hist != null){
            if (minimum > hist.getMinimumTemperature() && hist.getMinimumTemperature() != Double.NaN) {
                minimum = hist.getMinimumTemperature();
            }

            }
        }
        return minimum;
    }
    public double getMaximumTemperature(){

        double maximum= allCells.getMaximumTemperature();
        for (VisualizationCells hist: this.history){
            if (hist != null){
                if (maximum < hist.getMaximumTemperature() && hist.getMaximumTemperature() != Double.NaN){
                    maximum = hist.getMaximumTemperature();

                }
            }
        }
        return maximum;
    }

    public String setBoundariesForCube(int x1, int y1, int z1, int x2, int y2, int z2, double constantTemperature, double heatFlow, double startingTemperature){
        return this.allCells.setBoundariesForCube(x1,y1,z1,x2,y2,z2, constantTemperature, heatFlow, startingTemperature);
    }


    public int getSize(String axis){
        if (axis.toLowerCase().contains("x")){
            return sizeX;
        }
        if (axis.toLowerCase().contains("y")){
            return sizeY;
        }
        if (axis.toLowerCase().contains("z")){
            return sizeZ;
        }
        return 0;
    }

    List<CellArea> getFluidCellAreas(){
        List<CellArea> result = new ArrayList<>();
        for (CellArea area: areas){
            if (area.isFluid()){
                result.add(area);
            }
        }
        return result;
    }

    public boolean initialize(double time, double deltaT, int numberThreads) throws Exception{
        //Create areas
        if (this.isInitialized)
            return false;

        this.isInitialized=true;
        this.allCells.createAllVirtualBorderCells(this.startingValue, this.cellLength*this.cellLength*this.cellLength/allCells.gasConstant);
        this.createAreas();
        this.deltaT = deltaT;
        this.numberCalculatedSteps = 0;


        // TODO: calculate steps and delta T
          this.numberSteps = (int)(time/deltaT);

        //create Threads
        this.numberThreads = numberThreads;
        this.createCalculationThreads(this.numberThreads, numberSteps);
        this.saveSnapshotInHistroy();

        return true;
    }

    public void saveSnapshotInHistroy(){


        for (int i=0; i<this.history.length; i++){
            if (this.history[i]==null){
                this.history[i] = new VisualizationCells(this.allCells, this.calcElapsedTime());
                return;

            }

        }

    }

    private double calcElapsedTime() {
        return (double) this.numberCalculatedSteps*deltaT;
    }


    double[][] getZLayer(int z){
        double[][] result = new double[sizeX][sizeY];
        for (int x = 0; x < sizeX; x ++){
            for (int y=0;y<sizeY; y++) {
                result[x][y] = this.allCells.getCell(x,y,z).getValue();
            }
        }
        return result;
    }

    private boolean[][][] getFalseArray(){
        boolean [][][] result= new boolean[this.sizeX][this.sizeY][this.sizeZ];
        for(int x = 0; x < this.sizeX; x++){
            for(int y=0; y<this.sizeY; y++){
                for(int z = 0;z<this.sizeZ; z++){
                    result[x][y][z]= false;
                }
            }
        }
        return result;
    }

    private void createAreas(){
        this.areas = new ArrayList<>();
         for(Coordinates coord:Coordinates.getAllCoordinates(this.sizeX, this.sizeY, this.sizeZ)){
             //check if cell already in a area
            if (!allCells.getCell(coord).isInitialized){
                //create new Area
                areas.add(new CellArea(this, coord, this.logger));
                //set all cells inside this area to initialized
                this.allCells.setCellsToInitialized(this.areas.get(this.areas.size()-1).coords);
            }
        }

        for(CellArea area: areas){
            double[] values =  new double[area.getMaxY() - area.getMinY() + 1];
            for (int layer = 0; layer <= (area.getMaxY() - area.getMinY()); layer++) {
                values[layer] = allCells.getMeanLastValueFor(area.getCellsForLayer(layer+area.getMinY()));
            }
            area.setMeanValues(values);

            if (area.isFluid()){
                for (Coordinates tempCoord: area.coords){

                    allCells.getCell(tempCoord).getAsFluidCell().setCellArea(area);
                }
            }
        }
    }



    private void setNumberSolidCells(){
        this.numberCellsForSolidCalculation = this.allCells.getCellsForSolidCalculation().size();
    }


    private void createCalculationThreads(int numberThreads, int steps)throws Exception{
        setNumberSolidCells();
        this.numberThreads = numberThreads;
        List<Coordinates> fluidCalcCells = this.allCells.getAllFluidCells();
        calculationThreads = new CalculationThread[numberThreads-1];

        List<Coordinates> solidCalcCells = this.allCells.getCellsForSolidCalculation();

        for (int i = -1; i< numberThreads-1; i++){
            if (i==-1){
                this.mainThread = new MainThread(this, getSublistForList(solidCalcCells, numberThreads, 0),getSublistForList(fluidCalcCells, numberThreads, 0), steps, this.getFluidCellAreas());

            } else {
                calculationThreads[i] = new CalculationThread(this, getSublistForList(solidCalcCells, numberThreads, i+1),getSublistForList(fluidCalcCells, numberThreads, i+1), steps);
            }
        }
        mainThread.setThreads(calculationThreads);
    }

    private List<Coordinates> getSublistForList(List<Coordinates> list, int numberSubLists, int index){
        int intervalSize = list.size()/numberSubLists;
        if (list.size()==0){
            return list;
        }
        if (numberSubLists == index+1){
            int indexmalSize = index*intervalSize;
            return list.subList(index*intervalSize, list.size());
        } else {
            int indexmalSize = index*intervalSize;
            int endInterval = (index+1)*intervalSize;
            return list.subList(index*intervalSize, intervalSize*(index+1));
        }
    }

    double[][] getYLayer(int y){
        double[][] result = new double[sizeX][sizeZ];
        for (int x = 0; x < sizeX; x ++){
            for (int z=0;z<sizeZ; z++) {
                result[x][y] = this.allCells.getCell(x,y,z).getValue();
            }
        }
        return result;
    }
    double[][] getXLayer(int x){
        double[][] result = new double[sizeY][sizeZ];
        for (int y=0;y<sizeY; y++) {
            for (int z=0;z<sizeZ; z++) {
                result[x][y] = this.allCells.getCell(x,y,z).getValue();
            }
        }
        return result;
    }

    public int startCalculation(){
        if (!this.isInitialized){
            return -1;
        }
        mainThread.start();
        return 0;
    }

    public void calcSolidCells(List<Coordinates> range){
        for(Coordinates coord:range){
            this.calcNewValueForSolidCell(coord);
        }
    }

    public void updateAllValues(){
        allCells.updateAllOldValues();
    }

    private void calcNewValueForSolidCell(Coordinates coords){
        if (allCells.getCell(coords).getConstantTemperature()!= -1){
            return;
        }
        Cell calcCell = allCells.getCell(coords);
        if (coords.equals(logCoords)){
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "calculating log cell ");
        }

        calcCell.setValue(0);
        for(Coordinates neighbourCellCoords: allCells.getAllAdjacentCellsForSolidCalculation(coords)) {
            Cell neighbourCell = allCells.getCell(neighbourCellCoords);
            if (calcCell.isFluid()){
                calcCell.addToValue((neighbourCell.getLastValue() -calcCell.getLastValue()) *neighbourCell.getConductivity()*calcCell.getConductivity()/100);
            } else {
                //double diff = allCells.getCell(neighbourCell).getLastValue() -allCells.getCell(coords).getLastValue();
                calcCell.addToValue((neighbourCell.getLastValue() -calcCell.getLastValue())*neighbourCell.getConductivity() );
            }
        }
        if (coords.equals(logCoords)){
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "difference before: " + allCells.getCell(coords).getValue());
        }
        calcCell.setValue(calcCell.getValue() *this.deltaT/calcCell.getCapacity()/this.cellLength);

        if (coords.equals(logCoords)){
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "difference after: " + calcCell.getValue());
        }
        calcCell.addToValue(calcCell.getLastValue());
        calcCell.addToValue(calcCell.getHeatFlow()*this.deltaT);
        if (coords.equals(logCoords)){
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "Zelle " + logCoords + " : temperatur " + calcCell.getValue());
        }

    }


    public boolean calculationReady(){
       return this.numberCalculatedSteps >= this.numberSteps;

    }

    public double getPercentageOfStatus(){
        double percentage = ((double)numberCalculatedSteps/(double)numberSteps * 100.0);

        return percentage;

    }

    public void increaseNumberCalculatedSteps(){
        numberCalculatedSteps++;
    }

    public void applyDiffussionAndUpliftOnCells(List<Coordinates> fluidCells){
        for (Coordinates coord:fluidCells){
            this.calculateParticleFlow(coord);
        }
    }

    public int getNumberOfTimeSteps(){
        return this.history.length;
    }

    private void calculateParticleFlow(Coordinates coord){
        if (coord.equals(logCoords)){
            System.out.print(logCoords.toString());
        }
        if(coord.equals(this.logCoords)){
            this.logFluidCell("before diff", coord);
        }
        double diffusion = this.calcDiffusionForCell(coord);

        if(coord.equals(this.logCoords)){
            this.logFluidCell("after diff", coord);
        }

        double convection = this.calcConvectionForCell(coord);
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, coord.toString() + ": diffussion: " + diffusion);
        for(Coordinates neighbor:allCells.getAllAdjacentFluidCells(coord)){
            this.particleFlowFromTo(coord, neighbor, diffusion);
            if (coord.equals(logCoords)){
              //  this.logFluidCell("diffussion from", coord);
              //  this.logFluidCell("diffussion to", neighbor);
            }
        }

        if(coord.equals(this.logCoords)){
            this.logFluidCell("after convection", coord);
        }



        allCells.getCell(coord).getAsFluidCell().calcDiffussionToBorderCell(diffusion);
        if(coord.equals(this.logCoords)){
            this.logFluidCell("before diffussion over borders", coord);
        }
        if (allCells.getCell(coord).getAsFluidCell().isBorderCell()) {
            allCells.getCell(coord).getAsFluidCell().calcDiffussionFromBorderCell(this.calcDiffusionForVirtualBorderCell(coord));
            if(coord.equals(this.logCoords)){
                this.logFluidCell("after diffussion over borders", coord);
            }
            if(coord.equals(this.logCoords)){
                this.logFluidCell("after convection over borders", coord);
            }
        }

            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, coord.toString()+": convection: " + convection);

            this.calcConvectionFlowFromCell(coord, convection);



/*
        if(allCells.getCell(coord).getAsFluidCell().hasBorderCellOnTop()){
            allCells.getCell(coord).getAsFluidCell().calcConvectionOverBorder(convection);
        }
        */
        //this.logFluidCell("after heatFlow from " + coord.toString() , this.logCoords);

        //non functional
        if (this.logCoords.equals(coord)){
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "diff for " + logCoords.toString() + " : " + diffusion);
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "conv for " + logCoords.toString() + " : " + convection);
            double diff = allCells.getCell(coord).getAsFluidCell().getLastNumberParticles() - allCells.getCell(coord).getAsFluidCell().getNumberParticles();
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "flow for " + coord.toString() + " : " + diff + " for lasNumberPart " + allCells.getCell(coord).getAsFluidCell().getLastNumberParticles());
            this.logFluidCell("after convection", coord);
            //this.logFluidCell("y+1 convection", coord.getCellYPlus1());
        }


    }

    private void calcConvectionFlowFromCell(Coordinates coord, double convection) {
        if (allCells.cellExists(coord.getCellYPlus1()) && allCells.getCell(coord.getCellYPlus1()).isFluid()) {
            this.particleFlowFromTo(coord, coord.getCellYPlus1(), convection);
        } else {
            /*
            int numberAdjacentCellsInThisLayer = 0;
            List<Coordinates.direction> allNeighborDirections = new ArrayList<>();
            allNeighborDirections.addAll(this.allCells.getCell(coord).getAsFluidCell().getNeighborDirections());
            allNeighborDirections.addAll(this.allCells.getCell(coord).getAsFluidCell().getBordercellDirections());
            allNeighborDirections.remove(Coordinates.direction.YMINUS1);





            numberAdjacentCellsInThisLayer = allNeighborDirections.size();
            convection /= (double) numberAdjacentCellsInThisLayer;
            for(Coordinates.direction direction: allNeighborDirections){
                if (this.allCells.getCell(coord).getAsFluidCell().getBordercellDirections().contains(direction)){
                    allCells.getCell(coord).getAsFluidCell().addToNumberParticlesAndInnerEnergy(-convection, allCells.getCell(coord).getLastValue(), direction);
                } else {
                    this.particleFlowFromTo(coord, coord.getCell(direction), convection);
                }
            }


             */
        }
    }


    private void calcConvectionFlowFromCellToBorderCell(Coordinates coord, double convection) {
        if (allCells.getCell(coord).getAsFluidCell().isBorderCell() && allCells.getCell(coord).getAsFluidCell().hasBorderCellOnTop()) {
            allCells.getCell(coord).getAsFluidCell().addToNumberParticlesAndInnerEnergy(-convection, allCells.getCell(coord).getAsFluidCell().getTemperatureOfBorderCell(), Coordinates.direction.YPLUS1);
            return;
        } else if (allCells.getCell(coord).getAsFluidCell().isBorderCell() && allCells.getCell(coord).getAsFluidCell().hasBorderCellOnBottom()) {
            allCells.getCell(coord).getAsFluidCell().addToNumberParticlesAndInnerEnergy(convection, allCells.getCell(coord).getAsFluidCell().getTemperatureOfBorderCell(), Coordinates.direction.YMINUS1);
            return;
        } else {
            /*
            TODO: Convection for border cells in the same layer
            int numberAdjacentCellsInThisLayer = 0;
            List<Coordinates> neighborsInThisLayer = this.allCells.getAllAdjacentFluidCells(coord);

            if (allCells.cellExists(coord.getCellYMinus1()) && allCells.getCell(coord.getCellYMinus1()).isFluid()) {
                int removeIndex = 0;
                for (Coordinates anyCoord : neighborsInThisLayer) {
                    if (anyCoord.equals(coord.getCellYMinus1())) {
                        removeIndex = neighborsInThisLayer.indexOf(anyCoord);
                        break;
                    }

                }
                neighborsInThisLayer.remove(removeIndex);
            }

            numberAdjacentCellsInThisLayer = neighborsInThisLayer.size();
            convection /= (double) numberAdjacentCellsInThisLayer;
            for(Coordinates neighbor: neighborsInThisLayer){
                this.particleFlowFromTo(coord, neighbor, convection);
            }





             */

        }
    }


    /**
     * Calculate the amount of particles that will flow over each border
     * @param cell
     * @return
     */
    private double calcDiffusionForCell(Coordinates cell){
        //DIFFUSION BASEFACTOR
        double baseFactor = this.baseAmplificationFactor;
        FluidCell calcCell = allCells.getCell(cell).getAsFluidCell();
        baseFactor *=calcCell.getLastValue()/calcCell.getViskosity();
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "basefactor for temp and material" + cell.toString() + ": " + baseFactor);
        baseFactor *=deltaT;
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "basefactor for delta T" + cell.toString() + ": " + baseFactor);
        baseFactor *= allCells.getCell(cell).getAsFluidCell().getLastNumberParticles();
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "basefactor for number particles" + cell.toString() + ": " + baseFactor);

        return baseFactor;
        //return baseFactor*allCells.getCell(cell).getLastValue()*allCells.getCell(cell).getAlpha()*deltaT*allCells.getCell(cell).getAsFluidCell().getLastNumberParticles();
    }

    /**
     * Calculate the amount of particles that will flow over each border
     * @param cell
     * @return
     */
    private double calcDiffusionForVirtualBorderCell(Coordinates cell){
        double baseFactor = 0.075;
        baseFactor *=allCells.getCell(cell).getAsFluidCell().getTemperatureOfBorderCell()*allCells.getCell(cell).getAlpha()*deltaT*allCells.getCell(cell).getAsFluidCell().getNumberParticlesOfSingleVirtualCell();
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "diffussion for border cells: " + baseFactor);
        return baseFactor;
    }


    private void particleFlowFromTo(Coordinates source, Coordinates target, double amount){


        Coordinates.direction direction = Coordinates.getDestinationForCoordinates(source, target);
        allCells.getCell(source).getAsFluidCell().addToNumberParticlesAndInnerEnergy(-amount, allCells.getCell(target).getLastValue(), direction);
        allCells.getCell(target).getAsFluidCell().addToNumberParticlesAndInnerEnergy(amount, allCells.getCell(source).getLastValue());

    }


     private double calcConvectionForCell(Coordinates coordinates){

        //CONVECTION BASE FACTOR
        double baseFactor = 4*baseAmplificationFactor;
        baseFactor *= (this.allCells.getCell(coordinates).getLastValue() - getMeanValueForAreaAndLayer(coordinates, this.allCells.getCell(coordinates).getAsFluidCell()));
        baseFactor *= this.allCells.getCell(coordinates).getAlpha()*this.allCells.getCell(coordinates).getAsFluidCell().getLastNumberParticles()*deltaT;

         if (coordinates.equals(logCoords)){
             this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "meanValueForLayer : " + getMeanValueForAreaAndLayer(coordinates, this.allCells.getCell(coordinates).getAsFluidCell()) + " leads to difference: " + (this.allCells.getCell(coordinates).getLastValue() - getMeanValueForAreaAndLayer(coordinates, this.allCells.getCell(coordinates).getAsFluidCell())));
         }

        //return baseFactor;
         return 0;
    }

    public void logFluidCell(String introduction, Coordinates coords){
        if (this.allCells.getCell(coords).getAsFluidCell() == null){
            return;
        }

        StringBuilder builder = new StringBuilder("Cell " + coords + " " + introduction+" T= " );
        double temp = this.allCells.getCell(coords).getAsFluidCell().getValue();
        builder.append(temp + " N=");
        double part = this.allCells.getCell(coords).getAsFluidCell().getNumberParticles();
        builder.append(part + " NT=");
        builder.append(part*temp);
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, builder.toString());
        StringBuilder builder0 = new StringBuilder("Cell " + coords + " " + introduction+" T0= " );
        double temp0 = this.allCells.getCell(coords).getAsFluidCell().getLastValue();
        builder0.append(temp0 + " N0=");
        double part0 = this.allCells.getCell(coords).getAsFluidCell().getLastNumberParticles();
        builder0.append(part0 + " N0T0=");
        builder0.append(part0*temp0);
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, builder0.toString());

    }


    private double getMeanValueForAreaAndLayer(Coordinates coords, FluidCell cell){

        if(coords.y == cell.getArea().getMinY()){
            return (cell.getArea().getMeanValueForY(coords.y) + cell.getArea().getMeanValueForY(coords.y +1))/2;
        }
        if(coords.y == cell.getArea().getMaxY()){
            return (cell.getArea().getMeanValueForY(coords.y) + cell.getArea().getMeanValueForY(coords.y -1))/2;
        }
        return (cell.getArea().getMeanValueForY(coords.y) + cell.getArea().getMeanValueForY(coords.y -1) + cell.getArea().getMeanValueForY(coords.y +1))/3;
     }

     public String getAverageTemperatureForTime(int time){
         DecimalFormat f = new DecimalFormat("#0.000");
        if (time >= this.history.length){
            return f.format(this.allCells.getAverageTemp());
        } else {
            return f.format(this.history[time].getAverageTemp());
        }
     }

     public boolean hasFluidCells(){
         for (CellArea eachArea: this.areas){
             if (eachArea.isFluid()){
                 return true;
             }

         }
         return false;
     }

    public String getNumberParticlesForTime(int time){
        DecimalFormat f = new DecimalFormat("#0.0");


        if(!this.hasFluidCells()){
            return "0";
        }

        if (time >= this.history.length){
            return f.format(this.allCells.getNumberParticles());
        } else {
            return f.format(this.history[time].getNumberParticles());
        }
    }


    public String getMaximumTemperatureForTime(int time){
        DecimalFormat f = new DecimalFormat("#0.000");
        if (time >= this.history.length){
            return f.format(this.allCells.getMaximumTemperature());
        } else {
            return f.format(this.history[time].getMaximumTemperature());
        }
    }


    public String  getMinimumTemperatureForTime(int time){
        DecimalFormat f = new DecimalFormat("#0.000");
        if (time >= this.history.length){
            return  f.format(this.allCells.getMinimumTemperature());
        } else {
            return  f.format(this.history[time].getMinimumTemperature());
        }
    }

    public String getElapsedTimeForTime(int time){
        DecimalFormat f = new DecimalFormat("#0.0");
        if (time >= this.history.length){
            return f.format(this.deltaT*this.numberCalculatedSteps);
        } else {
            return f.format(this.history[time].getElapsedTime());
        }
    }



/*
    private void createCalculationThreads(){
        this.createCalculationThreads(this.numberThreads);
    }

    private void createCalculationThreads(int numberThreads){
        this.numberThreads = numberThreads;
        int threadSize = this.size/numberThreads;
        calculationThreads = new CalculationThread[numberThreads];
        int tempSize = 0;
        for (int i = 0; i< numberThreads-1; i++){
            calculationThreads[i] = new CalculationThread(this, tempSize,tempSize+threadSize-1);
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "start: " + tempSize + "end : "+ -(tempSize+threadSize-1));
             tempSize += threadSize;
        }
        calculationThreads[this.calculationThreads.length-1] = new CalculationThread(this, tempSize, this.size-1);
    }

    private double calcSolidTemperatureFlowFromCellAToCellB(Cell cellB, Cell cellA){
        if (cellA == null || cellA.isFluid()) {
            return 0;
        }
        if (cellB == null || cellB.isFluid()) {
            return 0;
        }
        */
        /*
        if (x==20 && y==20 && z == 20){
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "\nLastCellValue: " + cell.getLastValue()+ " LastOwnCellValue: " + ownCell.getLastValue() + " alpha: " + cell.getAlpha());
        }
        */
       // return ((cellA.getLastValue() - cellB.getLastValue())/cellB.getAlpha());
   // }

/*


    private void calcNewValueForFluidCell() {
        this.fluidEquations.solve(this.cells);


    }

    public void calcNewCellValueForSolidCells(int x, int y, int z) {

        if (this.getCell(x,y,z).isSolid()){
            this.calcNewValueForSolidCell(x,y,z);
        }
    }

    public void calcSpace(int steps, int threadInterval){
        this.fluidEquations = new SystemOfEquations(this.cells);

        int numberIterations = steps / threadInterval;
        for (int iteration = 0; iteration< numberIterations; iteration++){

            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "iteration " + iteration + "from " + numberIterations);
            this.createCalculationThreads();
            this.calcSpaceThreads(threadInterval);


        }

    }

    private void calcSpaceThreads(int counter){

        for(int i = 0; i < this.calculationThreads.length; i++) {
            this.calculationThreads[i].setCounter(counter);
            System.out.println("start " + i);
            System.out.println("start: "+ this.calculationThreads[i].getState().toString());
            this.calculationThreads[i].start();
            System.out.println("started: "+ this.calculationThreads[i].getState().toString());
        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        for(int i = 0; i < this.calculationThreads.length; i++) {
            try {
                System.out.println(this.calculationThreads[i].getState().toString() + " : join " + i);
                this.calculationThreads[i].join();
                System.out.println("after: "+ this.calculationThreads[i].getState().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void calcSpaceFromTo(int xStart, int xEnd){

        //Solid cells
        for(int x = xStart; x<=xEnd; x++) {
            for (int y = 0; y < this.size; y++) {
                for (int z = 0; z < this.size; z++) {
                    this.calcNewCellValueForSolidCells(x, y, z);
                }
            }
        }
        //surface cells

        //fluid cells
        this.calcNewValueForFluidCell();

    }


    public String toString(){

        String result = "";
        for(int x = 0; x<this.size; x++){
            result += "\n\n\n";
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "\nx=" + x);
            for(int y = 0; y<this.size; y++){
                result += "\n";
                for(int z = 0; z<this.size; z++){
                    result +=this.getCell(x,y,z).getValue() + " ";
                }
            }
        }
        return result;
    }

    */

    public String drawArea(CellArea area){
        StringBuilder builder = new StringBuilder("area: \n");
        for(Coordinates coord: area.coords){
            if (area.isFluid()){
                FluidCell cell = this.allCells.getCell(coord).getAsFluidCell();
                builder.append(coord.toString() + " : T=");
                builder.append(cell.getValue() + " T0=");
                builder.append(cell.getLastValue() + " N=");
                builder.append(cell.getNumberParticles() + " N0=");
                builder.append(cell.getLastNumberParticles() + "\n");
            }
        }
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, builder.toString());
        return builder.toString();
    }




    public Cell[][] getCellsForLayerAndTime(String axis, int layer, int time) {
        if (time == this.getNumberOfTimeSteps()){
            return this.allCells.getCellsForLayer(axis, layer);
            }

            return this.history[time].getCellsForLayer(axis, layer);
    }

    public double getMaxParticleFlow() {
        double maxPart = -1;
        for (CellArea eachArea : this.areas){
            double tempFlow = this.getMaxParticleFlowForArea(eachArea);
            if (tempFlow > maxPart){
                maxPart = tempFlow;
            }

        }
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "Maximum flow found: " + maxPart);
        return maxPart;
    }

    public double getMinParticleFlow() {
        double minPart = 300;
        for (CellArea eachArea : this.areas){
            double tempFlow = this.getMinParticleFlowForArea(eachArea);
            if (tempFlow < minPart){
                minPart = tempFlow;
            }

        }
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "Minimum flow found: " + minPart);
        return minPart;
    }


    private double getMaxParticleFlowForArea(CellArea eachArea) {
        if (!eachArea.isFluid()){
                return 0;
        } else {
                double maxValue = 0;
                for (Coordinates eachCell: eachArea.coords){
                    double cellFlow = this.allCells.getCell(eachCell).getAsFluidCell().getParticleFlowSum();
                    if (cellFlow > maxValue){
                        maxValue = cellFlow;
                    }

            }
            return maxValue;
        }
    }

    private double getMinParticleFlowForArea(CellArea eachArea) {
        if (!eachArea.isFluid()){
            return 0;
        } else {
            double minValue = 100;
            for (Coordinates eachCell: eachArea.coords){
                double cellFlow = this.allCells.getCell(eachCell).getAsFluidCell().getParticleFlowSum();
                if (cellFlow < minValue){
                    minValue = cellFlow;
                }

            }
            return minValue;
        }
    }

    public void logLogCell(String message) {
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, message);
        System.out.print(message+ "\n");
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, this.allCells.getCell(logCoords).toString());
        System.out.print(this.allCells.getCell(logCoords).toString()+ "\n");
    }
}
