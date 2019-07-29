package Heatequation.hmi;

import Heatequation.Cells.Cell;
import Heatequation.Space;

import javax.swing.*;

public class DrawingTable extends JTable {
    Space space;
    String axis;
    String type;
    int numberRows;
    int numberColumns;
    int layer;
    Cell[][] drawingCells;
    JTextPane console;
    protected int defaultRowHeigth;


    public DrawingTable(){

    }

    protected int setRowHeigthForNumberRows(){

        int size = 600;
        size /= this.numberRows;
        this.defaultRowHeigth = size;
        return size;
    }

    public void setMinAndMaxValues(double minValue, double maxValue){
    }

    public void updateTable(String axis, int layer, String type){


    }

    public void updateTableWithHistory(String axis, int layer, String type, int time){

    }

    private void setTableRenderer(){

    }

    private void setRowAndColumnsSize(){

    }

    public void setCells(int newLayer){


    }
    public void setCellsFromHistory(int newLayer, int time){
    }
}
