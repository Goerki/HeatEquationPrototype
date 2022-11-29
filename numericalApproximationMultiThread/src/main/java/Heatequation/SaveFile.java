package Heatequation;

import Heatequation.Cells.Cell;
import java.io.Serializable;

public class SaveFile implements Serializable {

    public Cell[][][] cells;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    public int numberCellsForSolidCalculation;
    public int numberThreads;
    public double deltaT;
    public double cellLength;
    public int numberSteps;
    public int numberCalculatedSteps;

    public SaveFile( Cell[][][] cells,int sizeX, int sizeY, int sizeZ,int numberCellsForSolidCalculation,
                             int numberThreads,
                             double deltaT,
                             double cellLength,
                             int numberSteps,
                             int numberCalculatedSteps

    ) {
        this.cells=cells;
        this.sizeX=sizeX;
        this.sizeY=sizeY;
        this.sizeZ=sizeZ;
        this.numberCellsForSolidCalculation=numberCellsForSolidCalculation;
        this.numberThreads=numberThreads;
        this.deltaT=deltaT;
        this.cellLength=cellLength;
        this.numberSteps=numberSteps;
        this.numberCalculatedSteps=numberCalculatedSteps;

    }
}
