import Cells.Cell;
import Cells.CellArea;
import Cells.Cells;
import Cells.Coordinates;
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


    SystemOfEquations(CellArea area, Cells cells){
        this.area = area;
        this.dimension = area.coords.size();
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
            equations[area.coords.size()+1][centerIndex] = 1/cells.getAllAdjacentFluidCells(centerCell).size();
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

        //ausgehende Teilchen
        equations[centerIndex][centerIndex] = -centerCell.getLastValue();
        for(Coordinates neighborCell :adjacentCells){
            //einkommende Teilchen
            int listIndex = area.getListIndexForCell(neighborCell);
            if (listIndex <0 || listIndex >= area.coords.size()){
                System.out.print("FUCK YOU!!!");
            }
            //equations[centerIndex][listIndex] = this.cells.getCell(neighborCell).getLastValue();
            equations[centerIndex][listIndex] = this.cells.getCell(neighborCell).getLastValue() / cells.getAllAdjacentFluidCells(neighborCell).size();
        }

        //boundary condition
 //       equations[area.coords.size()][centerIndex] = 1/cells.getAllAdjacentFluidCells(centerCoordinates).size();

        //boundaries
        //TODO: DRUCK
        boundaries[centerIndex] = - centerCell.getLastValue()*centerCell.getAsFluidCell().getLastNumberParticles() + Cells.cellSize*Cells.cellSize*Cells.cellSize/Cells.gasConstant;

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
