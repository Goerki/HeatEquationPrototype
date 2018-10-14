
public class Space {
    Cell[][][] cells;
    int size;

    Space(int size){
        cells = new Cell[size][size][size];
        this.size = size;
        for (int i=0; i< size; i++){
            for(int j=0;j< size; j++){
                for(int k=0; k<size; k++){
                    cells[i][j][k] = new Cell(i+j+k);
                }
            }
        }
    }

    private long calcTemperatureFlowFromCell(int x, int y, int z, Cell cell) {
        if (cell == null) {
            return 0;
        }
        Cell tempCell = this.getCell(x, y, z);
        if (tempCell == null) {
            return 0;
        }
        return (cell.getValue() - tempCell.getLastValue())/size;
    }

    Cell getCell(int x, int y, int z){
        if (x < 0 || x >= size) {
            return null;
        }
        if (y < 0 || y >= size) {
            return null;
        }
        if (z < 0 || z >= size) {
            return null;
        }
        return this.cells[x][y][z];
    }


    public void calcNewCellValue(int x, int y, int z) {

        this.cells[x][y][z].setValue(this.getCell(x,y,z).getValue() + calcTemperatureFlowFromCell(x,y,z,this.getCell(x-1,y,z))
                + calcTemperatureFlowFromCell(x,y,z,this.getCell(x+1,y,z))
                + calcTemperatureFlowFromCell(x,y,z,this.getCell(x,y-1,z))
                + calcTemperatureFlowFromCell(x,y,z,this.getCell(x,y+1,z))
                + calcTemperatureFlowFromCell(x,y,z,this.getCell(x,y,z-1))
                + calcTemperatureFlowFromCell(x,y,z,this.getCell(x,y,z+1)));
    }

    public void calcSpace(){
        for(int x = 0; x<this.size; x++){
            for(int y = 0; y<this.size; y++){
                for(int z = 0; z<this.size; z++){
                    this.calcNewCellValue(x,y,z);
                }
            }
        }
    }

    public String toString(){

        String result = "";
        for(int x = 0; x<this.size; x++){
            result += "\n\n\n";
            System.out.print("\nx=" + x);
            for(int y = 0; y<this.size; y++){
                result += "\n";
                for(int z = 0; z<this.size; z++){
                    result +=this.getCell(x,y,z).getValue() + " ";

                }
            }
        }
        return result;
    }
}
