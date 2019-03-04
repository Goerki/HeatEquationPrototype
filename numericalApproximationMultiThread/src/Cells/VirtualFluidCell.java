package Cells;

public class VirtualFluidCell {
    private int numberBorders;
    private double temperature;
    private boolean isOnTop;

    public VirtualFluidCell(int numberBorders, double temp){
        this.numberBorders = numberBorders;
        this.temperature = temp;
        isOnTop = false;
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
