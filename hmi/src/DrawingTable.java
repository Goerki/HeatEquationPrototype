import Cells.Cell;
import Cells.Cells;
import net.sf.json.JSONObject;

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


    public DrawingTable(Cells cells, String axis, int layer, String type){
        this.axis=axis;
        this.type = type;
        this.layer = layer;
        this.cells=cells;
        setRowAndColumnsSize();
        this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
        setCells(layer);
        tableRenderer = new CustomRenderer(this.numberRows,this.numberColumns);
        tableRenderer.setColors(drawingCells,type);
        setTableRenderer();
    }

    public void updateTable(String axis, int layer, String type){
        if (!axis.equals(this.axis)){
            this.axis = axis;
            setRowAndColumnsSize();
            this.setModel(new CustomTableModel(this.numberColumns, this.numberRows));
            tableRenderer = new CustomRenderer(this.numberRows, this.numberColumns);
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
