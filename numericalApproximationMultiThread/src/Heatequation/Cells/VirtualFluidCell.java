package Heatequation.Cells;

public class VirtualFluidCell {
    private int numberBorders;
    private double temperature;
    private boolean isOnTop;
    private double pressure;

    public VirtualFluidCell(int numberBorders, double temp){
        this.numberBorders = numberBorders;
        this.temperature = temp;
        isOnTop = false;
        this.pressure = 1;
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
}
