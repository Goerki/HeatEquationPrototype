package Heatequation;

import Heatequation.Cells.Cell;
import Heatequation.Cells.CellArea;
import Heatequation.Cells.Cells;
import Heatequation.Cells.Coordinates;
import Jama.Matrix;

import java.io.Serializable;
import java.util.List;

public class SystemOfEquations implements Serializable {
    double[][] equations;
    double[] boundaries;
    double[][] result;
    CellArea area;
    Cells cells;
    int dimension;
    HeatequationLogger logger;
    private double pressure;


    SystemOfEquations(CellArea area, Cells cells, HeatequationLogger logger){
        this.logger = logger;
        this.area = area;
        this.dimension = area.coords.size()+area.getNumberVirtualCells();
        equations = new double[this.dimension][this.dimension];
        result = new double[this.dimension][this.dimension];
        boundaries = new double[this.dimension];
        this.cells = cells;
        resetEquationsAndboundaries();
    }

    private void resetEquationsAndboundaries(){
        for (int i = 0; i < boundaries.length; i++){
            for (int j = 0; j < boundaries.length; j++){
                equations[i][j] =0;
                result[i][j] =0;
            }
            boundaries[i]=0;
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

    public void addToEquations(Coordinates centerCoordinates, CellArea area){
        int centerIndex = area.getListIndexForCell(centerCoordinates);
        List<Coordinates> adjacentCells = cells.getAllAdjacentFluidCells(centerCoordinates);
        Cell centerCell = cells.getCell(centerCoordinates);
        Coordinates logCoords = new Coordinates(2,2,2);

        if (centerCoordinates.equals(logCoords)){
            logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "gotcha");

        }

        //ausgehende Teilchen
        equations[centerIndex][centerIndex] = -centerCell.getLastValue();
        if (centerCoordinates.equals(logCoords)){
            logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "value: " + centerCell.getLastValue());

        }
        for(Coordinates neighborCell :adjacentCells) {
            //einkommende Teilchen
            int neighborIndex = area.getListIndexForCell(neighborCell);

            if (centerCoordinates.equals(logCoords)){
                logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "neighborCell " + neighborCell.toString() + " :" + this.cells.getCell(neighborCell).getLastValue() / cells.getNumberOfAdjacentFluidCells(neighborCell) );

            }

            //equations[centerIndex][listIndex] = this.cells.getCell(neighborCell).getLastValue();

            //andere Reihenfolge der indizes
            //
            equations[centerIndex][neighborIndex] = this.cells.getCell(neighborCell).getLastValue() / cells.getNumberOfAdjacentFluidCells(neighborCell);
            //equations[listIndex][centerIndex] = this.cells.getCell(neighborCell).getLastValue() / cells.getNumberOfAdjacentFluidCells(neighborCell);

        }


            //virtual bordercells einkommend
            int virtualBorderCellIndex = -1;
            if (centerCell.getAsFluidCell().isBorderCell()) {
                virtualBorderCellIndex = area.getListIndexForVirtualCell(centerCoordinates);
                equations[centerIndex][virtualBorderCellIndex] = centerCell.getAsFluidCell().getTemperatureOfBorderCell() * centerCell.getAsFluidCell().getNumberOfVirtualBorders();

                //virtual cell ausgehend ... N1T1 = N2T2
                equations[virtualBorderCellIndex][virtualBorderCellIndex] = -centerCell.getAsFluidCell().getTemperatureOfBorderCell();
                //equations[virtualBorderCellIndex][centerIndex]=centerCell.getAsFluidCell().getLastValue()*centerCell.getAsFluidCell().getNumberOfVirtualBorders()/6;

                boundaries[virtualBorderCellIndex] = 0;
                //equations[virtualBorderCellIndex][listIndex] = this.cells.getCell(neighborCell).getLastValue()/cells.getNumberOfAdjacentFluidCells(neighborCell);

            }

            /*
            if(centerCell.getAsFluidCell().isBorderCell()){
                equations[virtualBorderCellIndex][virtualBorderCellIndex] = centerCell.getAsFluidCell().getTemperatureOfBorderCell();
                equations[virtualBorderCellIndex][centerIndex] = -centerCell.getLastValue();
              //  boundaries[virtualBorderCellIndex] = - centerCell.getAsFluidCell().getLastNumberParticles() + Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant;
                boundaries[virtualBorderCellIndex] = 3333;

            }
            */


            /*
            if(centerCell.getAsFluidCell().isBorderCell()){
                int virtualBorderCellIndex = area.getListIndexForVirtualCell(centerCoordinates);
                equations[centerIndex][virtualBorderCellIndex] = centerCell.getAsFluidCell().getTemperatureOfBorderCell();
                equations[virtualBorderCellIndex][virtualBorderCellIndex] = 1;
                boundaries[virtualBorderCellIndex] = - centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles() + Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant;
            }
                   */



        //boundary condition
 //       equations[area.coords.size()][centerIndex] = 1/cells.getNumberOfAdjacentFluidCells(centerCoordinates);

        //boundaries
        if (centerCoordinates.equals(logCoords)){
            logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "result "  +( Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant*this.pressure -centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles()));

        }

        boundaries[centerIndex] = Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant*this.pressure -centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles();

    }

    public void solveEquations(){
        result = new Matrix(equations).solve(new Matrix(boundaries, dimension)).getArray();
    }

    public double getResultForListIndex(int index){
        return result[index][0];
    }

    public double getResultForCoordinates(Coordinates coord) throws Exception {
        int index = this.area.getListIndexForCell(coord);
        return result[index][0];
    }

    public double getResultForVirtualCell(Coordinates centerCell) {
        return result[area.getListIndexForVirtualCell(centerCell)][0];
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
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, this.toString());
    }

    public String toString(){
        StringBuilder builder = new StringBuilder("SYSTEM OF EQUATIONS: \n");
        int virtualCellCounter = 1;
        double absoluteSum =0;
        double relativeSum =0;
        for (int line =0 ; line < this.boundaries.length; line++){
            if (line > this.area.coords.size()){
                builder.append("virtual " + virtualCellCounter + " : ");
                virtualCellCounter++;
            } else {
                try {
                    builder.append(line+  " : "+ this.area.getCoordinatesForListIndex(line).toString() + " with " + cells.getNumberOfAdjacentFluidCells(area.getCoordinatesForListIndex(line)) + " neighborCells : ");
                } catch (Exception e) {
                    e.printStackTrace();
                    builder.append(e.toString());
                }
            }
            try {
                builder.append("T0 = " + this.cells.getCell(area.getCoordinatesForListIndex(line)).getLastValue() + " N0 = " + this.cells.getCell(area.getCoordinatesForListIndex(line)).getAsFluidCell().getLastNumberParticles());
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int column =0 ; column < this.boundaries.length; column++){
                builder.append(" " + this.equations[line][column]+ " ");
            }
            builder.append(" = " + this.boundaries[line] + " ");
            builder.append(" Ergebnis: " + this.result[line][0]+ " \n");
            absoluteSum += this.result[line][0];
            try {
                relativeSum += this.result[line][0]/cells.getNumberOfAdjacentFluidCells(area.getCoordinatesForListIndex(line));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        builder.append("\nSumme aller Ergebnisse: " + absoluteSum );
        builder.append("\nrelative Summe mit Berücksichtigung der Nachbarn: " +relativeSum );
        return builder.toString();

    }

    public void limitEquations() {
        for(int i = 0; i < this.dimension; i++){
            this.result[i][0] *= 0.005;

        }
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
