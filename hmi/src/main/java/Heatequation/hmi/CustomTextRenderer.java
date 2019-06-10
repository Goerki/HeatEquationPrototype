package Heatequation.hmi;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.Element;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class CustomTextRenderer extends DefaultTableCellRenderer {
    List<String> keys;
    List<String> values;

    CustomTextRenderer(Map<String, String> keyValuesPairs){
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
        for (String key: keyValuesPairs.keySet()){
            keys.add(key);
            this.values.add(keyValuesPairs.get(key));
        }

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (cellComponent instanceof CustomTextRenderer){
            ((CustomTextRenderer) cellComponent).setText(this.getTextForCoords(row, column));
        }
        cellComponent.enableInputMethods(true);

        return cellComponent;
    }

    private String getTextForCoords(int row, int column) {
        if (column==0){
            return this.keys.get(row);

        } else {
            return this.values.get(row);
        }
    }
}
