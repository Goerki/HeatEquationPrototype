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
    private boolean isIsobar;
    private HeatequationLogger logger;
    private Map<Coordinates, List<Junction>> validJunctionMap;
    private Map<Coordinates, List<Junction>> incomingJunctionMap;
    private int numberJunctions;
    private int numberVirtualJunctions;
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
            //this.normalization = space.allCells.gasConstant / space.getCellLength()/space.getCellLength()/space.getCellLength()/(double) this.coords.size();
        } else {
                        this.isIsobar = true;

        }
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
        this.incomingJunctionMap = new HashMap<>();
        this.numberJunctions = 0;
        this.numberVirtualJunctions =0;
        for (Coordinates eachCoord: this.coords){
            this.incomingJunctionMap.put(eachCoord, null);

            this.validJunctionMap.put(eachCoord, this.getAllValidJunctions(eachCoord, space));
            this.numberJunctions += this.getAllValidJunctions(eachCoord, space).size();
            this.numberVirtualJunctions += space.allCells.getCell(eachCoord).getAsFluidCell().getNumberOfVirtualBorders();
        }

        for (Coordinates eachCoord: this.coords){
            for (Junction eachJunction: this.validJunctionMap.get(eachCoord)) {


                this.addJunctionToIncomingJunctionMap(eachJunction);

            }
        }


    }

    private void addJunctionToIncomingJunctionMap(Junction eachJunction) {
        List<Junction> newList;
        if(this.incomingJunctionMap.get(eachJunction.getTo()) == null){
            newList = new ArrayList<>();
        } else {
            newList = this.incomingJunctionMap.get(eachJunction.getTo());
        }
        newList.add(eachJunction);
        this.incomingJunctionMap.put(eachJunction.getTo(), newList);
    }

    private List<Junction> getAllValidJunctions(Coordinates centerCoord, Space space) {
        List<Junction> validJunctionList = new ArrayList<>();
        Junction temp;
        for (Coordinates neighbor: space.allCells.getAllAdjacentFluidCells(centerCoord)) {

            try {
                temp = new Junction(centerCoord, neighbor);
                validJunctionList.add(temp);
            } catch (Exception e) {

            }
        }

            if (space.allCells.getCell(centerCoord).getAsFluidCell().isBorderCell()){
                try {
                        temp = new Junction(centerCoord, centerCoord, space);
                        validJunctionList.add(temp);
                } catch (Exception e) {
                    e.printStackTrace();
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
    }

    public Junction getJunctionsForListIndex(int index) throws Exception{
        return this.junctionToIndexMapping.get(index);
    }


    public List<Junction> getOutgoingJunctionsForCell(Coordinates centerCell) throws Exception{
        if (this.validJunctionMap.get(centerCell)==null){
            throw new Exception("no outgoing junction found for cell " + centerCell);

        }
        return this.validJunctionMap.get(centerCell);
    }

    public List<Junction> getIncomingJunctionsForCell(Coordinates centerCell) throws Exception{
        if (this.incomingJunctionMap.get(centerCell)==null){
            throw new Exception("no incoming junction found for cell " + centerCell);

        }
        return this.incomingJunctionMap.get(centerCell);
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




    public boolean isIsobar() {
        return this.isIsobar;
    }


    public int getNumberJunctions() {
        return numberJunctions;
    }
}
