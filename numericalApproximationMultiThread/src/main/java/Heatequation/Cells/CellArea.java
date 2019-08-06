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
    private Map<Coordinates, List<Junction>> validJunctionMap;
    private Map<Coordinates, List<Junction>> completeJunctionMap;
    private Map<Coordinates, Integer> sizeNearField;
    private int numberJunctions;
    private Map<Junction, Integer> systemOfEquationsMapping;
    private Map<Integer,Junction> junctionToIndexMapping;
    private Map<Junction, Integer> systemOfEquationsMappingForVirtualCells;
    private Map<Integer,Junction> junctionsToIndexMappingForVirtualCells;



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

        setYLayers();
        this.setBorderCellsWithVirtualCells(space.allCells);

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
            this.createJunctions(space);
            this.setIndexJunctionMapping();
        }



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

    private void createJunctions(Space space){
        if (!this.isFluid){
            return;
        }
        this.validJunctionMap = new HashMap<>();
        this.sizeNearField = new HashMap<>();
        this.completeJunctionMap = new HashMap<>();
        this.numberJunctions = 0;
        for (Coordinates eachCoord: this.coords){

            this.validJunctionMap.put(eachCoord, this.getAllValidJunctions(eachCoord, space));
            this.numberJunctions += this.getAllValidJunctions(eachCoord, space).size();
        }

        //test if system can still be solved
        for (Coordinates centerCell: this.validJunctionMap.keySet()) {
            List<Junction> completeList = new ArrayList<>();
            for (Coordinates eachCell: this.validJunctionMap.keySet())  {
                for (Junction eachJunction : this.validJunctionMap.get(eachCell)) {
                    if (eachJunction.getFrom().equals(centerCell) || eachJunction.getTo().equals(centerCell)) {
                        if (!completeList.contains(eachJunction)) {
                            completeList.add(eachJunction);
                        }
                    }
                }
            }
            completeJunctionMap.put(centerCell, completeList);
        }
    }

    private List<Junction> getAllValidJunctions(Coordinates centerCoord, Space space) {
        List<Junction> validJunctionList = new ArrayList<>();
        for (Coordinates neighbor: space.allCells.getAllAdjacentFluidCells(centerCoord)){
            Junction temp;
            try {
                temp = new Junction(centerCoord, neighbor);
                validJunctionList.add(temp);
            } catch (Exception e) {

            }
        }
        return validJunctionList;
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

    public int getListIndexForJunction(Junction junction){
        return this.systemOfEquationsMapping.get(junction);
    }

    public void setIndexJunctionMapping(){
        int i = 0;
        this.systemOfEquationsMapping=new HashMap<>();
        this.junctionToIndexMapping = new HashMap<>();
        this.systemOfEquationsMappingForVirtualCells=new HashMap<>();
        this.junctionsToIndexMappingForVirtualCells = new HashMap<>();
        for (Coordinates coordinates: this.coords){
            for (Junction eachJunction: this.validJunctionMap.get(coordinates)){
                this.systemOfEquationsMapping.put(eachJunction, i);
                this.junctionToIndexMapping.put(i, eachJunction);
                i++;
            }

        }
        for(Coordinates virtualCells: this.borderCellsWithVirtualCells) {
            for (Junction eachJunction : this.validJunctionMap.get(virtualCells)) {
                this.systemOfEquationsMappingForVirtualCells.put(eachJunction, i);
                this.junctionsToIndexMappingForVirtualCells.put(i, eachJunction);
                i++;
            }
        }
    }

    public Junction getCoordinatesForListIndex(int index) throws Exception{
        return this.junctionToIndexMapping.get(index);
    }

    public int getSizeOfNearFieldCoordinatesForCell(Coordinates centerCell){
        return this.sizeNearField.get(centerCell);
    }

    public List<Junction> getOutgoingJunctionsForCell(Coordinates centerCell){
        return this.validJunctionMap.get(centerCell);
    }

    public List<Junction> getAllJunctionsForCell(Coordinates centerCell){
        return this.completeJunctionMap.get(centerCell);
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
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "pressure for cell " + eachCell.toString() + " is "+  space.allCells.getCell(eachCell).getAsFluidCell().getPressure());
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

        double[] temperatureDifferenceArray = new double[this.getNumberVirtualCells()];
        double temperatureDifferenceSum = 0;
        for(int i=0; i<this.getNumberVirtualCells(); i++){
            temperatureDifferenceArray[i] = (space.allCells.getCell(this.getborderCellsWithVirtualCells().get(i)).getValue() -  space.allCells.getCell(this.getborderCellsWithVirtualCells().get(i)).getAsFluidCell().getTemperatureOfBorderCell())* space.allCells.getCell(this.getborderCellsWithVirtualCells().get(i)).getAsFluidCell().getNumberOfVirtualBorders();
            temperatureDifferenceSum +=temperatureDifferenceArray[i];
        }

        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "energy difference calculated to " + this.pressureDivergence);

        double probe = 0;
        double[] einzelnerWert = new double[this.getNumberVirtualCells()];

        if (temperatureDifferenceSum!=0) {
            for (int i = 0; i < this.getNumberVirtualCells(); i++) {
                einzelnerWert[i] = temperatureDifferenceArray[i] / temperatureDifferenceSum;
                einzelnerWert[i] = einzelnerWert[i] * this.pressureDivergence / space.allCells.getCell(this.getborderCellsWithVirtualCells().get(i)).getAsFluidCell().getTemperatureOfBorderCell();
                //for testing reasons only
                //einzelnerWert[i] = 1.0/(double)this.getNumberVirtualCells();
                space.particleFlowFromVirtualCell(this.getborderCellsWithVirtualCells().get(i), einzelnerWert[i]);
                probe += einzelnerWert[i];
            }
        }
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "Probe calculated to " + probe);

        probe *= 300;
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "energy adds to " + probe);
    }

    private void calcDivergenceFromPressure(Space space) {
        this.pressureDivergence = 0;

        for(Coordinates eachCell: this.coords){
            this.pressureDivergence += this.getEnergyDifferenceToIdealValue(space, eachCell);
        }
    }

    private double getEnergyDifferenceToIdealValue(Space space, Coordinates eachCell) {
        return this.energyPerCell-space.allCells.getCell(eachCell).getValue()*space.allCells.getCell(eachCell).getAsFluidCell().getNumberParticles();
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


    public boolean isIsobar() {
        return this.isIsobar;
    }


    public int getNumberJunctions() {
        return numberJunctions;
    }
}
