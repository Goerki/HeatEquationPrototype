package Heatequation;

import Heatequation.Cells.*;
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

    int[][] GaußOperators;
    double[][] workingMatrix;
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
        this.dimension = area.getNumberJunctions();
        this.pressureFailure =0;


        this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "dimension: " + dimension);
        //double[][] matrix = new double[this.dimension][this.dimension];
        this.equationMatrix = new Matrix(this.dimension, this.dimension);
        this.boundaryVector = new Matrix( this.dimension, 1);
        this.resultVector =  new Matrix( this.dimension,1);


        this.cells = cells;
        resetEquationsAndboundaries();
        this.logCoords = new Coordinates(2,4,2);
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



    public void setEnergySum() {
        this.energySum=0;
        for (Coordinates otherCell : this.area.coords) {

            this.energySum+=this.cells.getCell(otherCell).getAsFluidCell().getLastNumberParticles() * this.cells.getCell(otherCell).getAsFluidCell().getLastValue();
        }
        this.average = this.energySum/(double) this.area.coords.size();
    }



    public void addToEquations(Coordinates centerCoordinates){
        for (Junction rowJunction: this.area.getOutgoingJunctionsForCell(centerCoordinates)){
            int rowIndex = area.getListIndexForJunction(rowJunction);

            //from cell
            for (Junction outgoingJunction: this.area.getOutgoingJunctionsForCell(rowJunction.getFrom())){
                if (outgoingJunction.equals(rowJunction)){
                    this.equationMatrix.set(rowIndex, rowIndex, -2.0);

                } else {
                    this.equationMatrix.set(rowIndex, area.getListIndexForJunction(outgoingJunction),-1.0);
                }
            }

            //to cell
            for (Junction outgoingJunctionFromTarget: this.area.getOutgoingJunctionsForCell(rowJunction.getTo())){
                    this.equationMatrix.set(rowIndex, area.getListIndexForJunction(outgoingJunctionFromTarget), 1);
                }


            //boundary
            this.boundaryVector.set(rowIndex, 0, cells.getCell(rowJunction.getTo()).getAsFluidCell().getEnergy() - cells.getCell(rowJunction.getFrom()).getAsFluidCell().getEnergy());


            }




        }

    public void solveEquations(){
        //double[][] dreieck = this.dreiecksMatrix();
        //this.getResultsforDreieck(dreieck);


        long start = System.currentTimeMillis();





        try {
            this.resultVector = this.equationMatrix.solve(boundaryVector);



            long duration = System.currentTimeMillis() -  start;
            this.logger.logMessage(HeatequationLogger.LogLevel.INFO,"solving of the System with " + this.dimension + " lines took " + duration + "milliseconds");



            System.out.print(resultVector.toString());
        } catch (Exception e) {
            System.out.print("\n\nMAtRIX HAT KEINEN VOLLEN RANG!!\n");
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


    public double getResultForListIndex(int index){
        return resultVector.get(index,0);
    }

    public double getResultForJunction(Junction junc) throws Exception {

        int index = this.area.getListIndexForJunction(junc);
        return resultVector.get(index,0);
    }

    public double getAbsoluteResultForJunction(Junction junc) throws Exception {

        int index = this.area.getListIndexForJunction(junc);
        return Math.abs(resultVector.get(index,0));
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
                try {
                    builder.append(line+  " : "+ this.area.getCoordinatesForListIndex(line).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    builder.append(e.toString());
                }

            try {
                builder.append("T0 = " + this.cells.getCell(area.getCoordinatesForListIndex(line).getFrom()).getLastValue() + " N0 = " + this.cells.getCell(area.getCoordinatesForListIndex(line).getFrom()).getAsFluidCell().getLastNumberParticles());
            } catch (Exception e) {
                e.printStackTrace();
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
                    relativeSum += this.resultVector.get(line,0) / cells.getNumberOfAdjacentFluidCells(area.getCoordinatesForListIndex(line).getFrom());
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

    public boolean isIsobar() {
        return this.area.isIsobar();
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
