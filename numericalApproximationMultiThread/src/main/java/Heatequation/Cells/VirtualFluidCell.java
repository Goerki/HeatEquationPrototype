package Heatequation.Cells;

import java.util.List;

public class VirtualFluidCell {
    private int numberBorders;
    private double temperature;
    private boolean isOnTop;
    private boolean isOnBottom;
    private double pressure;
    private double numberParticles;
    private List<FluidCell.particleFlowSource> directions;

    public VirtualFluidCell(int numberBorders, double temp, double gasConstant, List<FluidCell.particleFlowSource> directions){
        this.numberBorders = numberBorders;
        this.temperature = temp;
        isOnTop = false;
        this.pressure = 1;
        this.numberParticles = this.pressure/temp*gasConstant;
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

    public List<FluidCell.particleFlowSource> getDirections() {
        return directions;
    }
}
