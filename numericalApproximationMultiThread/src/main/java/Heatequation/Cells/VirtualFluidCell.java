package Heatequation.Cells;

import java.util.List;

public class VirtualFluidCell {
    private int numberBorders;
    private double temperature;
    private double pressure;
    private double energy;
    private List<Coordinates.direction> directions;

    public VirtualFluidCell(int numberBorders, double temp, double gasConstant, List<Coordinates.direction> directions){
        this.numberBorders = numberBorders;
        this.temperature = temp;
        this.pressure = 1;
        double numberParticles = this.pressure/temp*gasConstant;
        this.energy = numberParticles * this.temperature;
        this.directions = directions;
    }

    public double getPressure(){
        return this.pressure;
    }

    public int getNumberBorders() {
        return numberBorders;
    }

    public double getTemperature() {
        return temperature;
    }


    public boolean isOnTop(){
        return this.directions.contains(Coordinates.direction.YPLUS1);
    }

    public boolean isOnBottom() {
        return this.directions.contains(Coordinates.direction.YMINUS1);
    }

       public List<Coordinates.direction> getDirections() {
        return directions;
    }

    public double getEnergy() {
        return this.energy;
    }
}
