package Heatequation.Cells;


import Heatequation.HeatequationLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cells implements Serializable {
    Cell[][][] cells;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    List<Coordinates> coords;
    List<Coordinates> cellsForSolidCalculation;
    public static double gasConstant = 0.0001;
    public static double cellSize = 1;
    HeatequationLogger logger;

    public Cells(int size, double value, Material material, HeatequationLogger logger){
        this.sizeX = size;
        this.sizeY = size;
        this.sizeZ = size;
        this.logger= logger;
        cells = new Cell[size][size][size];
        initCoords();
        initAllCells(value, material);
        cellsForSolidCalculation = new ArrayList<>();

    }

    public Cells(int sizeX, int sizeY, int sizeZ, double value, Material material, HeatequationLogger logger){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.logger= logger;
        cells = new Cell[sizeX][sizeY][sizeZ];
        this.initCoords();
        this.initAllCells(value, material);

     }

     private void initAllCells(double value, Material material){
        for (Coordinates tempCoord: this.coords){
            try {
                if (material.isFluid()) {
                    this.makeSingleFluidCell(tempCoord, value, material);
                } else {
                    this.makeNewSolidCell(tempCoord,value,material);
                }
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

    public String setBoundariesForCube(int x1, int y1, int z1, int x2, int y2, int z2, Double constantTemperature, Double heatFlow, Double startingTemperature){
        return this.setBoundariesForCube(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2), constantTemperature, heatFlow, startingTemperature);

    }

    public List<Coordinates> getAllCoordinates(){
        List<Coordinates> result = new ArrayList<Coordinates>();
        for(int x =0; x< sizeX;x++){
            for(int y =0; y< sizeY;y++){
                for(int z =0;z< sizeZ;z++){
                    result.add(new Coordinates(x,y,z));

                }
            }
        }
        return result;
    }

    public List<Cell> getAllCells(){
        List<Cell> result = new ArrayList<Cell>();
        for(int x =0; x< sizeX;x++){
            for(int y =0; y< sizeY;y++){
                for(int z =0;z< sizeZ;z++){
                    result.add(getCell(x,y,z));
                }
            }
        }
        return result;
    }

    public double getMaximumTemperature(){
        double maxValue = -1;
        for(Cell cell:this.getAllCells()){
            if (cell.getValue() >maxValue){
                maxValue=cell.getValue();
            }
        }
        return maxValue;
    }

    public double getMinimumTemperature(){
        double minValue = 1000;
        for(Cell cell:this.getAllCells()){
            if (cell.getValue() <minValue){
                minValue=cell.getValue();
            }
        }
        return minValue;
    }


    public String setBoundariesForCube(Coordinates coords1, Coordinates coords2, Double constantTemperature, Double heatFlow, Double startinTemperature){
        int counter = 0;
        int tempCounter = 0;
        for(Coordinates coord:Coordinates.getCoordsbetween(coords1, coords2)){
            try {
                if (this.cellExists(coord)) {
                    if (startinTemperature>=0) {
                        this.getCell(coord).setValue(startinTemperature);
                        this.getCell(coord).setOldValue();
                        tempCounter++;
                    }
                    if (this.getCell(coord).isSolid()) {
                        this.getCell(coord).setConstantTemperature(constantTemperature);
                        this.getCell(coord).setHeatFlow(heatFlow);
                        counter++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
        if (counter >0){
            String result = "added ";
            if (heatFlow >= 0){
                result += "heat flow " + heatFlow;
            }
            if (constantTemperature >=0){
                if (result.length() >10) {
                    result += " and ";
                }
                result += "constant temperature " + constantTemperature;
            }
            result+= " to " + counter + " cells";
            return result;
        } else if(tempCounter > 0){
            return "temperature " + startinTemperature + " set to " + tempCounter + " cells";

        } else {
            return "No boundary conditions set - they can only be set for solid cells";
        }

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
        this.cellsForSolidCalculation=new ArrayList<Coordinates>();
        for (Coordinates coord:this.coords){
            if (this.getCell(coord).isSolid()){
                this.cellsForSolidCalculation.add(coord);
            }
            else if (this.getCell((coord)).isFluid){
                if (this.cellIsFluidRandCell(coord)){
                    this.cellsForSolidCalculation.add(coord);
                    this.getCell(coord).setForSolidCalculation(true);
                }
            }
        }
        return this.cellsForSolidCalculation;
     }


   /* public List<Coordinates> getAllAdjacentCellsForSolidCalculation(Coordinates centerFluidCell){

        List<Coordinates> result = new ArrayList<>();
        if(this.cellExists(centerFluidCell.getCellXMinus1()) && this.getCell(centerFluidCell.getCellXMinus1()).isForSolidCalculation()){
            result.add(centerFluidCell.getCellXMinus1());
        }
        if(this.cellExists(centerFluidCell.getCellYMinus1())&& this.getCell(centerFluidCell.getCellYMinus1()).isForSolidCalculation){
            result.add(centerFluidCell.getCellYMinus1());
        }
        if(this.cellExists(centerFluidCell.getCellZMinus1())&& this.getCell(centerFluidCell.getCellZMinus1()).isForSolidCalculation){
            result.add(centerFluidCell.getCellZMinus1());
        }
        if(this.cellExists(centerFluidCell.getCellXPlus1())&& this.getCell(centerFluidCell.getCellXPlus1()).isForSolidCalculation){
            result.add(centerFluidCell.getCellXPlus1());
        }
        if(this.cellExists(centerFluidCell.getCellYPlus1())&& this.getCell(centerFluidCell.getCellYPlus1()).isForSolidCalculation){
            result.add(centerFluidCell.getCellYPlus1());
        }
        if(this.cellExists(centerFluidCell.getCellZPlus1())&& this.getCell(centerFluidCell.getCellZPlus1()).isForSolidCalculation){
            result.add(centerFluidCell.getCellZPlus1());
        }
        return result;
    }
    */

    public List<Coordinates> getAllAdjacentCellsForSolidCalculation(Coordinates centerFluidCell){

        List<Coordinates> result = new ArrayList<>();
        if (this.getCell(centerFluidCell).isSolid()) {
            if (this.cellExists(centerFluidCell.getCellXMinus1())) {
                result.add(centerFluidCell.getCellXMinus1());
            }
            if (this.cellExists(centerFluidCell.getCellYMinus1())) {
                result.add(centerFluidCell.getCellYMinus1());
            }
            if (this.cellExists(centerFluidCell.getCellZMinus1())) {
                result.add(centerFluidCell.getCellZMinus1());
            }
            if (this.cellExists(centerFluidCell.getCellXPlus1())) {
                result.add(centerFluidCell.getCellXPlus1());
            }
            if (this.cellExists(centerFluidCell.getCellYPlus1())) {
                result.add(centerFluidCell.getCellYPlus1());
            }
            if (this.cellExists(centerFluidCell.getCellZPlus1())) {
                result.add(centerFluidCell.getCellZPlus1());
            }
        } else {
            if(this.cellExists(centerFluidCell.getCellXMinus1()) && this.getCell(centerFluidCell.getCellXMinus1()).isSolid()){
                result.add(centerFluidCell.getCellXMinus1());
            }
            if(this.cellExists(centerFluidCell.getCellYMinus1())&& this.getCell(centerFluidCell.getCellYMinus1()).isSolid()){
                result.add(centerFluidCell.getCellYMinus1());
            }
            if(this.cellExists(centerFluidCell.getCellZMinus1())&& this.getCell(centerFluidCell.getCellZMinus1()).isSolid()){
                result.add(centerFluidCell.getCellZMinus1());
            }
            if(this.cellExists(centerFluidCell.getCellXPlus1())&& this.getCell(centerFluidCell.getCellXPlus1()).isSolid()){
                result.add(centerFluidCell.getCellXPlus1());
            }
            if(this.cellExists(centerFluidCell.getCellYPlus1())&& this.getCell(centerFluidCell.getCellYPlus1()).isSolid()){
                result.add(centerFluidCell.getCellYPlus1());
            }
            if(this.cellExists(centerFluidCell.getCellZPlus1())&& this.getCell(centerFluidCell.getCellZPlus1()).isSolid()){
                result.add(centerFluidCell.getCellZPlus1());
            }
        }
        return result;
    }

    public List<Coordinates> getAllFluidCells(){
        List<Coordinates> allFluidCells=new ArrayList<Coordinates>();
        for (Coordinates coord:this.coords){
            if (this.getCell(coord).isFluid()){
                allFluidCells.add(coord);
            }
         }
        return allFluidCells;
    }

    public double getMeanLastValueFor(List<Coordinates> cellsForCalculation){
        double result =0;
        for(Coordinates singleCell: cellsForCalculation){
            result += this.getCell(singleCell).getLastValue();
        }
        return result/cellsForCalculation.size();
    }

    public int getNumberOfAdjacentFluidCells(Coordinates centerCell){
        int result = this.getAllAdjacentFluidCells(centerCell).size();
            result += this.getCell(centerCell).getAsFluidCell().getNumberOfVirtualBorders();
            return result;

    }

    public List<Coordinates> getAllAdjacentFluidCells(Coordinates centerFluidCell){

         List<Coordinates> result = new ArrayList<>();
         //TODO ??
         if(this.getCell(centerFluidCell).isFluid){
             FluidCell fluidCell = this.getCell(centerFluidCell).getAsFluidCell();
             if(fluidCell.isBorderCell()){
             }

         }
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

    public void updateAllOldValues(){
        for(int x=0; x < sizeX ;x++){
            for(int y=0; y < sizeY ;y++){
                for(int z=0; z < sizeZ ;z++){
                    cells[x][y][z].setOldValue();
                }
            }
        }
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

    public void createAllVirtualBorderCells(double temp){
        for(Coordinates coord:this.coords){
            int border =0;
            if (coord.x == 0 || coord.x == sizeX-1){
                border ++;
            }
            if (coord.y == 0 || coord.y == sizeY-1){
                border ++;
            }
            if (coord.z == 0 || coord.z == sizeZ-1){
                border ++;

            }
            if (this.getCell(coord).isFluid) {
            this.logger.logMessage(HeatequationLogger.LogLevel.DEBUG, "setting " + border + " border cells for cell " + coord);

                this.getCell(coord).getAsFluidCell().setBorderCell(border, temp);

                if (coord.y == 0 || coord.y == sizeY - 1) {
                    this.getCell(coord).getAsFluidCell().setBorderCellOnTop();
                }
            }
        }

    }

    public void setCellsToInitialized(List<Coordinates> list){
        for (Coordinates tempCoord: list){
            this.getCell(tempCoord).isInitialized = true;
        }
    }

    public String makeCubeFluidCells(int x1, int y1, int z1, int x2, int y2, int z2, Material material){
        return this.makeCubeFluidCells(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2), material);
    }

    public String makeCubeFluidCells(Coordinates chord1, Coordinates chord2, Material material){
        int counter = 0;
        for(Coordinates coords:Coordinates.getCoordsbetween(chord1, chord2)){
            try {
                if (this.cellExists(coords)) {
                    this.makeSingleFluidCell(coords, this.getCell(coords).getValue(), material);
                    counter++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.logMessage(HeatequationLogger.LogLevel.ERROR, "info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
        if (counter > 0){
            return counter + " cells set to " + material.name;
        } else {
            return "no cells found";
        }
    }

    public void makeSingleFluidCell(Coordinates coord,double value, Material material) throws Exception{
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "new fluid cell: " + coord.x + coord.y+ coord.z);
        this.makeSingleFluidCell(coord.x, coord.y, coord.z, value, material);
    }

    public void makeSingleFluidCell(int x, int y, int z,double value, Material material) throws Exception{
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "new fluid cell: " + x + y+ z);

        this.cells[x][y][z]= new FluidCell(value, material, 1);
    }

    public String makeCubeSolidCells(int x1, int y1, int z1, int x2, int y2, int z2, Material material){
        return this.makeCubeSolidCells(new Coordinates(x1,y1,z1), new Coordinates(x2,y2,z2), material);

    }

    public String makeCubeSolidCells(Coordinates chord1, Coordinates chord2, Material material){
        int counter = 0;
        for(Coordinates coords:Coordinates.getCoordsbetween(chord1, chord2)){
            try {
                this.makeSingleSolidCell(coords, material);
                counter++;
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "info: Cordinate " + coords + " not found in cells. " + e.toString());
            }
        }
        if (counter > 0){
            return counter + " cells set to " + material.name;
        } else {
            return "no cells found";
        }
    }

    public void makeSingleSolidCell(int x, int y, int z, Material material) throws Exception{
        makeSingleSolidCell(new Coordinates(x,y,z), material);
    }

    private void makeNewSolidCell(Coordinates coord, double value, Material material){
        this.cells[coord.x][coord.y][coord.z] = new SolidCell(material,value);

    }
    public void makeSingleSolidCell(Coordinates coord, Material material) throws Exception{
        if (coord.x >= sizeX || coord.y >= sizeY || coord.z >= sizeZ){
            return;
        }
        this.logger.logMessage(HeatequationLogger.LogLevel.INFO, "new solid cell: " + coord.x + coord.y+ coord.z);
        if(this.cells[coord.x][coord.y][coord.z].isSolid()){
            this.cells[coord.x][coord.y][coord.z].material = material;
        }else {
            this.cells[coord.x][coord.y][coord.z] = new SolidCell(material,this.cells[coord.x][coord.y][coord.z].getValue());
        }
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
                    result[y][x]=cells[x][y][value];
                }
            }
            return result;
        }
      return null;
    }
}
