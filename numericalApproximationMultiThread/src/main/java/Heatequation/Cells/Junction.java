package Heatequation.Cells;

import Heatequation.Space;

public class Junction {
    private Coordinates.direction direction;
    private Coordinates from;
    private Coordinates to;

    public Junction(Coordinates from, Coordinates to) throws Exception{
        this.from = from;
        this.to = to;

            this.getDirectionFromCoordinates(from, to, null);

    }

    public Junction(Coordinates from, Coordinates to, Space space) throws Exception{
        this.from = from;
        this.to = to;

        if (this.to == this.from){
            this.createJunctionToVirtualBorder(from, space);
        } else {
            this.getDirectionFromCoordinates(from, to, space);
        }

    }

    private void getDirectionFromCoordinates(Coordinates from, Coordinates to, Space space) throws Exception {
        if (this.to == this.from) {
            throw new Exception("Could not create virtual junction - not all data needed provided");

        } else {
            this.direction = null;
            if (from.x < to.x) {
                this.direction = Coordinates.direction.XPLUS1;
            }
            if (from.x > to.x) {
                if (direction == null) {
                    this.direction = Coordinates.direction.XMINUS1;
                    throw new Exception("Junction only allowed in positive directions");
                } else {
                    throw new Exception("could not get direction for junction from " + from.toString() + " to " + to.toString() + " - seems like cells are no neighbors");
                }
            }
            if (from.y > to.y) {
                if (direction == null) {
                    this.direction = Coordinates.direction.YMINUS1;
                    throw new Exception("Junction only allowed in positive directions");
                } else {
                    throw new Exception("could not get direction for junction from " + from.toString() + " to " + to.toString() + " - seems like cells are no neighbors");
                }
            }
            if (from.y < to.y) {
                if (direction == null) {
                    this.direction = Coordinates.direction.YPLUS1;
                } else {
                    throw new Exception("could not get direction for junction from " + from.toString() + " to " + to.toString() + " - seems like cells are no neighbors");
                }
            }
            if (from.z > to.z) {
                if (direction == null) {
                    this.direction = Coordinates.direction.ZMINUS1;
                    throw new Exception("Junction only allowed in positive directions");
                } else {
                    throw new Exception("could not get direction for junction from " + from.toString() + " to " + to.toString() + " - seems like cells are no neighbors");
                }
            }
            if (from.z < to.z) {
                if (direction == null) {
                    this.direction = Coordinates.direction.ZPLUS1;
                } else {
                    throw new Exception("could not get direction for junction from " + from.toString() + " to " + to.toString() + " - seems like cells are no neighbors");
                }
            }
        }
    }

    private void createJunctionToVirtualBorder(Coordinates from, Space space) {
        this.direction= space.allCells.getCell(from).getAsFluidCell().getVirtualBorderCell().getDirections().get(0);

    }

    public Coordinates getFrom() {
        return from;
    }

    public Coordinates getTo() {
        return to;
    }

    public Coordinates.direction getDirection(){
        return this.direction;
    }


    @Override
    public String toString(){
        return this.from.toString() + " " + this.direction.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object.getClass() != this.getClass()) {
            return false;
        }
        Junction testCoord = (Junction) object;
        return this.from.equals(testCoord.from) &&  this.to.equals(testCoord.to);
    }

    @Override
    public int hashCode() {
        return from.x + from.y + from.z + to.x+ to.y + to.z;
    }
}





