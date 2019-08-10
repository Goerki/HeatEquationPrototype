package Heatequation.Cells;

import java.util.List;

public class VirtualFluidCell {
    private int numberBorders;
    private double temperature;
    private boolean isOnTop;
    private boolean isOnBottom;
    private double pressure;
    private double energy;
    private List<Coordinates.direction> directions;

    public VirtualFluidCell(int numberBorders, double temp, double gasConstant, List<Coordinates.direction> directions){
        this.numberBorders = numberBorders;
        this.temperature = temp;
        isOnTop = false;
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

    public void setOnTop(boolean onTop){
        this.isOnTop = onTop;
    }

    public boolean isOnTop(){
        return this.isOnTop;
    }

    public boolean isOnBottom() {
        return isOnBottom;
    }

    public void setOnBottom(boolean onBottom) {
        isOnBottom = onBottom;
    }

    public List<Coordinates.direction> getDirections() {
        return directions;
    }

    public double getEnergy() {
        return this.energy;
    }
}
