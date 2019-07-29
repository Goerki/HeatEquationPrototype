package Heatequation.hmi;

import Heatequation.Space;

import javax.swing.*;

public class ArrowTable extends DrawingTable {

    CustomArrowRenderer arrowRenderer;



    public ArrowTable(Space space, String axis, int layer, String type, JTextPane console){

        this.axis=axis;
        this.type = type;
        this.layer = layer;
        this.space=space;
        setRowAndColumnsSize();
        this.setRowHeight(this.setRowHeigthForNumberRows());
        this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
        setCells(layer);
        this.console = console;
        arrowRenderer = new CustomArrowRenderer(this.numberRows,this.numberColumns, this.console, this.defaultRowHeigth,axis, space.getMinParticleFlow(), space.getMaxParticleFlow(), drawingCells);
        arrowRenderer.setMaxTemp(space.getMaximumTemperature());
        arrowRenderer.setMinTemp(space.getMinimumTemperature());
        arrowRenderer.setCells(drawingCells, this.axis);
        setTableRenderer();
    }

    @Override
    public void setMinAndMaxValues(double minValue, double maxValue){
        arrowRenderer.setMinTemp(minValue);
        arrowRenderer.setMaxTemp(maxValue);
    }

    @Override
    public void updateTable(String axis, int layer, String type){
        if (!axis.equals(this.axis)){
            this.axis = axis;
            setRowAndColumnsSize();
            this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
            arrowRenderer = new CustomArrowRenderer(this.numberRows, this.numberColumns, this.console, this.defaultRowHeigth, axis, space.getMinParticleFlow(), space.getMaxParticleFlow(), drawingCells);
            arrowRenderer.setMaxTemp(space.getMaximumTemperature());
            arrowRenderer.setMinTemp(space.getMinimumTemperature());

        }
        this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
        arrowRenderer = new CustomArrowRenderer(this.numberRows, this.numberColumns, this.console,this.defaultRowHeigth,axis, space.getMinParticleFlow(), space.getMaxParticleFlow(), drawingCells);
        arrowRenderer.setMaxTemp(space.getMaximumTemperature());
        arrowRenderer.setMinTemp(space.getMinimumTemperature());

        this.layer=layer;
        setCells(layer);
        //arrowRenderer.setColors(drawingCells,type);
        setTableRenderer();

    }

    @Override
    public void updateTableWithHistory(String axis, int layer, String type, int time){
        if (!axis.equals(this.axis)){
            this.axis = axis;
            setRowAndColumnsSize();
            this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
            arrowRenderer = new CustomArrowRenderer(this.numberRows, this.numberColumns, this.console,this.defaultRowHeigth,axis, space.getMinParticleFlow(), space.getMaxParticleFlow(), drawingCells);
            arrowRenderer.setMaxTemp(space.getMaximumTemperature());
            arrowRenderer.setMinTemp(space.getMinimumTemperature());

        }
        this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
        arrowRenderer = new CustomArrowRenderer(this.numberRows, this.numberColumns, this.console,this.defaultRowHeigth,axis, space.getMinParticleFlow(), space.getMaxParticleFlow(), drawingCells);
        arrowRenderer.setMaxTemp(space.getMaximumTemperature());
        arrowRenderer.setMinTemp(space.getMinimumTemperature());

        this.layer=layer;
        setCellsFromHistory(layer, time);
        //arrowRenderer.setColors(drawingCells,type);
        setTableRenderer();

    }


    private void setTableRenderer(){
        for(int column=0;column<numberColumns;column++){
            this.getColumnModel().getColumn(column).setCellRenderer(this.arrowRenderer);
        }
    }

    private void setRowAndColumnsSize(){
        if (axis.toLowerCase().contains("x")){
            this.numberRows = space.allCells.sizeY;
            this.numberColumns=space.allCells.sizeZ;
            return;
        }
        if (axis.toLowerCase().contains("y")){
            this.numberRows = space.allCells.sizeZ;
            this.numberColumns=space.allCells.sizeX;
            return;
        }
        if (axis.toLowerCase().contains("z")){
            this.numberRows = space.allCells.sizeX;
            this.numberColumns=space.allCells.sizeY;
            return;
        }
    }

    @Override
    public void setCells(int newLayer){
        drawingCells = space.allCells.getCellsForLayer(this.axis, this.layer);

    }

    @Override
    public void setCellsFromHistory(int newLayer, int time){
        drawingCells = space.getCellsForLayerAndTime(this.axis, newLayer, time);


    }
}
