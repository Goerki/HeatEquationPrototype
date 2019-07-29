package Heatequation.hmi;

import Heatequation.Cells.Cell;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

;

class CustomArrowRenderer implements TableCellRenderer {

    int numberRows;
    int numberCols;
    Cell[][] cells;
    double minValue;
    double maxValue;
    ArrowIcons icons;
    String iconPath;
    JTextPane console;
    int rowSize;


    CustomArrowRenderer(int rows, int cols, JTextPane console, int rowSize, String axis,double minValue, double maxValue, Cell[][] cells){
        numberCols = cols;
        numberRows= rows;
        this.minValue = minValue;
        this.maxValue =maxValue;
        this.console = console;
        this.iconPath = "C:\\Users\\thoni\\IdeaProjects\\HeatEquationPrototype\\hmi\\jsonFiles\\arrow_right.jpg";
        this.rowSize = rowSize;
        this.setCells(cells, axis);
        this.icons = new ArrowIcons(this.iconPath,axis,10,(int) ((double) rowSize*0.7), this.minValue, this.maxValue);


        }


    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){

        Component cellComponent;

        if (this.cells[row][column].isFluid()){

            try {
                cellComponent = new JLabel();
                ((JLabel) cellComponent).setIcon(new ImageIcon(this.icons.getImageForCell(this.cells[row][column].getAsFluidCell())));
            } catch (Exception e) {
                cellComponent = new JTextField();
                e.printStackTrace();
                cellComponent.setBackground(Color.WHITE);
            }

        } else {
            cellComponent = new JTextField();
            cellComponent.setBackground(Color.BLACK);
        }
        cellComponent.enableInputMethods(true);




        //cellComponent.prepareImage(arrow, cellComponent);

        //arrow.flush();

        //boolean status = graph.drawImage(arrow,0,0,30,30,cellComponent);





        /*
        //cellComponent.enableInputMethods(true);
        cellComponent.setSize(new Dimension(255,255));

        int check = cellComponent.checkImage(arrow, cellComponent);
        cellComponent.setBackground(Color.CYAN);
        cellComponent.enable();
        cellComponent.setVisible(true);


*/
        return cellComponent;
    }




    /*

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cellComponent.setBackground(this.color[row][column]);
        cellComponent.enableInputMethods(true);

        return cellComponent;
    }
    */

    public void setMinTemp(double value){
        this.minValue = value;
    }
    public void setMaxTemp(double value){
        this.maxValue = value;
    }

    public void setCells(Cell[][] cells, String axis){
        this.cells = new Cell[cells.length][cells[0].length];

            for (int column = 0; column < cells[0].length; column++){

                int reverseRow =cells.length;
                for (int row= 0; row<cells.length; row ++) {
                    reverseRow--;
                    this.cells[row][column] = cells[reverseRow][column];
            }

        }
        this.icons = new ArrowIcons(this.iconPath,axis,10,(int) ((double) rowSize*0.7),minValue, maxValue);

        }


    private int getNeutralIndex(){
        return 50;
    }

    private int getIndexForValue(double value){
        Double indexDouble = (value-this.minValue)*(99)/(this.maxValue - this.minValue);
        //System.out.print("value: " + value + " matches index " + indexDouble + " and RGB " + heatColorMap.get(indexDouble.intValue()).getRed() +heatColorMap.get(indexDouble.intValue()).getGreen()+ heatColorMap.get(indexDouble.intValue()).getBlue()+ "\n" );
        return indexDouble.intValue();
    }

    /*
    private Color getTemperatureColor(Cell cell){
        if(minValue == maxValue){
            return this.heatColorMap.get(getNeutralIndex());
        }
        if (cell.getValue() == Double.NaN){
            return new Color(0);
        }
        return this.heatColorMap.get(getIndexForValue(cell.getValue()));
    }
    */
}