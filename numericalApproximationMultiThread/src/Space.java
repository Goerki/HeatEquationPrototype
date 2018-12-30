import Cells.Cell;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import Cells.Cells;
import  Cells.FluidCell;
import Cells.CellArea;
import  Cells.Coordinates;



public class Space {
    Cells allCells;
    int sizeX;
    int sizeY;
    int sizeZ;
    int numberCellsForSolidCalculation;
    CalculationThread[] calculationThreads;
    int numberThreads;
    boolean isInitialized = false;
    SystemOfEquations fluidEquations;
    List<CellArea> areas;

    Space(int sizeX, int sizeY, int sizeZ, double startValue, double startViskosity, int numberThreads){
        this.sizeX= sizeX;
        this.sizeY= sizeY;
        this.sizeZ= sizeZ;
        this.allCells = new Cells(sizeX, sizeY, sizeZ, startValue, startViskosity);
        this.numberThreads=numberThreads;

    }

    public void createSolidCube(int x1, int y1, int z1, int x2, int y2, int z2, double value, double alpha){
        this.allCells.makeCubeSolidCells(x1,y1,z1,x2,y2,z2,value, alpha);
    }

    public void createFluidCube(int x1, int y1, int z1, int x2, int y2, int z2, double value, double viskosity){
        this.allCells.makeCubeFluidCells(x1,y1,z1,x2,y2,z2,value, viskosity);
    }


    boolean initialize(double time){
        //Create areas
        if (this.isInitialized)
            return false;

        this.isInitialized=true;
        this.createAreas();

        // TODO: calculate steps and delta T
        double deltaT = 0.005;
        int steps = (int)(time/deltaT);


        //create Threads

        this.createCalculationThreads(this.numberThreads, steps);

        return true;
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
                areas.add(new CellArea(this.allCells, coord));
                //set all cells inside this area to initialized
                this.allCells.setCellsToInitialized(this.areas.get(this.areas.size()-1).coords);
            }

        }

    }

    private void setNumberSolidCells(){
        this.numberCellsForSolidCalculation = this.allCells.getCellsForSolidCalculation().size();
    }


    private void createCalculationThreads(int numberThreads, int steps){
        this.numberThreads = numberThreads;
        int threadSize = this.numberCellsForSolidCalculation/numberThreads;
        calculationThreads = new CalculationThread[numberThreads];
        int tempStart = 0;
        int tempStop = threadSize;
        List<Coordinates> solidCalcCells = this.allCells.getCellsForSolidCalculation();

        for (int i = 0; i<= numberThreads; i++){

            if (i == numberThreads){

                calculationThreads[i] = new CalculationThread(this, solidCalcCells.subList(tempStart, solidCalcCells.size()),steps);
            } else {
                calculationThreads[i] = new CalculationThread(this, solidCalcCells.subList(tempStart, tempStop), steps);
                tempStart += threadSize;
                tempStop += threadSize-1;
                System.out.print("start: " + tempStart + "end : " + tempStop);
            }

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
            System.out.print("start: " + tempSize + "end : "+ -(tempSize+threadSize-1));
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
            System.out.print("\nLastCellValue: " + cell.getLastValue()+ " LastOwnCellValue: " + ownCell.getLastValue() + " alpha: " + cell.getAlpha());
        }
        */
       // return ((cellA.getLastValue() - cellB.getLastValue())/cellB.getAlpha());
   // }

/*
    private void calcNewValueForSolidCell(int x, int y, int z){
        this.cells[x][y][z].setValue(this.getCell(x,y,z).getValue() + calcSolidTemperatureFlowFromCellAToCellB(this.getCell(x,y,z),this.getCell(x-1,y,z))
                + calcSolidTemperatureFlowFromCellAToCellB(this.getCell(x,y,z),this.getCell(x+1,y,z))
                + calcSolidTemperatureFlowFromCellAToCellB(this.getCell(x,y,z),this.getCell(x,y-1,z))
                + calcSolidTemperatureFlowFromCellAToCellB(this.getCell(x,y,z),this.getCell(x,y+1,z))
                + calcSolidTemperatureFlowFromCellAToCellB(this.getCell(x,y,z),this.getCell(x,y,z-1))
                + calcSolidTemperatureFlowFromCellAToCellB(this.getCell(x,y,z),this.getCell(x,y,z+1)));
    }

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

            System.out.print("iteration " + iteration + "from " + numberIterations);
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
            System.out.print("\nx=" + x);
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
}
