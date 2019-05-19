package Heatequation.Cells;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Coordinates implements Serializable {
    public int x;
    public int y;
    public int z;

    public Coordinates(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

    }

    static List<Coordinates> getCoordsbetween(Coordinates coords1, Coordinates coords2) {
        List<Coordinates> result = new ArrayList<>();
        int smallX = getSmallInt(coords1.x, coords2.x);
        int bigX = getGreatInt(coords1.x, coords2.x);
        int smallY = getSmallInt(coords1.y, coords2.y);
        int bigY = getGreatInt(coords1.y, coords2.y);
        int smallZ = getSmallInt(coords1.z, coords2.z);
        int bigZ = getGreatInt(coords1.z, coords2.z);

        for (int x = smallX; x <= bigX; x++) {
            for (int y = smallY; y <= bigY; y++) {
                for (int z = smallZ; z <= bigZ; z++) {
                    result.add(new Coordinates(x, y, z));
                }
            }
        }
        return result;

    }

    public Coordinates getCellXMinus1() {
        return new Coordinates(this.x - 1, this.y, this.z);
    }

    public Coordinates getCellYMinus1() {
        return new Coordinates(this.x, this.y - 1, this.z);
    }

    public Coordinates getCellZMinus1() {
        return new Coordinates(this.x, this.y, this.z - 1);
    }

    public Coordinates getCellXPlus1() {
        return new Coordinates(this.x + 1, this.y, this.z);
    }

    public Coordinates getCellYPlus1() {
        return new Coordinates(this.x, this.y + 1, this.z);
    }

    public Coordinates getCellZPlus1() {
        return new Coordinates(this.x, this.y, this.z + 1);
    }


    private static int getSmallInt(int int1, int int2) {
        if (int1 > int2) {
            return int2;
        } else {
            return int1;
        }
    }


    private static int getGreatInt(int int1, int int2) {
        if (int1 < int2) {
            return int2;
        } else {
            return int1;
        }
    }


    public boolean increase(int dimension) {
        if (this.x < dimension - 1) {
            this.x = this.x + 1;
            return true;
        } else {
            x = 0;
        }
        if (this.y < dimension - 1) {
            this.y = this.y + 1;
            return true;
        } else {
            y = 0;
        }
        if (this.z < dimension - 1) {
            this.z = this.z + 1;
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "x: " + this.x + " y: " + this.y + " z: " + this.z;
    }

    public static List<Coordinates> getAllCoordinates(int sizeX, int sizeY, int sizeZ) {
        List<Coordinates> result = new ArrayList<>();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    result.add(new Coordinates(x, y, z));
                }
            }
        }

        return result;
    }

    public static Coordinates getCoordinatesForParticleFlowSource(Coordinates centerCell, FluidCell.particleFlowSource source) {
        switch (source) {
            case XPLUS1: {
                return centerCell.getCellXPlus1();
            }
            case XMINUS1: {
                return centerCell.getCellXMinus1();
            }
            case YPLUS1: {
                return centerCell.getCellYPlus1();
            }
            case YMINUS1: {
                return centerCell.getCellYMinus1();
            }
            case ZPLUS1: {
                return centerCell.getCellZPlus1();
            }
            case ZMINUS1: {
                return centerCell.getCellZMinus1();
            }
        }
        return null;

    }

    public static FluidCell.particleFlowSource getSourceForCoordinates(Coordinates source, Coordinates target) {
        if (target.getCellYMinus1().equals(source)) {
            return FluidCell.particleFlowSource.YMINUS1;
        } else if (target.getCellYPlus1().equals(source)) {
            return FluidCell.particleFlowSource.YPLUS1;
        } else if (target.getCellXMinus1().equals(source)) {
            return FluidCell.particleFlowSource.XMINUS1;
        } else if (target.getCellXPlus1().equals(source)) {
            return FluidCell.particleFlowSource.XPLUS1;
        } else if (target.getCellZPlus1().equals(source)) {
            return FluidCell.particleFlowSource.ZPLUS1;
        } else if (target.getCellZMinus1().equals(source)) {
            return FluidCell.particleFlowSource.ZMINUS1;
        }
        return null;

    }

    public static FluidCell.particleFlowSource getOppositeParticleFlowDirection(FluidCell.particleFlowSource source) {
        switch (source) {
            case XMINUS1:
                return FluidCell.particleFlowSource.XPLUS1;
            case XPLUS1:
                return FluidCell.particleFlowSource.XMINUS1;
            case YMINUS1:
                return FluidCell.particleFlowSource.YPLUS1;
            case YPLUS1:
                return FluidCell.particleFlowSource.YMINUS1;
            case ZMINUS1:
                return FluidCell.particleFlowSource.ZPLUS1;
            case ZPLUS1:
                return FluidCell.particleFlowSource.ZMINUS1;
        }
        return null;
    }


    public static boolean equals(Coordinates coord1, Coordinates coord2) {
        if (coord1.x == coord2.x) {
            if (coord1.y == coord2.y) {
                if (coord1.z == coord2.z) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean equals(Coordinates coordinates) {
        return coordinates.x == this.x && this.y == coordinates.y && coordinates.z == this.z;
    }


    @Override
    public boolean equals(Object object) {
        if (object.getClass() != this.getClass()) {
            return false;
        }
        Coordinates testCoord = (Coordinates) object;
        return this.equals(testCoord);
    }

    @Override
    public int hashCode() {
        return x + y + z;
    }
}
