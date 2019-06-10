package Heatequation.Cells;

public class VisualizationCells extends Cells {
    private double elapsedTime;




    public VisualizationCells(Cells oldCells, double elapsedTime){
        this.elapsedTime = elapsedTime;
        this.logger = oldCells.logger;
        this.sizeX = oldCells.sizeX;
        this.sizeY = oldCells.sizeY;
        this.sizeZ = oldCells.sizeZ;
        this.cells = new Cell[sizeX][sizeY][sizeZ];
        for(int x =0; x< sizeX;x++){
            for(int y =0; y< sizeY;y++){
                for(int z =0;z< sizeZ;z++){
                    if (oldCells.cells[x][y][z].isFluid){
                    this.cells[x][y][z] = new FluidCell(oldCells.cells[x][y][z].value, oldCells.cells[x][y][z].material, oldCells.cells[x][y][z].getAsFluidCell().getPressure());
                    } else {
                        this.cells[x][y][z] = new SolidCell(oldCells.cells[x][y][z].material, oldCells.cells[x][y][z].value);
                    }
            }
        }

        }
        super.calcAverages();

    }


    public double getElapsedTime() {
        return elapsedTime;
    }
}
