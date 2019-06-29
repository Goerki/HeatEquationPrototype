package Heatequation.Cells;

import Heatequation.HeatequationLogger;
import Heatequation.Space;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class CellArea implements Serializable {
    private double pressureDivergence;
    private double energyPerCell;
    private boolean isFluid;
    public List<Coordinates> coords;
    private List<List<Coordinates>> yLayers;
    private List<Coordinates> borderCellsWithVirtualCells;
    private int minY;
    private int maxY;
    private double[] meanValues;
    public double averageTermperature;
    public double particleSum;
    private double pressure;
    BigDecimal pressureDeci;
    private double normalization;
    private boolean isIsochor;
    private boolean isIsobar;
    private HeatequationLogger logger;
    private Map<Coordinates, List<Coordinates>> nearFieldMap;
    private Map<Coordinates, Map<Coordinates, Double>> factorMap;
    private Map<Coordinates, Map<Coordinates, Double>> factorsForVirtualBorderCells;
    private Map<Coordinates, Integer> sizeNearField;
     private Map<Coordinates, Integer> systemOfEquationsMapping;
    private Map<Integer,Coordinates> coordinatesToIndexMapping;
    private Map<Coordinates, Integer> systemOfEquationsMappingForVirtualCells;
    private Map<Integer,Coordinates> coordinatesToIndexMappingForVirtualCells;



    public CellArea(Space space, Coordinates startCell, HeatequationLogger logger){
        if (space.allCells.getCell(startCell).isFluid){
            this.isFluid=true;
        } else {
            this.isFluid= false;
        }
        initCoords(space.allCells, startCell);
        if (this.coords.size() == 0){
            return;
        }

        this.logger = logger;
        this.averageTermperature = -1;
        this.particleSum = -1;
        this.minY = this.setMinY();
        this.maxY = this.setMaxY();
        this.meanValues= new double[maxY - minY];
        this.factorMap = new HashMap<>();
        this.factorsForVirtualBorderCells = new HashMap<>();
        setYLayers();
        this.setBorderCellsWithVirtualCells(space.allCells);
        this.setIndexCoordinatesMapping();
        if (this.borderCellsWithVirtualCells.size()==0){
            this.isIsobar = false;
            this.isIsochor = true;
            this.normalization = space.allCells.gasConstant / space.getCellLength()/space.getCellLength()/space.getCellLength()/(double) this.coords.size();
        } else {
            this.isIsochor= false;
            this.isIsobar = true;

        }
        this.calcPressure(space.allCells);

        if (this.isFluid){
            for (Coordinates coord: this.coords){
                space.allCells.setExistingNeighborDirections(coord);
            }
        }

        this.getNearFieldApproximation(space);

    }

    public void setAverageTemperature(Cells cells){
        this.averageTermperature = 0;
        for (Coordinates coords: this.coords){
            this.averageTermperature += cells.getCell(coords).getAsFluidCell().getNumberParticles();
        }

    }

    public void calcPressure(Cells space){
        if (this.isIsobar){
            this.pressure = space.getCell(this.borderCellsWithVirtualCells.get(0)).getAsFluidCell().getPressureOfBorderCell();
            this.energyPerCell = this.pressure / space.gasConstant*space.cellSize*space.cellSize*space.cellSize;
            this.pressureDeci = new BigDecimal(this.pressure);
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "area is isobar. pressure set to " + this.pressure);
        } else{

            //this.calcAverages(space);
            //this.pressure = this.particleSum*this.averageTermperature*this.normalization;
            //this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "area is not isobar. Old pressure set to " + this.pressure +  " from particleSum " + this.particleSum + " average Temperature " + averageTermperature + " and normalization " + this.normalization);

            if(this.isFluid) {
                /*
                double energySum = 0;
                for (Coordinates eachCell : this.coords) {
                    energySum += space.getCell(eachCell).getAsFluidCell().getValue();
                }
                this.pressure = energySum/(double)this.coords.size();
                this.pressure *= space.gasConstant * this.particleSum/(double)this.coords.size();

                this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "area is not isobar. NEW pressure set to " + this.pressure +  " from energySum " + energySum);

*/
                this.pressure =0;
                this.pressureDeci = new BigDecimal(0);
                for (Coordinates eachCell : this.coords) {
                    space.getCell(eachCell).getAsFluidCell().calculatePressure(space.gasConstant, 1);
                    //this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "pressure for cell " + eachCell.toString() + " is "+  space.getCell(eachCell).getAsFluidCell().getPressure());
                    pressure += space.getCell(eachCell).getAsFluidCell().getPressure();
                    pressureDeci = pressureDeci.add(space.getCell(eachCell).getAsFluidCell().getPressureAsBigDecimal());
                }
                pressureDeci = pressureDeci.divide(BigDecimal.valueOf(this.coords.size()), 50, RoundingMode.HALF_UP);


                this.pressure /= this.coords.size();
                this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "pressure set to " + pressure + " with average pressure of all cells. absolute value: " + pressureDeci);
                this.pressure = pressureDeci.doubleValue();
            }


        }
    }

    public void setFactorsForCell(Coordinates centerCell, double generalAverage, Cells cells){

        /*
        setting average for each cell
         */
        double average=0;
        for (Coordinates eachCord: this.getNearFieldCoordinatesForCell(centerCell)){
            average += cells.getCell(eachCord).getLastValue()*cells.getCell(eachCord).getAsFluidCell().getLastNumberParticles();
            if (cells.getCell(eachCord).getAsFluidCell().isBorderCell()){
                for (int i=0; i < cells.getCell(eachCord).getAsFluidCell().getNumberOfVirtualBorders(); i++) {
                    average+= cells.getCell(eachCord).getAsFluidCell().getTemperatureOfBorderCell() * cells.getCell(eachCord).getAsFluidCell().getNumberParticlesOfSingleVirtualCell();

                }
            }
        }
        average/= this.getSizeOfNearFieldCoordinatesForCell(centerCell);
        //end

        Map<Coordinates, Double> result = new HashMap<>();
        Map<Coordinates, Double> resultForVirtualCells = new HashMap<>();
        double sumOfAllDifferences =0;

        List<Double> differences = new ArrayList<>();
        double probe = 0;
        for (Coordinates eachCord: this.getNearFieldCoordinatesForCell(centerCell)){
            double factor = Math.abs(average - cells.getCell(eachCord).getLastValue()*cells.getCell(eachCord).getAsFluidCell().getLastNumberParticles());
            factor += 1.0;
            differences.add(factor);

            result.put(eachCord,factor);
            sumOfAllDifferences += factor;
            if (cells.getCell(eachCord).getAsFluidCell().isBorderCell()){
                double virtualFactor=0;
                for (int i=0; i < cells.getCell(eachCord).getAsFluidCell().getNumberOfVirtualBorders(); i++) {
                    virtualFactor= Math.abs(average - cells.getCell(eachCord).getAsFluidCell().getTemperatureOfBorderCell() * cells.getCell(eachCord).getAsFluidCell().getNumberParticlesOfSingleVirtualCell());
                    virtualFactor += 1.0;
                    sumOfAllDifferences += virtualFactor;
                }
                resultForVirtualCells.put(eachCord, virtualFactor);

            }
        }
        //add one to make the equation system solveable
        sumOfAllDifferences += Math.abs(average - cells.getCell(centerCell).getLastValue()*cells.getCell(centerCell).getAsFluidCell().getLastNumberParticles())+1.0;
        for (Coordinates eachCoord: this.getNearFieldCoordinatesForCell(centerCell)){
            result.replace(eachCoord, result.get(eachCoord)/sumOfAllDifferences);
            probe+= result.get(eachCoord);
            if (cells.getCell(eachCoord).getAsFluidCell().isBorderCell()){
                resultForVirtualCells.replace(eachCoord, resultForVirtualCells.get(eachCoord)/sumOfAllDifferences);


            }
        }

        if (probe> 1.1 || probe< 0.9){
            logger.logMessage(HeatequationLogger.LogLevel.ERROR, "factors are not correct for cell " + centerCell + " probe: " + probe);
        }

        if (this.factorMap.containsKey(centerCell)){
            this.factorMap.replace(centerCell, result);
            } else {
            this.factorMap.put(centerCell, result);
        }
        if (this.factorsForVirtualBorderCells.containsKey(centerCell)){
            this.factorsForVirtualBorderCells.replace(centerCell, resultForVirtualCells);
        } else {
            this.factorsForVirtualBorderCells.put(centerCell, resultForVirtualCells);
        }
    }

    public double getFactorFor(Coordinates centerCell, Coordinates targetCell) throws Exception{
        if (this.factorMap.get(centerCell).get(targetCell)!=null){
            return this.factorMap.get(centerCell).get(targetCell);
        } else {
            throw new Exception("Coult not get factor for flow from " + centerCell + " to " + targetCell);
        }

    }

    public double getPressure() {
        return this.pressure;
    }

    public int getNumberVirtualCells(){
        return this.borderCellsWithVirtualCells.size();
    }

    private void setBorderCellsWithVirtualCells(Cells cells){
        this.borderCellsWithVirtualCells= new ArrayList<>();
        if(this.isFluid){
            for(Coordinates coord : this.coords){
                if(cells.getCell(coord).getAsFluidCell().isBorderCell()){
                    this.borderCellsWithVirtualCells.add(coord);
                }
            }
        }
    }

    private List<Coordinates> getCoordsThatNotInArray(List<Coordinates> list, List<Coordinates> allCoordinates){

        List<Coordinates> result = new ArrayList<>();
        for (Coordinates newCoordinate: allCoordinates){
            if (!this.isCoordinateInList(newCoordinate, list)){
                result.add(newCoordinate);
            }
        }
        return result;
    }

    private boolean isCoordinateInList(Coordinates newCoord, List<Coordinates> list){
        for (Coordinates eachCoord: list){
            if (newCoord.equals(eachCoord)){
                return true;
            }

        }
        return false;
    }

    private void getNearFieldApproximation(Space space){
        if (!this.isFluid){
            return;
        }
        this.nearFieldMap = new HashMap<>();
        this.sizeNearField = new HashMap<>();
        for (Coordinates eachCoord: this.coords){
            this.nearFieldMap.put(eachCoord, this.getClosest100Cells(eachCoord, space));
        }
    }


    private List<Coordinates> getClosest100Cells(Coordinates centerCell, Space space){
        List<Coordinates> result = new ArrayList<>();
        List<Coordinates> lastStep = new ArrayList<>();
        List<Coordinates> nextStep = new ArrayList<>();

        Integer size = 0;
        int currentSize = 0;
        if (space.allCells.getCell(centerCell).isFluid && space.allCells.getCell(centerCell).getAsFluidCell().isBorderCell()){
            currentSize += space.allCells.getCell(centerCell).getAsFluidCell().getNumberOfVirtualBorders();
            nextStep.add(centerCell);
        }
        nextStep.addAll(space.allCells.getAllAdjacentFluidCells(centerCell));

        while (size + nextStep.size()+ currentSize< 100){

            size += nextStep.size() + currentSize;
            currentSize = 0;

            result.addAll(nextStep);
            lastStep.clear();
            lastStep.addAll(nextStep);
            nextStep.clear();

            for (Coordinates cellFromLastStep: lastStep){
                nextStep.addAll(space.allCells.getAllAdjacentFluidCells(cellFromLastStep));
            }
            nextStep = this.removeDoubleEntriesFromList(nextStep);
            nextStep = this.getCoordsThatNotInArray(result,nextStep);
            nextStep = this.removeCoordinateFomList(nextStep, centerCell);

            for (Coordinates eachCell: nextStep){
                if (space.allCells.getCell(eachCell).isFluid && space.allCells.getCell(eachCell).getAsFluidCell().isBorderCell()){
                    currentSize += space.allCells.getCell(eachCell).getAsFluidCell().getNumberOfVirtualBorders();
                }
            }


            if (nextStep.isEmpty()){
                this.sizeNearField.put(centerCell, size);
                return result;

            }
        }


        //this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "needs to add " + difference + " cells from " + nextStep.size() +" cells: " + nextStep.toString());
        int range = nextStep.size();

        while (size < 100){
            int randomInt = (int) (Math.random()*range);
            while (this.isCoordinateInList(nextStep.get(randomInt), result)){
                randomInt = (int) (Math.random()*range);
            }
            result.add(nextStep.get(randomInt));
            size ++;
            if (space.allCells.getCell(nextStep.get(randomInt)).getAsFluidCell().isBorderCell()){
                size += space.allCells.getCell(nextStep.get(randomInt)).getAsFluidCell().getNumberOfVirtualBorders();
            }

            nextStep.remove(randomInt);
            range --;

        }
        this.sizeNearField.put(centerCell, size);

        return result;
    }

    private List<Coordinates> removeCoordinateFomList(List<Coordinates> nextStep, Coordinates centerCell) {
        for (int i=0; i<nextStep.size(); i++){
            if (centerCell.equals(nextStep.get(i))){
                nextStep.remove(i);
                return nextStep;
            }
        }
        return nextStep;
    }

    public void setMeanValues(double[] values){
        this.meanValues=values;
    }

    public double getMeanValueForY(int yValue){
        return this.meanValues[yValue - minY];
    }

    public List<Coordinates> getCellsForLayer(int layer){
        return yLayers.get(layer-minY);
    }

    private void setYLayers(){
        this.yLayers = new ArrayList<>();
        for (int layer = this.minY; layer <= this.maxY; layer++){
            List<Coordinates> tempList = new ArrayList<>();
            for(Coordinates coord: this.coords){
                if (coord.y == layer){
                    tempList.add(coord);
                }
            }
            yLayers.add(tempList);
        }
    }





    public boolean isFluid(){
        return this.isFluid;
    }

    private void initCoords(Cells allCells, Coordinates startCell){

        coords= new ArrayList<>();
        coords.add(startCell);
        List<Coordinates> lastStepList = new ArrayList<>();
        List<Coordinates> currentStepList = new ArrayList<>();
        //init latest cells
        lastStepList = allCells.getAllAdjacentCellsForIsFluid(startCell, this.isFluid);
        //while latest cells not empty create latest cells again
        while(!lastStepList.isEmpty()){
            for(Coordinates currentCoord: lastStepList){
                currentStepList.addAll(allCells.getAllAdjacentCellsForIsFluid(currentCoord, this.isFluid));
            }
            lastStepList = this.getCoordsNotInArea(currentStepList);
            lastStepList = this.removeDoubleEntriesFromList(lastStepList);
            currentStepList.clear();
            this.coords.addAll(lastStepList);
        }
    }

    private int setMaxY(){
        int maxY= coords.get(0).y;
        for(Coordinates coord: this.coords){
            if (maxY < coord.y){
                maxY=coord.y;
            }
        }
        return maxY;
    }

    private int setMinY(){
        int minY= coords.get(0).y;
        for(Coordinates coord: this.coords){
            if (minY > coord.y){
                minY=coord.y;
            }
        }
        return minY;
    }

    private List<Coordinates> removeDoubleEntriesFromList(List<Coordinates> currentList){
        List<Coordinates> result = new ArrayList<>();
        for(Coordinates tempCoord: currentList){
            if (!this.cellContainedInList(tempCoord, result)){
                result.add(tempCoord);
                //this.coords.add(tempCoord);
            }
        }
        return result;
    }

    private List<Coordinates> getCoordsNotInArea(List<Coordinates> currentList){
        List<Coordinates> result = new ArrayList<>();
        for(Coordinates tempCoord: currentList){
            if (!this.cellContainedInCoordinates(tempCoord)){
                result.add(tempCoord);
                //this.coords.add(tempCoord);
            }
        }
        return result;
    }

    private boolean cellContainedInCoordinates(Coordinates coord){
        return this.cellContainedInList(coord, this.coords);
    }

    private boolean cellContainedInList(Coordinates coord, List<Coordinates> list){
        for (Coordinates listMember: list){
            if (listMember.equals(coord)){
                return true;
            }
        }
        return false;
    }


    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getListIndexForCell(Coordinates targetCell){
        return this.systemOfEquationsMapping.get(targetCell);
    }

    public void setIndexCoordinatesMapping(){
        int i = 0;
        this.systemOfEquationsMapping=new HashMap<>();
        this.coordinatesToIndexMapping= new HashMap<>();
        this.systemOfEquationsMappingForVirtualCells=new HashMap<>();
        this.coordinatesToIndexMappingForVirtualCells= new HashMap<>();
        for (Coordinates coordinates: this.coords){
            this.systemOfEquationsMapping.put(coordinates, i);
            this.coordinatesToIndexMapping.put(i, coordinates);
            i++;
        }
        for(Coordinates virtualCells: this.borderCellsWithVirtualCells){
            this.systemOfEquationsMappingForVirtualCells.put(virtualCells, i);
            this.coordinatesToIndexMappingForVirtualCells.put(i, virtualCells);
            i++;
        }
    }

    public Coordinates getCoordinatesForListIndex(int index) throws Exception{
        return this.coordinatesToIndexMapping.get(index);
    }

    public int getSizeOfNearFieldCoordinatesForCell(Coordinates centerCell){
        return this.sizeNearField.get(centerCell);
    }

    public List<Coordinates> getNearFieldCoordinatesForCell(Coordinates centerCell){
        return this.nearFieldMap.get(centerCell);
    }

    public int getListIndexForVirtualCell(Coordinates centerCoordinates) {
        return systemOfEquationsMappingForVirtualCells.get(centerCoordinates);
    }


    public boolean isBorderCell(Coordinates coordinates){
        return this.borderCellsWithVirtualCells.contains(coordinates);
    }

    public List<Coordinates> getborderCellsWithVirtualCells(){
        return this.borderCellsWithVirtualCells;
    }

    public void calcAverages(Cells cells) {
        this.particleSum = 0;
        this.averageTermperature = 0;
        if(this.isFluid) {
            for (Coordinates eachCoordinate : this.coords) {
                this.particleSum +=  cells.getCell(eachCoordinate).getAsFluidCell().getNumberParticles();
                this.averageTermperature +=  cells.getCell(eachCoordinate).getAsFluidCell().getValue();
            }
        }
        averageTermperature /= (double) this.coords.size();
    }

    public double getPressureCalculationFailure(Cells cells){

        double sumOfAllDifferences = 0;
        double absoluteSumOfAllDifferences = 0;
        BigDecimal sumOfAllBigDeciDifferences = BigDecimal.valueOf(0);
        for (Coordinates eachCell: this.coords){
            //double eachPressure = space.allCells.getCell(eachCell).getValue()*space.allCells.getCell(eachCell).getAsFluidCell().getNumberParticles()*space.allCells.gasConstant;
            double eachPressure = cells.getCell(eachCell).getAsFluidCell().getPressure();
            double difference = this.pressure - eachPressure;
            sumOfAllDifferences += difference;
        }

        return sumOfAllDifferences;
    }


    public void printPressureForAllCells(Space space) {


        double tempPressure =0;
        BigDecimal tempPressureDeci = new BigDecimal(0);
        for (Coordinates eachCell : this.coords) {
            space.allCells.getCell(eachCell).getAsFluidCell().calculatePressure(space.allCells.gasConstant, 1);
            //this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "pressure for cell " + eachCell.toString() + " is "+  space.getCell(eachCell).getAsFluidCell().getPressure());
            tempPressure += space.allCells.getCell(eachCell).getAsFluidCell().getPressure();
            tempPressureDeci = tempPressureDeci.add(space.allCells.getCell(eachCell).getAsFluidCell().getPressureAsBigDecimal());
        }
        tempPressureDeci = tempPressureDeci.divide(BigDecimal.valueOf(this.coords.size()), 50, RoundingMode.HALF_UP);


        tempPressure /= this.coords.size();
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "pressure calculated to " + tempPressure + " with average pressure of all cells. absolute value: " + tempPressureDeci);

        double difference = tempPressure - this.pressure;
        BigDecimal differenceDeci = tempPressureDeci.subtract(this.pressureDeci);
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "Failure from " + difference + ". absolute value: " + differenceDeci.toString());

    }

    public void applyParticleFlowFromBorderCells(Space space){
        this.calcDivergenceFromPressure(space);

    }

    private void calcDivergenceFromPressure(Space space) {
        this.pressureDivergence = 0;

        for(Coordinates eachCell: this.coords){
            this.pressureDivergence = this.getEnergyDifferenceToIdealValue(space, eachCell);

        }
    }

    private double getEnergyDifferenceToIdealValue(Space space, Coordinates eachCell) {
        return this.energyPerCell - space.allCells.getCell(eachCell).getValue();
    }


        /*
        StringBuilder builder = new StringBuilder("Pressure: " + this.pressure + "\nPressure of each cell: \n");
        double sumOfAllDifferences = 0;
        double absoluteSumOfAllDifferences = 0;
        BigDecimal sumOfAllBigDeciDifferences = BigDecimal.valueOf(0);
        for (Coordinates eachCell: this.coords){
            //double eachPressure = space.allCells.getCell(eachCell).getValue()*space.allCells.getCell(eachCell).getAsFluidCell().getNumberParticles()*space.allCells.gasConstant;
            double eachPressure = space.allCells.getCell(eachCell).getAsFluidCell().getPressure();
            double difference = this.pressure - eachPressure;
            BigDecimal eachPressureDeci = space.allCells.getCell(eachCell).getAsFluidCell().getPressureAsBigDecimal();
            BigDecimal differenceDeci = this.pressureDeci.subtract(eachPressureDeci);
            sumOfAllBigDeciDifferences = sumOfAllBigDeciDifferences.add(differenceDeci);
            absoluteSumOfAllDifferences += Math.abs(difference);
            sumOfAllDifferences += difference;
            builder.append(eachCell.toString() + ": " + difference + " for pressure: " + eachPressure + " bigDeci: " + differenceDeci.toString() + "\n");

        }
        this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, builder.toString());
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "sum of all differences: " + sumOfAllDifferences + " absolute: " + absoluteSumOfAllDifferences + " bigDeci: " +sumOfAllBigDeciDifferences.toString());
        }
        */


    public BigDecimal getBigDeciPressure() {
        return this.pressureDeci;
    }

    public double getFactorForVirtualCells(Coordinates centerCoordinates, Coordinates otherCell) {
        return this.factorsForVirtualBorderCells.get(centerCoordinates).get(otherCell);
    }

    public boolean isIsobar() {
        return this.isIsobar;
    }
}
