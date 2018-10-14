import Cell.Cell;

import java.util.concurrent.TimeUnit;
import  Cell.SolidCell;
import  Cell.FluidCell;


public class Space {
    Cell[][][] cells;
    int size;
    CalculationThread[] calculationThreads;
    int numberThreads;
    SystemOfEquations fluidEquations;

    Space(int size){
        cells = new Cell[size][size][size];
        this.size = size;
        for (int i=0; i< size; i++){
            for(int j=0;j< size; j++){
                for(int k=0; k<size; k++){
                    cells[i][j][k] = new FluidCell(10, 1000);
                }
            }
        }

        this.createCalculationThreads(1);
/*
        for (int i=20; i< 30; i++){
            for(int j=20;j< 30; j++){
                for(int k=20; k<30; k++){
                    cells[i][j][k].setValue(50);
                }
            }
        }
        */

    }


    double[][] getZLayer(int z){
        double[][] result = new double[size][size];
        for (int x = 0; x < size; x ++){
            for (int y=0;y<size; y++) {
                result[x][y] = this.cells[x][y][z].getDoubleValue();
            }
        }
        return result;
    }

    double[][] getXLayer(int x){
        double[][] result = new double[size][size];
        for (int y=0;y<size; y++) {
            for (int z=0;z<size; z++) {
                result[y][z] = this.cells[x][y][z].getDoubleValue();
            }
        }
        return result;
    }

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
        /*
        if (x==20 && y==20 && z == 20){
            System.out.print("\nLastCellValue: " + cell.getLastValue()+ " LastOwnCellValue: " + ownCell.getLastValue() + " alpha: " + cell.getAlpha());
        }
        */
        return ((cellA.getLastValue() - cellB.getLastValue())/cellB.getAlpha());
    }

    Cell getCell(int x, int y, int z){
        if (x < 0 || x >= size) {
            return null;
        }
        if (y < 0 || y >= size) {
            return null;
        }
        if (z < 0 || z >= size) {
            return null;
        }
        return this.cells[x][y][z];
    }

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
}
