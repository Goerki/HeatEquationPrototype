package Heatequation.Cells;

import Heatequation.HeatequationLogger;
import Heatequation.Space;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CellArea implements Serializable {
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
    private double normalization;
    private boolean isIsochor;
    private boolean isIsobar;
    private HeatequationLogger logger;
    private Map<Coordinates, List<Coordinates>> nearFieldMap;
    private Map<Coordinates, Integer> systemOfEquationsMapping;



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
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "area is isobar. pressure set to " + this.pressure);
        } else{
            this.calcAverages(space);
            this.pressure = this.particleSum*this.averageTermperature*this.normalization;
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "area is not isobar. pressure set to " + this.pressure +  " from particleSum " + this.particleSum + " average Temperature " + averageTermperature + " and normalization " + this.normalization);
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
        for (Coordinates eachCoord: this.coords){
            this.nearFieldMap.put(eachCoord, this.getClosest100Cells(eachCoord, space));
        }
    }


    private List<Coordinates> getClosest100Cells(Coordinates centerCell, Space space){
        List<Coordinates> result = new ArrayList<>();
        List<Coordinates> lastStep = new ArrayList<>();
        List<Coordinates> nextStep = new ArrayList<>();

        nextStep.addAll(space.allCells.getAllAdjacentFluidCells(centerCell));

        while (result.size() + nextStep.size() < 100){
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


            if (nextStep.isEmpty()){
                return result;

            }
        }

        int difference = 100 - result.size();
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "needs to add " + difference + " cells from " + nextStep.size() +" cells: " + nextStep.toString());
        int range = nextStep.size();

        for (int i =0; i<difference; i++){
            int randomInt = (int) (Math.random()*range);
            while (this.isCoordinateInList(nextStep.get(randomInt), result)){
                randomInt = (int) (Math.random()*range);
            }
            result.add(nextStep.get(randomInt));
            nextStep.remove(randomInt);
            range --;

        }

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
        int i = 0;
        for (Coordinates coordinates: this.coords){
            if (targetCell.equals(coordinates)){
                return i;
            }
            i++;
        }
        return -1;
    }

    public Coordinates getCoordinatesForListIndex(int index) throws Exception{
        if (this.coords.size()>index){
            return this.coords.get(index);
        } else {
            throw new Exception("corrdinates not found for index " + index + " in area " + this.coords.toString());
        }
    }

    public List<Coordinates> getNearFieldCoordinatesForCell(Coordinates centerCell){
        return this.nearFieldMap.get(centerCell);
    }

    public int getListIndexForVirtualCell(Coordinates centerCoordinates) {
        int virtualCellIndex = this.getIndexOfCoordinatesInVirtualBorderCellsList(centerCoordinates);
        return virtualCellIndex+this.coords.size();
    }

    private int getIndexOfCoordinatesInVirtualBorderCellsList(Coordinates coordinates){
        int i=0;
        for(Coordinates listCoordinate: this.borderCellsWithVirtualCells){
            if (listCoordinate.equals(coordinates)){
                return i;
            } else {
                i ++;
            }
        }
        return -1;
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
            for (Coordinates eachCoordinate : coords) {
                this.particleSum +=  cells.getCell(eachCoordinate).getAsFluidCell().getNumberParticles();
                this.averageTermperature +=  cells.getCell(eachCoordinate).getAsFluidCell().getValue();
            }
        }
        averageTermperature /= this.coords.size();
    }





}
