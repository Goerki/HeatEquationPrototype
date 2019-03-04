import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Cells.Cells;
import Cells.CellArea;
import  Cells.Coordinates;
import Cells.Material;
import  Cells.Cell;
import  Cells.FluidCell;

public class Space implements Serializable {
    Cells allCells;
    int sizeX;
    int sizeY;
    int sizeZ;
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


    Space(int sizeX, int sizeY, int sizeZ, double startValue, Material material, int numberThreads){
        this.sizeX= sizeX;
        this.sizeY= sizeY;
        this.sizeZ= sizeZ;
        this.allCells = new Cells(sizeX, sizeY, sizeZ, startValue, material);
        this.numberThreads=numberThreads;
        this.cellLength = 1;

    }

    public String createCube(int x1, int y1, int z1, int x2, int y2, int z2, Material material){
        if (material.isSolid()){
            return this.allCells.makeCubeSolidCells(x1, y1, z1, x2, y2, z2, material);
        } if (material.isFluid()){
            return this.allCells.makeCubeFluidCells(x1,y1,z1,x2,y2,z2,material);
        }
        else{
            return "please check material definition in json file";
        }

    }
    public double getMinimumTemperature(){
        return allCells.getMinimumTemperature();
    }
    public double getMaximumTemperature(){
        return allCells.getMaximumTemperature();
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

    boolean initialize(double time, double deltaT, int numberThreads){
        //Create areas
        if (this.isInitialized)
            return false;

        this.isInitialized=true;
        this.createAreas();
        this.deltaT = deltaT;
        this.numberCalculatedSteps = 0;


        // TODO: calculate steps and delta T
          this.numberSteps = (int)(time/deltaT);

        //create Threads
        this.numberThreads = numberThreads;
        this.createCalculationThreads(this.numberThreads, numberSteps);
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


    private void createCalculationThreads(int numberThreads, int steps){
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
        allCells.getCell(coords).setValue(0);
            Coordinates ausgabe = new Coordinates(0,7,2);
        for(Coordinates neighbourCell: allCells.getAllAdjacentCellsForSolidCalculation(coords)) {
            if (allCells.getCell(coords).isFluid()){
                allCells.getCell(coords).addToValue((allCells.getCell(neighbourCell).getLastValue() -allCells.getCell(coords).getLastValue()) *allCells.getCell(coords).getAsFluidCell().getNusseltNumber());
            } else {
                double diff = allCells.getCell(neighbourCell).getLastValue() -allCells.getCell(coords).getLastValue();
                allCells.getCell(coords).addToValue(allCells.getCell(neighbourCell).getLastValue() -allCells.getCell(coords).getLastValue() );
            }
        }
        if (coords.equals(ausgabe)){
            System.out.print("\ndifference before: " + allCells.getCell(coords).getValue());
        }
        allCells.getCell(coords).setValue(allCells.getCell(coords).getValue() *this.deltaT*allCells.getCell(coords).getAlpha()/this.cellLength);

        if (coords.equals(ausgabe)){
            System.out.print("\ndifference after: " + allCells.getCell(coords).getValue());
        }
        allCells.getCell(coords).addToValue(allCells.getCell(coords).getLastValue());
        allCells.getCell(coords).addToValue(allCells.getCell(coords).getHeatFlow()*this.deltaT);
        if (coords.equals(ausgabe)){
            System.out.print("\nZelle " + ausgabe + " : temperatur " + allCells.getCell(coords).getValue());
        }

    }


    public boolean calculationReady(){
       return this.numberCalculatedSteps >= this.numberSteps;

    }

    public double getPercentageOfStatus(){
        System.out.print("\ncalculatedSteps: " + numberCalculatedSteps + " numberSteps "+ numberSteps + "bruch: " + (double)numberCalculatedSteps/(double)numberSteps);
        return ((double)numberCalculatedSteps/(double)numberSteps * 100.0);
    }

    public void increaseNumberCalculatedSteps(){
        numberCalculatedSteps++;
    }

    public void calculateParticleFlowForCells(List<Coordinates> fluidCells){
        for (Coordinates coord:fluidCells){
            this.calculateParticleFlow(coord);
        }
    }

    private void calculateParticleFlow(Coordinates coord){
        double diffusion = this.calcDiffusionForCell(coord);
        double convection = 0;
        //System.out.print("diffussion: " + diffusion);
        for(Coordinates neighbor:allCells.getAllAdjacentFluidCells(coord)){
            this.particleFlowFromTo(coord, neighbor, diffusion);
        }
        allCells.getCell(coord).getAsFluidCell().calcDiffussionToBorderCell(diffusion);
        if (allCells.cellExists(coord.getCellYPlus1()) && allCells.getCell(coord.getCellYPlus1()).isFluid()){
            convection = this.calcConvectionForCell(coord);
            //System.out.print("convection: " + convection);

            this.particleFlowFromTo(coord, coord.getCellYPlus1(), convection);
        }
        if(allCells.getCell(coord).getAsFluidCell().hasBorderCellOnTop()){
            allCells.getCell(coord).getAsFluidCell().calcConvectionOverBorder(this.calcConvectionForCell(coord));
        }
        double diff = allCells.getCell(coord).getAsFluidCell().getLastNumberParticles() - allCells.getCell(coord).getAsFluidCell().getNumberParticles();
        System.out.print("\nflow for " + coord.toString() + " : " + diff + " for lasNumberPart " + allCells.getCell(coord).getAsFluidCell().getLastNumberParticles());
    }

    private double calcDiffusionForCell(Coordinates cell){
        double baseFactor = 0.05;
        return baseFactor*allCells.getCell(cell).getValue()*allCells.getCell(cell).getAlpha()*deltaT*allCells.getCell(cell).getAsFluidCell().getLastNumberParticles();
    }

    private void particleFlowFromTo(Coordinates source, Coordinates target, double amount){
        allCells.getCell(source).getAsFluidCell().addToAbsoluteNumberParticles(-amount, allCells.getCell(target).getLastValue());
        allCells.getCell(target).getAsFluidCell().addToAbsoluteNumberParticles(amount, allCells.getCell(source).getLastValue());
    }

     private double calcConvectionForCell(Coordinates coordinates){
        double baseFactor = 0.05;
        baseFactor *= (this.allCells.getCell(coordinates).getLastValue() - getMeanValueForAreaAndLayer(coordinates, this.allCells.getCell(coordinates).getAsFluidCell()));
        baseFactor *= this.allCells.getCell(coordinates).getAlpha()*this.allCells.getCell(coordinates).getAsFluidCell().getLastNumberParticles();

        return baseFactor;
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
