package Heatequation.hmi;;

import Heatequation.Cells.Cell;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class CustomColorRenderer extends DefaultTableCellRenderer{

    int numberRows;
    int numberCols;
    Color[][] color;
    Cell[][] cells;
    double minValue;
    double maxValue;
    List<Color> heatColorMap;
    JTextPane console;


    CustomColorRenderer(int rows, int cols, JTextPane console){
        numberCols = cols;
        numberRows= rows;
        this.color = new Color[rows][cols];
        minValue = 0;
        maxValue =0;
        heatColorMap = new ArrayList<>();
        setHeatColorMapFromJson();
        this.console = console;
        }

    private void setHeatColorMapFromJson(){
        String filePath = new File("").getAbsolutePath();
        String fileData = null;
        try {
            fileData = new String(Files.readAllBytes(Paths
                    .get("hmi\\jsonFiles\\heatColors.json")));


        Gson gson = new Gson();
        JsonObject allColors = gson.fromJson(fileData, JsonObject.class).getAsJsonObject("Colors");
        Integer index = 1;
        while(allColors.get(index.toString())!= null){
            try {
                heatColorMap.add(setColorFromHexString(allColors.get(index.toString()).getAsString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color setColorFromHexString(String hexCode) throws Exception{
        if (hexCode.startsWith("#")){
            hexCode = hexCode.substring(1, 6);
        }
        if (hexCode.length() !=6){
            throw new Exception("Could not select Color from hex string " + hexCode);
        }
        return new Color(
                Integer.valueOf(hexCode.substring(0, 2), 16),
                Integer.valueOf(hexCode.substring(2, 4), 16),
                Integer.valueOf(hexCode.substring(4, 6), 16));
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cellComponent.setBackground(this.color[row][column]);
        cellComponent.enableInputMethods(true);

        return cellComponent;
    }

    public void setMinTemp(double value){
        this.minValue = value;
    }
    public void setMaxTemp(double value){
        this.maxValue = value;
    }

    public void setColors(Cell[][] cells, String type){
        this.cells = cells;
        if(type.contains("mat")){
            int reverseRows = numberRows;
            for(int row=0 ;row < this.numberRows;row ++){
                reverseRows--;
                for(int col =0; col < numberCols;col ++){
                    this.color[row][col] = getMaterialColor(cells[reverseRows][col]);

                }
            }
        }
        if(type.contains("heat")){
            int reverseRows = numberRows;
            for(int row=0 ;row < this.numberRows;row ++){
                reverseRows--;
                for(int col =0; col < numberCols;col ++){
                    this.color[row][col] = getTemperatureColor(cells[reverseRows][col]);

                }
            }
        }
    }

    private Color getMaterialColor(Cell cell){
        return cell.material.color;

    }

    private int getNeutralIndex(){
        return this.heatColorMap.size()/2;
    }

    private int getIndexForValue(double value){
        Double indexDouble = (value-this.minValue)*(this.heatColorMap.size()-1)/(this.maxValue - this.minValue);
        //System.out.print("value: " + value + " matches index " + indexDouble + " and RGB " + heatColorMap.get(indexDouble.intValue()).getRed() +heatColorMap.get(indexDouble.intValue()).getGreen()+ heatColorMap.get(indexDouble.intValue()).getBlue()+ "\n" );
        return indexDouble.intValue();
    }

    private Color getTemperatureColor(Cell cell){
        if(minValue == maxValue){
            return this.heatColorMap.get(getNeutralIndex());
        }
        if (cell.getValue() == Double.NaN){
            return new Color(0);
        }
        return this.heatColorMap.get(getIndexForValue(cell.getValue()));
    }
}