package Cells;


import java.util.ArrayList;
import java.util.List;

public class Cells {
    Cell[][][] cells;
    int sizeX;
    int sizeY;
    int sizeZ;
    List<Coordinates> coords;
    List<Coordinates> cellsForSolidCalculation;

    public Cells(int size, double value, double viskosity){
        this.sizeX = size;
        this.sizeY = size;
        this.sizeZ = size;
        cells = new Cell[size][size][size];
        initCoords();
        initAllCells(value, viskosity);
        cellsForSolidCalculation = new ArrayList<>();
    }

    public Cells(int sizeX, int sizeY, int sizeZ, double value, double viskosity){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        cells = new Cell[sizeX][sizeY][sizeZ];
        this.initCoords();
        this.initAllCells(value, viskosity);
     }

     private void initAllCells(double value, double viskosity){
        for (Coordinates tempCoord: this.coords){
            try {
                this.makeSingleFluidCell(tempCoord,value, viskosity );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
     }

     public boolean cellExists(Coordinates target){
        if (target.x >= 0 && target.x < sizeX){
            if (target.y >= 0 && target.y < sizeY){
                if (target.z >= 0 && target.z < sizeZ){
                    return true;
                }
            }
        }
        return false;
     }

     public List<Coordinates> getAllAdjacentCells(Coordinates centerCell){
        List<Coordinates> result = new ArrayList<>();
        if(this.cellExists(centerCell.getCellXMinus1())){
            result.add(centerCell.getCellXMinus1());
            }
         if(this.cellExists(centerCell.getCellYMinus1())){
             result.add(centerCell.getCellYMinus1());
         }
         if(this.cellExists(centerCell.getCellZMinus1())){
             result.add(centerCell.getCellZMinus1());
         }
         if(this.cellExists(centerCell.getCellXPlus1())){
             result.add(centerCell.getCellXPlus1());
         }
         if(this.cellExists(centerCell.getCellYPlus1())){
             result.add(centerCell.getCellYPlus1());
         }
         if(this.cellExists(centerCell.getCellZPlus1())){
             result.add(centerCell.getCellZPlus1());
         }
         return result;
     }

     public List<Coordinates> getAllAdjacentCellsForIsFluid(Coordinates centerCell, boolean isFluid){
        if (isFluid) {
            return this.getAllAdjacentFluidCells(centerCell);
        } else {
            return this.getAllAdjacentSolidCells(centerCell);
        }
     }


    private boolean cellIsFluidRandCell(Coordinates coord){
        if (this.getAllAdjacentSolidCells(coord).size() > 0){
            return true;
        }
        else {
            return false;
        }
    }

     public List<Coordinates> getCellsForSolidCalculation(){
        for (Coordinates coord:this.coords){
            if (this.getCell(coord).isSolid()){
                this.cellsForSolidCalculation.add(coord);
            }
            else if (this.getCell((coord)).isFluid){
                if (this.cellIsFluidRandCell(coord)){
                    this.cellsForSolidCalculation.add(coord);
                }
            }
        }
        return this.cellsForSolidCalculation;
     }



     public List<Coordinates> getAllAdjacentFluidCells(Coordinates centerFluidCell){

         List<Coordinates> result = new ArrayList<>();
         if(this.cellExists(centerFluidCell.getCellXMinus1()) && this.getCell(centerFluidCell.getCellXMinus1()).isFluid){
             result.add(centerFluidCell.getCellXMinus1());
         }
         if(this.cellExists(centerFluidCell.getCellYMinus1())&& this.getCell(centerFluidCell.getCellYMinus1()).isFluid){
             result.add(centerFluidCell.getCellYMinus1());
         }
         if(this.cellExists(centerFluidCell.getCellZMinus1())&& this.getCell(centerFluidCell.getCellZMinus1()).isFluid){
             result.add(centerFluidCell.getCellZMinus1());
         }
         if(this.cellExists(centerFluidCell.getCellXPlus1())&& this.getCell(centerFluidCell.getCellXPlus1()).isFluid){
             result.add(centerFluidCell.getCellXPlus1());
         }
         if(this.cellExists(centerFluidCell.getCellYPlus1())&& this.getCell(centerFluidCell.getCellYPlus1()).isFluid){
             result.add(centerFluidCell.getCellYPlus1());
         }
         if(this.cellExists(centerFluidCell.getCellZPlus1())&& this.getCell(centerFluidCell.getCellZPlus1()).isFluid){
             result.add(centerFluidCell.getCellZPlus1());
         }
         return result;
     }

    public List<Coordinates> getAllAdjacentSolidCells(Coordinates centerSolidCell){

        List<Coordinates> result = new ArrayList<>();
        if(this.cellExists(centerSolidCell.getCellXMinus1()) && this.getCell(centerSolidCell.getCellXMinus1()).isSolid()){
            result.add(centerSolidCell.getCellXMinus1());
        }
        if(this.cellExists(centerSolidCell.getCellYMinus1())&& this.getCell(centerSolidCell.getCellYMinus1()).isSolid()){
            result.add(centerSolidCell.getCellYMinus1());
        }
        if(this.cellExists(centerSolidCell.getCellZMinus1())&& this.getCell(centerSolidCell.getCellZMinus1()).isSolid()){
            result.add(centerSolidCell.getCellZMinus1());
        }
        if(this.cellExists(centerSolidCell.getCellXPlus1())&& this.getCell(centerSolidCell.getCellXPlus1()).isSolid()){
            result.add(centerSolidCell.getCellXPlus1());
        }
        if(this.cellExists(centerSolidCell.getCellYPlus1())&& this.getCell(centerSolidCell.getCellYPlus1()).isSolid()){
            result.add(centerSolidCell.getCellYPlus1());
        }
        if(this.cellExists(centerSolidCell.getCellZPlus1())&& this.getCell(centerSolidCell.getCellZPlus1()).isSolid()){
            result.add(centerSolidCell.getCellZPlus1());
        }
        return result;
    }

    private void initCoords(){
        coords = new ArrayList<Coordinates>();
        for (int z =0; z< this.sizeZ; z++){
            for (int y =0; y< this.sizeY; y++){
                for (int x =0; x< this.sizeX; x++){
                    coords.add(new Coordinates(x,y,z));
                }
            }
        }
    }

    public void setCellsToInitialized(List<Coordinates> list){
        for (Coordinates tempCoord: list){
            this.getCell(tempCoord).isInitialized = true;
        }
    }

    public void makeCubeFluidCells(int x1, int y1, int z1, int x2, int y2, int z2, double value, double viskosity){
        this.makeCubeFluidCells(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2), value, viskosity);
    }

    public void makeCubeFluidCells(Coordinates chord1, Coordinates chord2, double value, double viskosity){
        for(Coordinates coords:Coordinates.getCoordsbetween(chord1, chord2)){
            try {
                this.makeSingleFluidCell(coords, value, viskosity);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
    }

    public void makeSingleFluidCell(Coordinates coord, double value, double viskosity) throws Exception{
        this.makeSingleFluidCell(coord.x, coord.y, coord.z, value, viskosity);
    }

    public void makeSingleFluidCell(int x, int y, int z, double value, double viskosity) throws Exception{
        this.cells[x][y][z]= new FluidCell(value, viskosity);
    }

    public void makeCubeSolidCells(int x1, int y1, int z1, int x2, int y2, int z2, double value, double alpha){
        this.makeCubeSolidCells(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2), value, alpha);

    }

    public void makeCubeSolidCells(Coordinates chord1, Coordinates chord2, double value, double alpha){
        for(Coordinates coords:Coordinates.getCoordsbetween(chord1, chord2)){
            try {
                this.makeSingleSolidCell(coords, value, alpha);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
    }

    public void makeSingleSolidCell(int x, int y, int z, double value, double alpha) throws Exception{
        this.cells[x][y][z]= new SolidCell(value, alpha);
    }
    public void makeSingleSolidCell(Coordinates coord, double value, double alpha) throws Exception{
        this.cells[coord.x][coord.y][coord.z]= new SolidCell(value, alpha);
    }

    public Cell getCell(int x, int y, int z){
        return this.cells[x][y][z];
    }

    public Cell getCell(Coordinates tempCoords){
        return this.cells[tempCoords.x][tempCoords.y][tempCoords.z];
    }
}
