import Heatequation.Space;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.io.*;

public class ShowSpaceDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JTabbedPane tableSelecter;
    private JPanel tablePanel;
    private JPanel layerSelectionPanel;
    private JComboBox axisSelection;
    private JSlider layerSlider;
    private JTextPane currentLayerSelectionTextPane;
    private JButton saveButton;
    private JTable table1;
    private JTextPane consoleTextPane;
    private DrawingTable drawingTable;
    Space space;

    public ShowSpaceDialog(Space space) {
        this.space = space;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        this.tableSelecter.setSelectedIndex(0);


        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        axisSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                newAxisSelection(axisSelection.getModel().getSelectedItem().toString());
            }
        });

        // call onSliderChanged when the layer Slider is changed
        layerSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderChanged(layerSlider.getValue());
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSave();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        newTable();
        drawTable();
        newAxisSelection("x");
        sliderChanged(0);

            System.out.print("reasy!");
            System.out.print("\nMaxTemp: " + this.space.getMaximumTemperature() + " MinTemp: "+ this.space.getMinimumTemperature());
    }

    private void newTable(){
        System.out.print("\naxisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(),mat" + axisSelection.getModel().getSelectedItem().toString()+ layerSlider.getValue()+ "mat");
        if (drawingTable != null) {
            tablePanel.remove(drawingTable);
        }
        if (tableSelecter.getSelectedIndex() == 0){
            this.drawingTable = new DrawingTable(space.allCells, axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "heat", consoleTextPane);
            this.tablePanel.add(this.drawingTable);
        } else if (tableSelecter.getSelectedIndex() == 2){
            this.drawingTable = new DrawingTable(space.allCells, axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "mat", consoleTextPane);
            this.tablePanel.add(this.drawingTable);
        }
    }

    private void onSave(){
        File saveFile = (new FileExplorer().openSaveDialogAndGetChosenFile());
        if(saveFile==null){
            return;
        }
        this.saveSpaceToFile(saveFile);
    }

    private void saveSpaceToFile(File savefile){
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(savefile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(this.space);
            objectOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void onCancel() {
        dispose();
        System.exit(0);
    }

    private void newAxisSelection(String newAxis){
        newLayerSliderSize(space.getSize(newAxis)-1);
        layerSlider.setValue(0);
        sliderChanged(0);
    }
    private void newLayerSliderSize(int newSize){
        layerSlider.setMaximum(newSize);
        layerSlider.updateUI();
    }

    private void sliderChanged(Integer newValue){
        currentLayerSelectionTextPane.setText(newValue.toString());
        updateTable();
        drawTable();
    }

    private void drawTable(){
        drawingTable.enable();
        drawingTable.show();
    }


    private void updateTable(){
        if (materialSelected()){
            this.drawingTable.setMinAndMaxValues(space.getMinimumTemperature(), space.getMaximumTemperature());
            this.drawingTable.updateTable(axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "mat");
        }else {
            this.drawingTable.updateTable(axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "heat");
        }
        tablePanel.updateUI();
        drawingTable.updateUI();
    }


    private boolean materialSelected() {
        if (this.tableSelecter.getSelectedIndex() == 2) {
            return true;
        } else {
            return false;
        }
    }
}
