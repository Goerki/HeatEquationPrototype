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


    double[] boundaryVector;
    double[] resultVector;
    double[][] gaussOperators;
    long[][] workingMatrix;
    CellArea area;
    Cells cells;
    int dimension;
    HeatequationLogger logger;
    double pressureFailure;
    private boolean isInitialized;



    SystemOfEquations(CellArea area, Cells cells, HeatequationLogger logger) throws Exception{
        this.logger = logger;
        this.area = area;
        this.dimension = area.getNumberJunctions();
        this.pressureFailure =0;



        double size = dimension*dimension*8.0/(1024.0*1024.0);
        this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "dimension: " + dimension + " each matrix will take about " + size + " MB");
        this.boundaryVector = new double[dimension];
        this.resultVector =  new double[dimension];
        this.gaussOperators = new double[this.dimension][this.dimension];
        this.workingMatrix = new long[this.dimension][this.dimension];
        this.cells = cells;
        resetEquationsAndboundaries();
        //this.logCoords = new Coordinates(2,4,2);
        this.createGaussMatrix();
    }

    private void createGaussMatrix() throws Exception{

        //fill working matrix
        for (Coordinates eachCoord: this.area.coords){
            this.addToEquations(eachCoord);

        }
        System.out.println("init: " + this.drawTable(this.workingMatrix));
        this.printVektorSumme();
        //System.out.println(this.drawTable(workingMatrix));
        this.createTriangleMatrix();
        System.out.println("dreieck: " + this.drawTable(this.workingMatrix));

        this.createDiagonalMatrix();
        System.out.println("diagonal: " + this.drawTable(this.workingMatrix));
        //System.out.print("matrix: " + this.workingMatrix.toString());

        this.normalizeSystem();
        System.out.println("normal: " + this.drawTable(this.workingMatrix));
        System.out.println("gauss: " + this.drawTable(this.gaussOperators));
        System.out.println("\nsummen:");
        this.printVektorSumme();
        System.out.println("\n\n\nsummen Gausss:");
        this.printVektorSummeGauss();
        this.isInitialized=true;

        System.out.println("Gauß matrix: " + this.drawTable(this.gaussOperators));

    }

    private void printVektorSummeGauss() throws Exception {
        for (int col=0; col < dimension; col ++){
            double sum =0;
            double absoluteSum=0;

            for (int row=0; row < dimension; row ++){
                absoluteSum += Math.abs(this.gaussOperators[row][col]);
                sum += this.gaussOperators[row][col];
            }
            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR ,"Summe für " + this.area.getJunctionsForListIndex(col) + " : " +sum + " absolute: " + absoluteSum);

        }
    }

    private void printVektorSumme() throws Exception {
        for (int col=0; col < dimension; col ++){
            double sum =0;
            double absoluteSum=0;

            for (int row=0; row < dimension; row ++){
                absoluteSum += Math.abs(this.workingMatrix[row][col]);
                sum += this.workingMatrix[row][col];
            }
            this.logger.logMessage(HeatequationLogger.LogLevel.ERROR ,"Summe für " + this.area.getJunctionsForListIndex(col) + " : " +sum + " absolute: " + absoluteSum);

        }
    }



    private void resetEquationsAndboundaries(){
        for (int i = 0; i < dimension; i++){
            for (int j = 0; j <dimension; j++){
                this.gaussOperators[i][j] =0;
                this.workingMatrix[i][j] =0;
                if (i==j){
                    this.gaussOperators[i][j] =1.0;
                }
            }
            boundaryVector[i] =0;
            resultVector[i]=0;
        }
    }


    public void addToBoundaries(Coordinates centerCoordinates) {
        try {
            for (Junction rowJunction : this.area.getOutgoingJunctionsForCell(centerCoordinates)) {
                int rowIndex = area.getListIndexForJunction(rowJunction);
                if (rowJunction.getTo() == rowJunction.getFrom()){
                    this.boundaryVector[rowIndex] = cells.getCell(rowJunction.getTo()).getAsFluidCell().getEnergy() - cells.getCell(rowJunction.getFrom()).getAsFluidCell().getVirtualBorderCell().getEnergy();

                } else {

                    //boundary
                    this.boundaryVector[rowIndex] = cells.getCell(rowJunction.getTo()).getAsFluidCell().getEnergy() - cells.getCell(rowJunction.getFrom()).getAsFluidCell().getEnergy();

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }










    private void addToEquations(Coordinates centerCoordinates) {
        List<Junction> outgoingJunctions;
            try {
                outgoingJunctions = this.area.getOutgoingJunctionsForCell(centerCoordinates);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            for (Junction rowJunction :outgoingJunctions) {
                int rowIndex = area.getListIndexForJunction(rowJunction);
                if (rowIndex == this.dimension - 1) {
                    System.out.println("hm?");

                }
                //this.logger.logMessage(HeatequationLogger.LogLevel.SYTEMOFEQUATIONS, "junction from " + rowJunction.getFrom().toString() + " to " + rowJunction.getTo().toString() +  " with index " + rowIndex);

                if (rowJunction.getTo() == rowJunction.getFrom()) {
                    this.workingMatrix[rowIndex][rowIndex] --;
                    try {
                        for (Junction outgoingJunctionFromTarget : this.area.getOutgoingJunctionsForCell(rowJunction.getTo())) {
                            this.workingMatrix[rowIndex][area.getListIndexForJunction(outgoingJunctionFromTarget)]--;
                        }
                    } catch (Exception e) {
                    }

                    /*
                    try {
                        for (Junction incomingJunctionFromTarget : this.area.getIncomingJunctionsForCell(rowJunction.getTo())) {
                            this.workingMatrix[rowIndex][area.getListIndexForJunction(incomingJunctionFromTarget)]++;
                        }
                    } catch (Exception e) {
                    }
                    */

                } else {
                    //from cell
                    try {
                        for (Junction outgoingJunction : this.area.getOutgoingJunctionsForCell(rowJunction.getFrom())) {
                            int outgoingIndex = area.getListIndexForJunction(outgoingJunction);
                            this.workingMatrix[rowIndex][outgoingIndex]--;
                            //if (outgoingIndex==rowIndex && this.area.getOutgoingJunctionsForCell(rowJunction.getTo()).size()!=0){
                            if (outgoingIndex==rowIndex){
                                this.workingMatrix[rowIndex][outgoingIndex]--;
                            }
                        }
                    } catch (Exception e) {

                    }
                    /*
                    try {
                        for (Junction incomingJunction : this.area.getIncomingJunctionsForCell(rowJunction.getFrom())) {
                            int incomingIndex = area.getListIndexForJunction(incomingJunction);
                            this.workingMatrix[rowIndex][incomingIndex]++;
                        }
                    } catch (Exception e) {

                    }

*/

                    //to cell
                    try {
                        for (Junction outgoingJunctionFromTarget : this.area.getOutgoingJunctionsForCell(rowJunction.getTo())) {
                            int incomingIndex = area.getListIndexForJunction(outgoingJunctionFromTarget);
                            this.workingMatrix[rowIndex][incomingIndex]++;
                        }
                    } catch (Exception e) {

                    }
                    /*
                    try {
                        for (Junction incomingJunctionFromTarget : this.area.getIncomingJunctionsForCell(rowJunction.getTo())) {
                            int incomingIndex = area.getListIndexForJunction(incomingJunctionFromTarget);
                            this.workingMatrix[rowIndex][incomingIndex]--;
                        }
                    } catch (Exception e) {

                    }
                    */

                }

            }

    }


        public static long getLcm(long num1, long num2){
        if (num1 == 0 || num2 == 0){
            return 0;
        }

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


            }

            int steps =0;
            while (calc1 != calc2) {

                if (calc1< calc2){
                    calc2 -= calc1;
                } else {
                    calc1 -= calc2;
                }

                steps++;
                if (steps > 1000){

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
            return Math.abs(num1*num2/calc1);
    }



    private void normalizeSystem() {
        for (int i=0; i< dimension; i++){
            this.divideEachEntryOfRowFromGaussOperators(i,this.workingMatrix[i][i]);
            this.workingMatrix[i][i] =1;
        }
    }


    private double[] calcResults() {


        double[] result = new double[dimension];
        for (int row=0; row < dimension; row++){
            for (int col =0; col < dimension; col ++){
                result[row]+= gaussOperators[row][col] * this.boundaryVector[col];

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



                    this.gaussOperators[rowIndex] = this.getMatrixRow(this.gaussOperators, rowIndex,workingFactor);
                    this.gaussOperators[rowIndex] = this.addRows(this.gaussOperators[rowIndex],this.getMatrixRow(this.gaussOperators, diagonalIndex,diagonalFactor));
                    //this.gaussOperators[rowIndex][diagonalIndex] += diagonalFactor;

                    //this.gaussOperators[rowIndex][rowIndex] += workingFactor;

                    for (int columnIndex = 0; columnIndex < this.dimension; columnIndex++) {

                        //TODO: Check performance of calculation of 0 vs if statement
                        if (this.workingMatrix[rowIndex][columnIndex] != 0 || this.workingMatrix[diagonalIndex][columnIndex] != 0) {
                            workingRow[columnIndex] += diagonalRow[columnIndex];
                            //System.out.println("triangle calculated for " + rowIndex + " and "+columnIndex + " for diagonal " +diagonalIndex );
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
                            //System.out.println("calculated diagonal for " + rowIndex + " and "+columnIndex + " for diagonal " + diagonalIndex);
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
            multiplyer*=5;
        }
        while (this.rowDividibleBy(workingRow, 3)){
            for (int i=0; i< workingRow.length; i++){
                if (workingRow[i] != 0){
                    workingRow[i]/=3;
                }
            }
            multiplyer*=3;
        }
        while (this.rowDividibleBy(workingRow, 2)){
            for (int i=0; i< workingRow.length; i++){
                if (workingRow[i] != 0){
                    workingRow[i]/=2;
                }
            }
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




    public double getResultForListIndex(int index){
        return resultVector[index];
    }

    public double getResultForJunction(Junction junc) throws Exception {

        int index = this.area.getListIndexForJunction(junc);
        return resultVector[index];
    }

    public double getAbsoluteResultForJunction(Junction junc) throws Exception {

        int index = this.area.getListIndexForJunction(junc);
        return Math.abs(resultVector[index]);
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
        builder.append(this.drawTable(this.gaussOperators));
        /*
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
        */


        builder.append("\nSumme aller Ergebnisse: " + absoluteSum );
        builder.append("\nrelative Summe mit Berücksichtigung der Nachbarn: " +relativeSum );
        return builder.toString();

    }

    private String drawTable(long[][] matrix) {
        StringBuilder builder = new StringBuilder("\n" +this.dimension + " x " + this.dimension + " matrix: \n");

        for (int row = 0; row < this.dimension; row++) {
            for (int col = 0; col < this.dimension; col++) {
                if (Math.signum(matrix[row][col]) !=-1){
                    builder.append(" ");
                }
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
        for (int i=0; i<dimension; i++){
            sum += this.getResultForListIndex(i);

        }
        return sum;
    }


    public boolean isIsobar() {
        return this.area.isIsobar();
    }


    public void calcResultFor(Coordinates eachCoord) {
        try {
            for (Junction eachJunction: this.area.getOutgoingJunctionsForCell(eachCoord)){

                int row = this.area.getListIndexForJunction(eachJunction);
                    for (int col =0; col < dimension; col ++){
                        this.resultVector[row]+= gaussOperators[row][col] * this.boundaryVector[col];

                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetResultVector() {
        for (int i=0; i< dimension; i++){
            this.resultVector[i] =0;
        }
    }
}

