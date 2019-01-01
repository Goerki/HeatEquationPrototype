import Cells.Cell;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class CustomRenderer extends DefaultTableCellRenderer{

    int numberRows;
    int numberCols;
    Color[][] color;


    CustomRenderer(int rows, int cols){

        numberCols = cols;
        numberRows= rows;
        this.color = new Color[rows][cols];

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cellComponent.setBackground(this.color[row][column]);
        return cellComponent;
    }

    public void setColors(Cell[][] cells, String type){
        if(type.contains("mat")){
            for(int row=0 ;row < this.numberRows;row ++){
                for(int col =0; col < numberCols;col ++){
                    this.color[row][col] = getMaterialColor(cells[row][col]);
                }
            }
        }
    }

    Color getMaterialColor(Cell cell){
        return cell.material.color;

    }
}