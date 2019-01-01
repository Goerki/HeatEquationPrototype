package Cells;


import java.util.ArrayList;
import java.util.List;

public class Cells {
    Cell[][][] cells;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    List<Coordinates> coords;
    List<Coordinates> cellsForSolidCalculation;

    public Cells(int size, double value, Material material){
        this.sizeX = size;
        this.sizeY = size;
        this.sizeZ = size;
        cells = new Cell[size][size][size];
        initCoords();
        initAllCells(value, material);
        cellsForSolidCalculation = new ArrayList<>();
    }

    public Cells(int sizeX, int sizeY, int sizeZ, double value, Material material){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        cells = new Cell[sizeX][sizeY][sizeZ];
        this.initCoords();
        this.initAllCells(value, material);
     }

     private void initAllCells(double value, Material material){
        for (Coordinates tempCoord: this.coords){
            try {
                this.makeSingleFluidCell(tempCoord,value, material );
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

    public void makeCubeFluidCells(int x1, int y1, int z1, int x2, int y2, int z2, Material material){
        this.makeCubeFluidCells(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2), material);
    }

    public void makeCubeFluidCells(Coordinates chord1, Coordinates chord2, Material material){
        for(Coordinates coords:Coordinates.getCoordsbetween(chord1, chord2)){
            try {
                this.makeSingleFluidCell(coords, this.getCell(coords).value , material);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
    }

    public void makeSingleFluidCell(Coordinates coord,double value, Material material) throws Exception{
        this.makeSingleFluidCell(coord.x, coord.y, coord.z, value, material);
    }

    public void makeSingleFluidCell(int x, int y, int z,double value, Material material) throws Exception{
        this.cells[x][y][z]= new FluidCell(value, material);
    }

    public void makeCubeSolidCells(int x1, int y1, int z1, int x2, int y2, int z2, Material material){
        this.makeCubeSolidCells(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2),material);

    }

    public void makeCubeSolidCells(Coordinates chord1, Coordinates chord2, Material material){
        for(Coordinates coords:Coordinates.getCoordsbetween(chord1, chord2)){
            try {
                this.makeSingleSolidCell(coords, material);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
    }

    public void makeSingleSolidCell(int x, int y, int z,double alpha, Material material) throws Exception{
        this.cells[x][y][z]= new SolidCell(material);
    }
    public void makeSingleSolidCell(Coordinates coord, Material material) throws Exception{
        if (coord.x >= sizeX || coord.y >= sizeY || coord.z >= sizeZ){
            return;
        }
        System.out.print("\nnew solid cell: " + coord.x + coord.y+ coord.z);
        this.cells[coord.x][coord.y][coord.z]= new SolidCell(material);
    }

    public Cell getCell(int x, int y, int z){
        return this.cells[x][y][z];
    }

    public Cell getCell(Coordinates tempCoords){
        return this.cells[tempCoords.x][tempCoords.y][tempCoords.z];
    }

    public Cell[][] getCellsForLayer(String layer, int value){
        if (layer.toLowerCase().contains("x")){
            Cell[][] result = new Cell[sizeY][sizeZ];
            for (int y=0; y<sizeY;y++){
                for (int z=0;z<sizeZ;z++){
                    result[y][z]=cells[value][y][z];
                }
            }
            return result;
        }
        if (layer.toLowerCase().contains("y")){
            Cell[][] result = new Cell[sizeZ][sizeX];
            for (int x=0; x<sizeX;x++){
                for (int z=0;z<sizeZ;z++){
                    result[z][x]=cells[x][value][z];
                }
            }
            return result;
        }
        if (layer.toLowerCase().contains("z")){
            Cell[][] result = new Cell[sizeX][sizeY];
            for (int x=0; x<sizeX;x++){
                for (int y=0;y<sizeY;y++){
                    result[x][y]=cells[x][y][value];
                }
            }
            return result;
        }
      return null;
    }
}
