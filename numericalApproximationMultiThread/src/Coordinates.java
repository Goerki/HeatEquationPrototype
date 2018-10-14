import Cell.Cell;

final class Coordinates {
    int x;
    int y;
    int z;
    int dimension;

    Coordinates(int x, int y, int z, int dimension){
        this.x =x;
        this.y =y;
        this.z = z;
        this.dimension = dimension;

    }

    static int getNumberInSystem(int x, int y, int z, int dimension){

       return dimension*dimension*z + dimension*y + x;
    }

    static Coordinates getCoordinatesForNumber(int number, int dimension){
        int z=0;
        while(number > dimension*dimension) {
            number -= dimension*dimension;
            z++;
        }
        int y=0;
        while(number > dimension) {
            number -= dimension;
            y++;
        }
        int x=number;
        return new Coordinates(x,y,z, dimension);
    }

    static int getNumberInSystem(Coordinates coords){
        return getNumberInSystem(coords.x, coords.y, coords.z, coords.dimension);
    }

    public Coordinates getCoordsX(int offset) throws Exception{
        if ((this.x + offset) >= this.dimension){
            throw new Exception("coords out of range. x-value: " + this.x + " offset: " + offset + " dimension " + this.dimension);
        }
        return new Coordinates(this.x+offset, this.y, this.z, this.dimension);
    }

    public Coordinates getCoordsY(int offset) throws Exception{
        if ((this.y + offset) >= this.dimension){
            throw new Exception("coords out of range. y-value: " + this.y + " offset: " + offset + " dimension " + this.dimension);
        }
        return new Coordinates(this.x, this.y+offset, this.z, this.dimension);
    }

    public Coordinates getCoordsZ(int offset) throws Exception{
        if ((this.z + offset) >= this.dimension){
            throw new Exception("coords out of range. z-value: " + this.z + " offset: " + offset+ " dimension " + this.dimension );
        }
        return new Coordinates(this.x, this.y, this.z+offset, this.dimension);
    }


    public boolean increase(){
        if (this.x < dimension-1){
            this.x = this.x+1;
            return true;
        }else {
            x =0;
        }
        if (this.y <  dimension-1){
            this.y = this.y+1;
            return true;
        }else {
            y=0;
        }
        if (this.z <  dimension-1){
            this.z = this.z+1;
            return true;
        }else {
            return false;
        }
       }

    public String toString(){
        return "x: " + this.x + " y: " + this.y + " z: " + this.z + " dim: " + this.dimension + "\n";
    }

       public static Cell[] transformTo1DimensionalCells(Cell[][][] cells){
           Coordinates coords = new Coordinates(0,0,0,cells.length);
           int dimension  = cells.length;
           Cell[] result = new Cell[dimension*dimension*dimension];

           for (int i=0; i < dimension*dimension*dimension; i ++){
               result[0] = cells[coords.x][coords.y][coords.z];
               coords.increase();
           }
           return result;
       }

    public static double[] transformTo1DimensionLastValue(Cell[][][] cells){
        Coordinates coords = new Coordinates(0,0,0,cells.length);
        int dimension  = cells.length;
        double[] result = new double[dimension*dimension*dimension];

        for (int i=0; i < dimension*dimension*dimension; i ++){
            result[i] = cells[coords.x][coords.y][coords.z].getLastValue();
            coords.increase();
        }
        return result;
    }
}
