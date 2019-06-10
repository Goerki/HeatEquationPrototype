package Heatequation.hmi;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextTable extends JTable {
    int numberRows;
    Map<String,String> keyValuePairs;
    CustomTextRenderer tableRenderer;

    public TextTable(Map<String,String> keyValuePairs){
        this.numberRows = keyValuePairs.size();
        this.keyValuePairs = keyValuePairs;
        this.tableRenderer = new CustomTextRenderer(keyValuePairs);

        this.setModel(new CustomTableModel(2, this.numberRows));
        setTableRenderer();
    }


    private void setTableRenderer(){
        for(int column=0;column<this.getColumnCount();column++){
            this.getColumnModel().getColumn(column).setCellRenderer(this.tableRenderer);
        }
    }

}
