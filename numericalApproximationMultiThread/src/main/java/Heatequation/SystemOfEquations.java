package Heatequation;

import Heatequation.Cells.Cell;
import Heatequation.Cells.CellArea;
import Heatequation.Cells.Cells;
import Heatequation.Cells.Coordinates;
import Heatequation.HeatequationLogger;
import Jama.Matrix;




import java.awt.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SystemOfEquations implements Serializable {

    Matrix equationMatrix;
    Matrix boundaryVector;
    Matrix resultVector;

    CellArea area;
    Cells cells;
    int dimension;
    HeatequationLogger logger;
    private double pressure;
    private BigDecimal energySumDeci;
    private double energySum;
    Coordinates logCoords;

    SystemOfEquations(CellArea area, Cells cells, HeatequationLogger logger){
        this.logger = logger;
        this.area = area;
        this.dimension = area.coords.size();


        this.equationMatrix = new Matrix(this.dimension, this.dimension);
        this.boundaryVector = new Matrix( this.dimension, 1);
        this.resultVector =  new Matrix( this.dimension,1);


        this.cells = cells;
        resetEquationsAndboundaries();
        this.logCoords = new Coordinates(2,2,2);
    }

    private void resetEquationsAndboundaries(){
        for (int i = 0; i < equationMatrix.getColumnDimension(); i++){
            for (int j = 0; j < equationMatrix.getColumnDimension(); j++){
                this.equationMatrix.set(i,j,0.0);
            }
            boundaryVector.set(i,0, 0.0);
            resultVector.set(i,0, 0.0);
        }
    }

    /*
    void fillEquations(CellArea area){
        for (Coordinates centerCell: area.coords){
            int centerIndex = area.getListIndexForCell(centerCell);
            List<Coordinates> adjacentCells = cells.getAllAdjacentFluidCells(centerCell);
            //ausgehende Teilchen
            equations[centerIndex][centerIndex] = 0 - adjacentCells.size();
            for(Coordinates neighborCell :adjacentCells){
                //einkommende Teilchen
                equations[centerIndex][area.getListIndexForCell(neighborCell)] = 1;
            }
            equations[area.coords.size()+1][centerIndex] = 1/cells.getNumberOfAdjacentFluidCells(centerCell);
        }


    }

    */

    public void fillEquations(CellArea area){
        for (Coordinates centerCellCoordinates: area.coords){
            this.addToEquations(centerCellCoordinates, area);

        }
    }

    public void setEnergySum() {
        this.energySumDeci = new BigDecimal(0);
        this.energySum=0;
        for (Coordinates otherCell : this.area.coords) {

            energySum+=this.cells.getCell(otherCell).getAsFluidCell().getLastNumberParticles() * this.cells.getCell(otherCell).getAsFluidCell().getLastValue();
                BigDecimal energy = new BigDecimal(this.cells.getCell(otherCell).getAsFluidCell().getLastNumberParticles() * this.cells.getCell(otherCell).getAsFluidCell().getLastValue());
                this.energySumDeci = energySumDeci.add(energy);
        }
    }



    public void addToEquations(Coordinates centerCoordinates, CellArea area){


        int centerIndex = area.getListIndexForCell(centerCoordinates);
        Cell centerCell = cells.getCell(centerCoordinates);


        if (centerCoordinates.equals(this.logCoords)){
            if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "gotcha");
            }

        }

/*
        BigDecimal energySumDeciForThisCell = new BigDecimal(this.energySumDeci.doubleValue());
        energySumDeciForThisCell = energySumDeciForThisCell.min(new BigDecimal(centerCell.getLastValue() * centerCell.getAsFluidCell().getLastNumberParticles()));

        BigDecimal average = energySumDeciForThisCell.divide(new BigDecimal(this.area.coords.size() -1), 30, RoundingMode.HALF_UP);
        */



        double sumOfAllDifferencesForThisCell = 0;
        for (Coordinates nearFieldCell: this.area.getNearFieldCoordinatesForCell(centerCoordinates)){
            sumOfAllDifferencesForThisCell += this.cells.getCell(nearFieldCell).getAsFluidCell().getLastNumberParticles() * this.cells.getCell(nearFieldCell).getAsFluidCell().getLastValue();

        }
        double average = sumOfAllDifferencesForThisCell/(double) this.area.getNearFieldCoordinatesForCell(centerCoordinates).size();
        double sumOfAllDifferences = 0;
        for (Coordinates nearFieldCell: this.area.getNearFieldCoordinatesForCell(centerCoordinates)){
            sumOfAllDifferences +=1;
            double difference = average - this.cells.getCell(nearFieldCell).getAsFluidCell().getLastNumberParticles() * this.cells.getCell(nearFieldCell).getAsFluidCell().getLastValue();
            if (difference > 0){
                sumOfAllDifferences += difference;
            } else {
                sumOfAllDifferences -= difference;
            }
        }





        //ausgehende Teilchen
        //equations[centerIndex][centerIndex] = -centerCell.getLastValue()*cells.getNumberOfAdjacentFluidCells(centerCoordinates);
        this.equationMatrix.set(centerIndex,centerIndex,-centerCell.getLastValue());

        if (centerCoordinates.equals(this.logCoords)){
            if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "value: " + centerCell.getLastValue());
            }

        }
         for(Coordinates otherCell :this.area.getNearFieldCoordinatesForCell(centerCoordinates)) {
            //einkommende Teilchen
            int neighborIndex = area.getListIndexForCell(otherCell);

            if (centerCoordinates.equals(this.logCoords)){
                if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
                    this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "neighborCell " + otherCell.toString() + " :" + this.cells.getCell(otherCell).getLastValue() / cells.getNumberOfAdjacentFluidCells(otherCell));
                }

            }
            //equations[centerIndex][listIndex] = this.cells.getCell(neighborCell).getLastValue();

            //andere Reihenfolge der indizes

            //equations[centerIndex][neighborIndex] = this.cells.getCell(neighborCell).getLastValue();
            //equations[centerIndex][neighborIndex] = this.cells.getCell(neighborCell).getLastValue()/ cells.getNumberOfAdjacentFluidCells(neighborCell);

             if (!otherCell.equals(centerCoordinates)){
                 //double factor = this.cells.getCell(otherCell).getLastValue()*this.cells.getCell(otherCell).getAsFluidCell().getLastNumberParticles()/energySum;
                 double factor = (this.cells.getCell(otherCell).getLastValue()*this.cells.getCell(otherCell).getAsFluidCell().getLastNumberParticles() - average +1)/sumOfAllDifferences;

                 //equations[neighborIndex][centerIndex] = factorDeci.doubleValue() * this.cells.getCell(centerCoordinates).getLastValue();
                 this.equationMatrix.set(neighborIndex,centerIndex,factor * this.cells.getCell(centerCoordinates).getLastValue());
             }
        }



            //virtual bordercells einkommend
            int virtualBorderCellIndex = -1;
            if (centerCell.getAsFluidCell().isBorderCell()) {
                virtualBorderCellIndex = area.getListIndexForVirtualCell(centerCoordinates);
                //equations[centerIndex][virtualBorderCellIndex] = centerCell.getAsFluidCell().getTemperatureOfBorderCell() * centerCell.getAsFluidCell().getNumberOfVirtualBorders();

                //virtual cell ausgehend ... N1T1 = N2T2
                this.equationMatrix.set(virtualBorderCellIndex,virtualBorderCellIndex,-centerCell.getAsFluidCell().getTemperatureOfBorderCell());
                this.equationMatrix.set(centerIndex,virtualBorderCellIndex,centerCell.getAsFluidCell().getLastValue());

                boundaryVector.set(virtualBorderCellIndex,0, 0.0);

                //equations[virtualBorderCellIndex][listIndex] = this.cells.getCell(neighborCell).getLastValue()/cells.getNumberOfAdjacentFluidCells(neighborCell);

            }



        //boundary condition
 //       equations[area.coords.size()][centerIndex] = 1/cells.getNumberOfAdjacentFluidCells(centerCoordinates);

        //boundaries
        if (centerCoordinates.equals(this.logCoords)){
            if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "result " + (Cells.cellSize * Cells.cellSize * Cells.cellSize / Cells.gasConstant * this.pressure - centerCell.getLastValue() * centerCell.getAsFluidCell().getLastNumberParticles()));
            }

        }

        //boundaries[centerIndex] = Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant -centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles();

        boundaryVector.set(centerIndex,0,  Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant*this.pressure -centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles());


    }

    public void solveEquations(){
        //double[][] dreieck = this.dreiecksMatrix();
        //this.getResultsforDreieck(dreieck);


        long start = System.currentTimeMillis();





        try {
            this.resultVector = this.equationMatrix.solve(boundaryVector);



            long duration = System.currentTimeMillis() -  start;
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO,"creation of the System with " + this.dimension + " lines took " + duration + "milliseconds");



            System.out.print(resultVector.toString());
        } catch (Exception e) {
            System.out.print("\n\nMARIX HAT KEINEN VOLLEN RANG!!\n");
            if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "\n\nMARIX HAT KEINEN VOLLEN RANG!!\n");
                this.draw();
            }
        }

    }

    private void getResultsforDreieck(double[][] dreieck) {

    }


    private double[] multiplyLineWith(double[] line, double factor){
        for (int i =0; i < line.length; i++){
            line[i]*= factor;
        }
        return line;
    }



    private double[] substractTwoLinesAndMultiplyTheSecondWith(double[] line1, double[] line2, double factor){
        double[] result = new double[line1.length];
        for(int i =0; i<line1.length; i++){
            result[i] = line1[i] - (line2[i]*factor);
           /* if (result[i]< 0.00001 && result[i] > -0.00001){
                result[i] =0;

            }
            */
        }
        return result;
    }

    public double getResultForListIndex(int index){
        return resultVector.get(index,0);
    }

    public double getResultForCoordinates(Coordinates coord) throws Exception {
        int index = this.area.getListIndexForCell(coord);
        return resultVector.get(index,0);
    }

    public double getResultForVirtualCell(Coordinates centerCell) {
        return resultVector.get(area.getListIndexForVirtualCell(centerCell),0);
    }

    public void fillBorderBoundaries() {
        /*
        for(Coordinates virtualCoords: this.area.getborderCellsWithVirtualCells()){
            equations[this.dimension-1][this.area.getListIndexForVirtualCell(virtualCoords)] = 1;

        }
        this.area.calcAverages(this.cells);
        this.boundaries[dimension-1] = this.area.particleSum * this.area.averageTermperature;

*/
    }

    public void draw(){
        if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
            this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, this.toString());
        }
    }

    public String toString(){
        StringBuilder builder = new StringBuilder("SYSTEM OF EQUATIONS: \n");
        int virtualCellCounter = 1;
        double absoluteSum =0;
        double relativeSum =0;
        for (int line =0 ; line < this.equationMatrix.getColumnDimension(); line++){
            if (line >= this.area.coords.size()){
                builder.append("virtual " + virtualCellCounter + " : ");
                virtualCellCounter++;
            } else {
                try {
                    builder.append(line+  " : "+ this.area.getCoordinatesForListIndex(line).toString() + " with " + cells.getNumberOfAdjacentFluidCells(area.getCoordinatesForListIndex(line)) + " neighborCells : ");
                } catch (Exception e) {
                    e.printStackTrace();
                    builder.append(e.toString());
                }

            try {
                builder.append("T0 = " + this.cells.getCell(area.getCoordinatesForListIndex(line)).getLastValue() + " N0 = " + this.cells.getCell(area.getCoordinatesForListIndex(line)).getAsFluidCell().getLastNumberParticles());
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
            for (int column =0 ; column < this.equationMatrix.getColumnDimension(); column++){
                builder.append(" " + this.equationMatrix.get(line, column) + " ");
            }
            builder.append(" = " + this.boundaryVector.get(line,0) + " ");
            builder.append(" Ergebnis: " + this.resultVector.get(line,0)+ " \n");
            absoluteSum += this.resultVector.get(line,0);
            if (line >= this.area.coords.size()){

            } else {
                try {
                    relativeSum += this.resultVector.get(line,0) / cells.getNumberOfAdjacentFluidCells(area.getCoordinatesForListIndex(line));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        builder.append("\nSumme aller Ergebnisse: " + absoluteSum );
        builder.append("\nrelative Summe mit Berücksichtigung der Nachbarn: " +relativeSum );
        return builder.toString();

    }


    public void setPressure() {
        this.area.calcPressure(this.cells);
        this.pressure = this.area.getPressure();
    }

/*
    SystemOfEquations(Cell[][][] cells){
        this.dimension = cells.length;
        this.equations = new double[dimension*dimension*dimension][dimension*dimension*dimension];
        System.out.print("dimension: " + this.dimension);
        this.init(cells);

    }


    public void init(Cell[][][] cells){
        Coordinates coords = new Coordinates(0,0,0, dimension);
        int i =0;
        equations[i][Coordinates.getNumberInSystem(coords)] = -1;
        try {
            equations[i][Coordinates.getNumberInSystem(coords.getCoordsX(1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                    cells[coords.x+1][coords.y][coords.z].getLastFluidValue() + 1;
        } catch (Exception e) {
        }
        try {
            equations[i][Coordinates.getNumberInSystem(coords.getCoordsY(1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                    cells[coords.x][coords.y+1][coords.z].getLastFluidValue() + 1;
        } catch (Exception e) {
        }
        try {
            equations[i][Coordinates.getNumberInSystem(coords.getCoordsZ(1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                    cells[coords.x][coords.y][coords.z+1].getLastFluidValue() + 1;
        } catch (Exception e) {
        }

        while (coords.increase()){
            System.out.print("coords: " + coords.toString());
            i++;
            equations[i][Coordinates.getNumberInSystem(coords)] = -1;
            try {
                equations[i][Coordinates.getNumberInSystem(coords.getCoordsX(1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                        cells[coords.x+1][coords.y][coords.z].getLastFluidValue() + 1;
            } catch (Exception e) {
            }
            try {
                equations[i][Coordinates.getNumberInSystem(coords.getCoordsX(-1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                        cells[coords.x-1][coords.y][coords.z].getLastFluidValue() + 1;
            } catch (Exception e) {
            }
            try {
                equations[i][Coordinates.getNumberInSystem(coords.getCoordsY(1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                        cells[coords.x][coords.y+1][coords.z].getLastFluidValue() + 1;
            } catch (Exception e) {
            }
            try {
                equations[i][Coordinates.getNumberInSystem(coords.getCoordsY(-1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                        cells[coords.x][coords.y-1][coords.z].getLastFluidValue() + 1;
            } catch (Exception e) {
            }
            try {
                equations[i][Coordinates.getNumberInSystem(coords.getCoordsZ(1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                        cells[coords.x][coords.y][coords.z+1].getLastFluidValue() + 1;
            } catch (Exception e) {
            }
            try {
                equations[i][Coordinates.getNumberInSystem(coords.getCoordsZ(-1))] = cells[coords.x][coords.y][coords.z].getLastFluidValue()/
                        cells[coords.x][coords.y][coords.z-1].getLastFluidValue() + 1;
            } catch (Exception e) {
            }


        }
    }



    public void solve(Cell[][][] boundaryCondition){
        double[] boundaries = Coordinates.transformTo1DimensionLastValue(boundaryCondition);



    }

*/
}
