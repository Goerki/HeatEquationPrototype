package Heatequation.hmi;

import Heatequation.Cells.Cell;
import Heatequation.Cells.Cells;

import javax.swing.*;

public class DrawingTable extends JTable {
    Cells cells;
    String axis;
    String type;
    int numberRows;
    int numberColumns;
    int layer;
    Cell[][] drawingCells;
    CustomRenderer tableRenderer;
    JTextPane console;


    public DrawingTable(Cells cells, String axis, int layer, String type, JTextPane console){
        this.axis=axis;
        this.type = type;
        this.layer = layer;
        this.cells=cells;
        setRowAndColumnsSize();
        this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
        setCells(layer);
        tableRenderer = new CustomRenderer(this.numberRows,this.numberColumns, console);
        this.console = console;
        tableRenderer.setMaxTemp(cells.getMaximumTemperature());
        tableRenderer.setMinTemp(cells.getMinimumTemperature());
        tableRenderer.setColors(drawingCells,type);
        setTableRenderer();
    }

    public void setMinAndMaxValues(double minValue, double maxValue){
        tableRenderer.setMinTemp(minValue);
        tableRenderer.setMaxTemp(maxValue);
    }

    public void updateTable(String axis, int layer, String type){
        if (!axis.equals(this.axis)){
            this.axis = axis;
            setRowAndColumnsSize();
            this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
            tableRenderer = new CustomRenderer(this.numberRows, this.numberColumns, this.console);
            tableRenderer.setMaxTemp(cells.getMaximumTemperature());
            tableRenderer.setMinTemp(cells.getMinimumTemperature());

        }
        this.layer=layer;
        setCells(layer);
        tableRenderer.setColors(drawingCells,type);
        setTableRenderer();

    }
    private void setTableRenderer(){
        for(int column=0;column<numberColumns;column++){
            this.getColumnModel().getColumn(column).setCellRenderer(this.tableRenderer);
        }
    }

    private void setRowAndColumnsSize(){
        if (axis.toLowerCase().contains("x")){
            this.numberRows = cells.sizeY;
            this.numberColumns=cells.sizeZ;
            return;
        }
        if (axis.toLowerCase().contains("y")){
            this.numberRows = cells.sizeZ;
            this.numberColumns=cells.sizeX;
            return;
        }
        if (axis.toLowerCase().contains("z")){
            this.numberRows = cells.sizeX;
            this.numberColumns=cells.sizeY;
            return;
        }
    }

    public void setCells(int newLayer){
        drawingCells = cells.getCellsForLayer(this.axis, this.layer);


    }
}
