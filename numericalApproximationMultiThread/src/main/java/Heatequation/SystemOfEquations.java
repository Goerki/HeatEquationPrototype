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
    private BigDecimal pressure;
    private double energySum;
    Coordinates logCoords;
    private double average;
    double pressureFailure;



    SystemOfEquations(CellArea area, Cells cells, HeatequationLogger logger){
        this.logger = logger;
        this.area = area;
        this.dimension = area.coords.size();
        this.pressureFailure =0;


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
        this.energySum=0;
        for (Coordinates otherCell : this.area.coords) {

            this.energySum+=this.cells.getCell(otherCell).getAsFluidCell().getLastNumberParticles() * this.cells.getCell(otherCell).getAsFluidCell().getLastValue();
        }
        this.average = this.energySum/(double) this.area.coords.size();
    }



    public void addToEquations(Coordinates centerCoordinates, CellArea area){


        int centerIndex = area.getListIndexForCell(centerCoordinates);
        Cell centerCell = cells.getCell(centerCoordinates);


        if (centerCoordinates.equals(this.logCoords)){
            if (this.logger.logLevelEnabled(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS)) {
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "gotcha");
            }
        }






        this.area.setFactorsForCell(centerCoordinates, this.average, cells);





        //ausgehende Teilchen

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
                 //equations[neighborIndex][centerIndex] = factorDeci.doubleValue() * this.cells.getCell(centerCoordinates).getLastValue();
             try {
                 //this.equationMatrix.set(centerIndex, neighborIndex,this.area.getFactorFor(centerCoordinates, otherCell) * this.cells.getCell(otherCell).getLastValue());

                 this.equationMatrix.set(neighborIndex, centerIndex,this.area.getFactorFor(centerCoordinates, otherCell) * centerCell.getLastValue());

             } catch (Exception e) {
                 e.printStackTrace();
                 this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, e.toString());
             }
             //this.equationMatrix.set(centerIndex,neighborIndex,this.area.getFactorFor(centerCoordinates,otherCell) * this.cells.getCell(otherCell).getLastValue());

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
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "result " + (Cells.cellSize * Cells.cellSize * Cells.cellSize / Cells.gasConstant * this.pressure.doubleValue() - centerCell.getLastValue() * centerCell.getAsFluidCell().getLastNumberParticles()));
            }

        }

        //boundaries[centerIndex] = Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant -centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles();

        BigDecimal idealerWErt= this.pressure.multiply(new BigDecimal(Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant));
        double istWert = centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles();
        boundaryVector.set(centerIndex,0,  (idealerWErt.doubleValue() -centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles()));


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
                this.setResultsToZero();
            }
        }

    }

    private void setResultsToZero() {
        for (int i = 0; i < equationMatrix.getColumnDimension(); i++) {
            resultVector.set(i, 0, 0.0);
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
    public double getSumOfAllResults(){
        double sum = 0;
        for (int i=0; i< this.resultVector.getColumnDimension(); i++){
            sum += this.getResultForListIndex(i);

        }
        return sum;
    }


    public void setPressure() {
        this.area.calcPressure(this.cells);
        this.pressure = this.area.getBigDeciPressure();
    }

    public void applyPressure() {
        for (Coordinates eachCell: this.area.coords){
            this.cells.getCell(eachCell).getAsFluidCell().applyPressure(pressure.doubleValue(), this.cells.gasConstant, 1);

        }
    }

    public void verifyPressureForEachCell() {
        for (Coordinates eachCell: this.area.coords){
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO, eachCell.toString() + ": " +this.cells.getCell(eachCell).getAsFluidCell().verifyPressure(pressure.doubleValue(), this.cells.gasConstant, 1));

        }
    }

    public void calcPressureCalculationFailure() {
        this.pressureFailure += this.area.getPressureCalculationFailure(this.cells);

        double difference = this.pressure.doubleValue() +(pressureFailure/(double)this.dimension);


        if (difference!= this.pressure.doubleValue()){
            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "difference is big enough: " + pressureFailure/(double)this.dimension + "adds to "+ (this.pressure.doubleValue() +(pressureFailure/(double)this.dimension))+" pressure changed from " + this.pressure.doubleValue() + " to " + (this.pressure.doubleValue() + (pressureFailure/(double)this.dimension)));
            String xString = String.valueOf(this.pressureFailure);
            StringBuilder builder;
            if (xString.charAt(0)== '-'){
                builder = new StringBuilder("-0");
                builder.append(xString.substring(2));
            } else {
                builder = new StringBuilder("0");
                builder.append(xString.substring(1));
            }
            double newpressureFailure = Double.parseDouble(builder.toString());

            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "failure from: "+this.pressureFailure + " to " + newpressureFailure) ;
            this.pressureFailure = newpressureFailure;
        } else {
            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "difference is 0. Combined Failure: " + (-1*this.pressureFailure));
        }
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
