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

    double[][] gaussOperators;
    long[][] workingMatrix;
    CellArea area;
    Cells cells;
    int dimension;
    HeatequationLogger logger;
    private BigDecimal pressure;
    private double energySum;
    Coordinates logCoords;
    private double average;
    double pressureFailure;
    int counter5;
    int counter3;
    int counter2;
    boolean isInitialized;



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
        this.gaussOperators = new double[this.dimension][this.dimension];
        this.workingMatrix = new long[this.dimension][this.dimension];


        this.cells = cells;
        resetEquationsAndboundaries();
        this.logCoords = new Coordinates(2,4,2);
        //this.createGaussMatrix();
        this.isInitialized = false;
        int counter5=1;
        int counter3=1;
        int counter2=1;
    }

    private void createGaussMatrix() {
        //System.out.println("init: " + this.drawTable(this.workingMatrix));
        this.createTriangleMatrix();
        //System.out.println("dreieck: " + this.drawTable(this.workingMatrix));
        this.createDiagonalMatrix();
        //System.out.println("diagonal: " + this.drawTable(this.workingMatrix));
        //System.out.print("matrix: " + this.workingMatrix.toString());
        this.normalizeSystem();

        //System.out.println("Gauß matrix: " + this.drawTable(this.gaussOperators));

    }

    private void resetEquationsAndboundaries(){
        for (int i = 0; i < equationMatrix.getColumnDimension(); i++){
            for (int j = 0; j < equationMatrix.getColumnDimension(); j++){
                this.equationMatrix.set(i,j,0.0);
                this.gaussOperators[i][j] =0;
                this.workingMatrix[i][j] =0;
                if (i==j){
                    this.gaussOperators[i][j] =1.0;
                }
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
            this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "junction from " + rowJunction.getFrom().toString() + " to " + rowJunction.getTo().toString() +  " with index " + rowIndex);

            //from cell
            for (Junction outgoingJunction: this.area.getOutgoingJunctionsForCell(centerCoordinates)){
                if (outgoingJunction.equals(rowJunction)){
                    this.equationMatrix.set(rowIndex, rowIndex, -2.0);
                    this.workingMatrix[rowIndex][rowIndex] = -2;
                    this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "diagonal entry for matrix "+  rowIndex + " added");

                } else {
                    this.equationMatrix.set(rowIndex, area.getListIndexForJunction(outgoingJunction),-1.0);
                    //this.equationMatrix.set(area.getListIndexForJunction(outgoingJunction),rowIndex,1.0);
                    this.workingMatrix[rowIndex][area.getListIndexForJunction(outgoingJunction)] = -1;
                    //this.workingMatrix[area.getListIndexForJunction(outgoingJunction)][rowIndex] = 1;
                    this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "junction from " + outgoingJunction.getFrom().toString() + " to " + outgoingJunction.getTo().toString() +  " with index " + rowIndex + " and " + area.getListIndexForJunction(outgoingJunction) + " set to -1");
                }
            }

            //to cell
            for (Junction outgoingJunctionFromTarget: this.area.getOutgoingJunctionsForCell(rowJunction.getTo())){
                    this.equationMatrix.set(rowIndex, area.getListIndexForJunction(outgoingJunctionFromTarget), 1);
                //this.equationMatrix.set( area.getListIndexForJunction(outgoingJunctionFromTarget),rowIndex, -1);
                this.workingMatrix[rowIndex][area.getListIndexForJunction(outgoingJunctionFromTarget)] = 1;
                //this.workingMatrix[area.getListIndexForJunction(outgoingJunctionFromTarget)][rowIndex] = -1;
                this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "junction from " + outgoingJunctionFromTarget.getFrom().toString() + " to " + outgoingJunctionFromTarget.getTo().toString() +  " with index " + rowIndex + " and " + area.getListIndexForJunction(outgoingJunctionFromTarget) + " set to +1");
                }


            //boundary
            this.boundaryVector.set(rowIndex, 0, cells.getCell(rowJunction.getTo()).getAsFluidCell().getEnergy() - cells.getCell(rowJunction.getFrom()).getAsFluidCell().getEnergy());


            }




        }

        public static long getLcm(long num1, long num2){
        if (num1 == 0 || num2 == 0){
            return 0;
        }

         long bigNumber;
         long smallNumber;
        //get big and small absolute number
            /*
        if (num1 == num2){
            return num1;

        } else if (num1 < num2){
            bigNumber = Math.abs(num2);
            smallNumber = Math.abs(num1);
        } else {
            bigNumber = Math.abs(num1);
            smallNumber = Math.abs(num2);
        }
        */
        //calc
            long calc1;
            long calc2;
            if (Math.abs(num1) > Math.abs(num2)){
                calc1= Math.abs(num1);
                calc2 = Math.abs(num2);
            } else {
                calc2= Math.abs(num1);
                calc1 = Math.abs(num2);
            }

            //calc1 > calc 2
            //einser abfangen
            if (calc1==1){
               return calc2;
            } if (calc2 ==1){
               return calc1;
            }

            //check if they can be divided by each other
            if (calc1 % calc2 == 0){
                calc1= calc2;
            } else {

/*
                while (calc1 % 2 == 0 && calc2 % 2 == 0) {
                    calc1 /= 2;
                    calc2 /= 2;

                }
                */
            }



            System.out.println("getting lcm for number1: " + num1 + " and number 2: " + num2);
            int steps =0;
            while (calc1 != calc2) {

                if (calc1< calc2){
                    calc2 -= calc1;
                } else {
                    calc1 -= calc2;
                }
                System.out.println("number1: " + calc1 + " and number 2: " + calc2 + "after step " + steps);
                steps++;
                if (steps > 1000){

                    System.out.println("hat nicht so ganz funktioniert....  " + num1 + " und " + num2);
                    if (Math.abs(num1) > Math.abs(num2)){
                        calc1= Math.abs(num1);
                        calc2 = Math.abs(num2);
                    } else {
                        calc2= Math.abs(num1);
                        calc1 = Math.abs(num2);
                    }
                    if(calc1 % 2 == 0 && calc2 % 2 == 0) {
                        int ggt = 2;
                        while (calc1 % 2 == 0 && calc2 % 2 == 0) {

                            calc1 /= 2;
                            calc2 /= 2;
                            ggt *= 2;

                        }
                        calc1=ggt;
                        calc2 = ggt;
                    } else {
                        calc1 = 1;
                        calc2=1;
                    }
                }
            }


            long product = num1 * num2;
            System.out.println("lcm = : " + product + " / " + calc1 +" =  "+num1*num2/calc1 + " after " + steps + " steps");
            return Math.abs(num1*num2/calc1);
    }

    public void solveEquations(){
        //double[][] dreieck = this.dreiecksMatrix();
        //this.getResultsforDreieck(dreieck);

        if (!this.isInitialized){
            this.createGaussMatrix();
            this.isInitialized=true;
        }

        long start = System.currentTimeMillis();




        double[] results = this.calcResults();






        try {
            //this.resultVector = this.equationMatrix.solve(boundaryVector);

            double[][]resMatrix = new double[dimension][1];
            for (int i=0; i < dimension; i++){
                resMatrix[i][0]= results[i];



            }


            this.resultVector = new Matrix(resMatrix);




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

    private void normalizeSystem() {
        for (int i=0; i< dimension; i++){
            this.divideEachEntryOfRowFromGaussOperators(i,this.workingMatrix[i][i]);
        }
    }


    private double[] calcResults() {


        double[] result = new double[dimension];
        for (int row=0; row < dimension; row++){
            for (int col =0; col < dimension; col ++){
                result[row]+= gaussOperators[row][col] * this.boundaryVector.get(col,0);

                //matrix multiplikation
            }
        }
        return result;
    }

    private long[] getMatrixRow(long[][] matrix, int row, long factor){
        long[] result = matrix[row];
        for (int i =0; i<this.dimension; i++){
            result[i] = result[i]*factor;
            }
        return result;
    }

    private double[] getMatrixRow(double[][] matrix, int row, long factor){
        double[] result = matrix[row];
        for (int i =0; i<this.dimension; i++){
            result[i] = result[i]*factor;
        }
        return result;
    }

    private void createTriangleMatrix() {

        for (int diagonalIndex =0; diagonalIndex < this.dimension; diagonalIndex++){

            for (int rowIndex = diagonalIndex+1; rowIndex < this.dimension; rowIndex++) {
                if (workingMatrix[rowIndex][diagonalIndex] != 0 && workingMatrix[diagonalIndex][diagonalIndex] != 0) {
                    long lcm = this.getLcm(workingMatrix[rowIndex][diagonalIndex],workingMatrix[diagonalIndex][diagonalIndex]);
                    long diagonalFactor = lcm /workingMatrix[diagonalIndex][diagonalIndex];
                    long workingFactor = -lcm/workingMatrix[rowIndex][diagonalIndex];
                    long[] diagonalRow = this.getMatrixRow(workingMatrix, diagonalIndex, diagonalFactor);
                    long[] workingRow = this.getMatrixRow(workingMatrix, rowIndex, workingFactor);
                    //this.multiplyEachEntryOfRowFromGaussOperators(rowIndex, workingFactor);

                    this.gaussOperators[rowIndex] = this.getMatrixRow(this.gaussOperators, rowIndex,workingFactor);
                    this.gaussOperators[rowIndex] = this.addRows(this.gaussOperators[rowIndex],this.getMatrixRow(this.gaussOperators, diagonalIndex,diagonalFactor));
                    //this.gaussOperators[rowIndex][diagonalIndex] += diagonalFactor;

                    //this.gaussOperators[rowIndex][rowIndex] += workingFactor;

                    for (int columnIndex = 0; columnIndex < this.dimension; columnIndex++) {

                        //TODO: Check performance of calculation of 0 vs if statement
                        if (this.workingMatrix[rowIndex][columnIndex] != 0 || this.workingMatrix[diagonalIndex][columnIndex] != 0) {
                            workingRow[columnIndex] += diagonalRow[columnIndex];
                            System.out.println("triangle calculated for " + rowIndex + " and "+columnIndex + " for diagonal " +diagonalIndex );
                        }

                    }
                    this.workingMatrix[rowIndex] = this.simplifyRow(workingRow, rowIndex);
                }
                }
            this.workingMatrix[diagonalIndex] = this.simplifyRow(this.workingMatrix[diagonalIndex], diagonalIndex);

        }





    }

    private double[] addRows(double[] row1, double[] row2) {
        double[] result = new double[dimension];
        for (int i=0; i< dimension; i++){
            result[i] = row1[i] + row2[i];

        }
        return result;
    }

    /*
    private void multiplyEachEntryOfRowFromGaussOperators(int rowIndex, double workingFactor) {
        for (int col =0; col < this.dimension; col ++){
            if (this.gaussOperators[rowIndex][col] != 0) {


                this.gaussOperators[rowIndex][col] = this.gaussOperators[rowIndex][col] * workingFactor;
            }
        }
    }
    */

    private void divideEachEntryOfRowFromGaussOperators(int rowIndex, double workingFactor) {
        if (workingFactor ==0){
            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "working factor was not calculated correctly!!!");
            return;
        }
        for (int col =0; col < this.dimension; col ++){
            if (this.gaussOperators[rowIndex][col] != 0) {


                this.gaussOperators[rowIndex][col] = this.gaussOperators[rowIndex][col] / workingFactor;
            }
        }
    }

    private void createDiagonalMatrix() {

        for (int diagonalIndex =this.dimension-1; diagonalIndex >= 0; diagonalIndex--){

            for (int rowIndex = diagonalIndex-1; rowIndex  >= 0; rowIndex--) {
                if (workingMatrix[rowIndex][diagonalIndex] != 0) {
                    long lcm = this.getLcm(workingMatrix[rowIndex][diagonalIndex],workingMatrix[diagonalIndex][diagonalIndex]);
                    long diagonalFactor = lcm /workingMatrix[diagonalIndex][diagonalIndex];
                    long workingFactor = -lcm/workingMatrix[rowIndex][diagonalIndex];
                    long[] diagonalRow = this.getMatrixRow(workingMatrix, diagonalIndex, diagonalFactor);
                    long[] workingRow = this.getMatrixRow(workingMatrix, rowIndex, workingFactor);
                    this.gaussOperators[rowIndex] = this.getMatrixRow(this.gaussOperators, rowIndex,workingFactor);
                    this.gaussOperators[rowIndex] = this.addRows(this.gaussOperators[rowIndex],this.getMatrixRow(this.gaussOperators, diagonalIndex,diagonalFactor));
                    //this.gaussOperators[rowIndex][diagonalIndex] += diagonalFactor;
                    //this.gaussOperators[rowIndex][rowIndex] += workingFactor;
                    for (int columnIndex = this.dimension-1; columnIndex  >= diagonalIndex; columnIndex--) {

                        //TODO: Check performance of calculation of 0 vs if statement
                        if (this.workingMatrix[rowIndex][columnIndex] != 0) {
                            workingRow[columnIndex] += diagonalRow[columnIndex];
                            System.out.println("calculated diagonal for " + rowIndex + " and "+columnIndex + " for diagonal " + diagonalIndex);
                        }

                    }
                    this.workingMatrix[rowIndex] = this.simplifyRow(workingRow, rowIndex);
                }
            }
            this.workingMatrix[diagonalIndex] = this.simplifyRow(this.workingMatrix[diagonalIndex], diagonalIndex);

        }





    }

    private long[] simplifyRow(long[] workingRow, int rowIndex) {
        long[] temp = new long[workingRow.length];
        //divide by smallest member
        /*
        long smallestEntry =5000;
        for (int i=0; i< workingRow.length; i++){
            if (Math.abs(workingRow[i])<smallestEntry){
                smallestEntry =Math.abs(workingRow[i]);
            }
        }

        boolean divideable;
        for (int i=0; i< workingRow.length; i++){
            if (Math.abs(workingRow[i])%smallestEntry ==0){
                workingRow[i] =workingRow[i];
            }
        }
        */
        int multiplyer = 1;
        while (this.rowDividibleBy(workingRow, 5)){
            for (int i=0; i< workingRow.length; i++){
                if (workingRow[i] != 0){
                    workingRow[i]/=5;
                }
            }
            this.counter5++;
            multiplyer*=5;
        }
        while (this.rowDividibleBy(workingRow, 3)){
            for (int i=0; i< workingRow.length; i++){
                if (workingRow[i] != 0){
                    workingRow[i]/=3;
                }
            }
            this.counter3++;
            multiplyer*=3;
        }
        while (this.rowDividibleBy(workingRow, 2)){
            for (int i=0; i< workingRow.length; i++){
                if (workingRow[i] != 0){
                    workingRow[i]/=2;
                }
            }
            this.counter2++;
            multiplyer*=2;
        }
        this.divideEachEntryOfRowFromGaussOperators(rowIndex, multiplyer);
        return workingRow;

    }

    private boolean rowDividibleBy(long[] workingRow, int divisor) {
        for (int i=0; i< workingRow.length; i++){
            if (workingRow[i] != 0 && Math.abs(workingRow[i])%divisor !=0){
                return false;
            }
        }
        return true;
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

    private String drawTable(long[][] matrix) {
        StringBuilder builder = new StringBuilder(this.dimension + " x " + this.dimension + " matrix: \n");

        for (int row = 0; row < this.dimension; row++) {
            for (int col = 0; col < this.dimension; col++) {
                builder.append(matrix[row][col] + " ");

            }
            builder.append("\n");

        }
        return builder.toString();
    }

    private String drawTable(double[][] matrix) {
        StringBuilder builder = new StringBuilder(this.dimension + " x " + this.dimension + " matrix: \n");

        for (int row = 0; row < this.dimension; row++) {
            for (int col = 0; col < this.dimension; col++) {
                builder.append(matrix[row][col] + " ");

            }
            builder.append("\n");

        }
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
