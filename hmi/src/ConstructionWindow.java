import Cells.Material;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConstructionWindow extends JDialog {

    private List<Material> solidMaterials;
    private List<Material> fluidMaterials;
    private Material selectedMaterial;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane generationSelecter;
    private JComboBox axisSelection;
    private JSlider layerSlider;
    private JButton saveButton;
    private JButton generateButton;
    private DrawingTable drawingTable;
    private JTextField textField1X;
    private JTextField textField1Z;
    private JTextField textField1Y;
    private JComboBox solidMaterialSelection;
    private JCheckBox fixedTemperatureCheckBox;
    private JTextField textField4;
    private JTextField textField2X;
    private JTextField textField2Z;
    private JTextField textField2Y;
    private JPanel consolePanel;
    private JPanel tablePanel;
    private JTextField alphaTextField;
    private JPanel layerSelectionPanel;
    private JTextPane currentLayerSelectionTextPane;
    private JComboBox fluidMaterialSelection;
    private JTextField viskosityTextField;
    public Space space;

    private void newArea(){

        getRootPane().updateUI();

    }

    private void onFail(){
this.openError("das ist ein Test String! Klick dich ins Knie");
getRootPane().disable();
    }

    private void openError(String errorMessage){
        PopUpWindow errorWindow = new PopUpWindow();
        errorWindow.setMessage(errorMessage, this);
        errorWindow.pack();
        errorWindow.setVisible(true);
    }

    public ConstructionWindow(int sizex, int sizey, int sizez) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        getMaterialsFromJsonFile();
        selectedMaterial = solidMaterials.get(0);
        createSolidMaterialSelection();
        createFluidMaterialSelection();
        this.space=new Space(sizex,sizey,sizez,100,selectedMaterial,4);




        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {onGenerate();}
        });

        axisSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                newAxisSelection(axisSelection.getModel().getSelectedItem().toString());
            }
        });



        newTable();
        drawTable();
        newAxisSelection("x");
        sliderChanged(0);

        // call onSliderChanged when the layer Slider is changed
        layerSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderChanged(layerSlider.getValue());
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


    private void drawTable(){
        drawingTable.enable();
        drawingTable.show();
    }

    private void sliderChanged(Integer newValue){
        currentLayerSelectionTextPane.setText(newValue.toString());
        updateTable();
        drawTable();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ConstructionWindow dialog = new ConstructionWindow(10, 10, 10);
        dialog.pack();

        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private boolean validGernerateInput(){
        return true;
    }
    private void onGenerate(){
        if (validGernerateInput()){
            space.createCube(Integer.valueOf(textField1X.getText()),
                    Integer.valueOf(textField1Y.getText()),
                    Integer.valueOf(textField1Z.getText()),
                    Integer.valueOf(textField2X.getText()),
                    Integer.valueOf(textField2Y.getText()),
                    Integer.valueOf(textField2Z.getText()),
                    selectedMaterial);
        }

               updateTable();
        drawTable();

    }

    private void newTable(){
        System.out.print("\naxisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(),mat" + axisSelection.getModel().getSelectedItem().toString()+ layerSlider.getValue()+ "mat");
        if (drawingTable != null) {
            tablePanel.remove(drawingTable);
        }
        this.drawingTable = new DrawingTable(space.allCells, axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(),"mat");
        this.tablePanel.add(this.drawingTable);
    }

    private void updateTable(){
        this.drawingTable.updateTable(axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(),"mat");
        tablePanel.updateUI();
        drawingTable.updateUI();
    }

    private Material getMaterialByName(String name){
        for(Material tempMaterial :solidMaterials){
            if (tempMaterial.name.contains(name)){
                return tempMaterial;
            }
        }
        for(Material tempMaterial :fluidMaterials){
            if (tempMaterial.name.contains(name)){
                return tempMaterial;
            }
        }
        return null;
    }


    private void createSolidMaterialSelection(){
        this.solidMaterialSelection.setModel(new ComboBoxModel() {
            @Override
            public void setSelectedItem(Object anItem) {
                selectedMaterial = getMaterialByName(anItem.toString());
                changeAlphaValue(selectedMaterial.alpha);
            }

            @Override
            public Object getSelectedItem() {
                return selectedMaterial.name;

            }

            @Override
            public int getSize() {
                return solidMaterials.size();
            }

            @Override
            public Object getElementAt(int index) {
                return solidMaterials.get(index).name;
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        });
    }


    private void createFluidMaterialSelection(){
        this.fluidMaterialSelection.setModel(new ComboBoxModel() {
            @Override
            public void setSelectedItem(Object anItem) {
                selectedMaterial = getMaterialByName(anItem.toString());
                changeViskosityValue(selectedMaterial.viskosity);
            }

            @Override
            public Object getSelectedItem() {
                return selectedMaterial.name;
            }

            @Override
            public int getSize() {
                return fluidMaterials.size();
            }

            @Override
            public Object getElementAt(int index) {
                return fluidMaterials.get(index).name;
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        });
    }

    private void changeAlphaValue(Double newValue){
        this.alphaTextField.setText(newValue.toString());
        this.alphaTextField.updateUI();
    }

    private void changeViskosityValue(Double newValue){
        this.viskosityTextField.setText(newValue.toString());
        this.viskosityTextField.updateUI();
    }

    private void getMaterialsFromJsonFile(){
        try {
     /*       String fileData = new String(Files.readAllBytes(Paths
                    .get("C:\\Users\\thoni\\IdeaProjects\\HeatEquationPrototype\\hmi\\jsonFiles\\materials.json")));
            System.out.print("filedata: " + fileData);
            */
            String filePath = new File("").getAbsolutePath();
            String fileData = new String(Files.readAllBytes(Paths
                    .get("hmi\\jsonFiles\\materials.json")));

            Gson gson = new Gson();
            JsonObject allObjects = gson.fromJson(fileData, JsonObject.class);
            JsonArray solidMaterials = allObjects.get("SolidMaterials").getAsJsonArray();
            JsonArray fluidMaterials = allObjects.get("FluidMaterials").getAsJsonArray();
            this.solidMaterials = new ArrayList<>();
            this.fluidMaterials = new ArrayList<>();

            for (int solidIncrement = 0; solidIncrement < solidMaterials.size(); solidIncrement++) {
                this.solidMaterials.add(new Material(solidMaterials.get(solidIncrement).getAsJsonObject()));
               }
            for (int fluidIncrement = 0; fluidIncrement < fluidMaterials.size(); fluidIncrement++) {
                this.fluidMaterials.add(new Material(fluidMaterials.get(fluidIncrement).getAsJsonObject()));
            }

            System.out.print("whatever...");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
