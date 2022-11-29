package Heatequation.hmi;

import Heatequation.Cells.Material;
import Heatequation.SaveFile;
import Heatequation.Space;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import java.awt.event.*;
import java.io.*;
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
    private JButton solidGenerateButton;
    private DrawingTable drawingTable;
    private JTextField textField1X;
    private JTextField textField1Z;
    private JTextField textField1Y;
    private JComboBox solidMaterialSelection;
    private JCheckBox fixedTemperatureCheckBox;
    private JTextField textField2X;
    private JTextField textField2Z;
    private JTextField textField2Y;
    private JPanel consolePanel;
    private JPanel tablePanel;
    private JTextField solidHeatCapacityTextField;
    private JPanel layerSelectionPanel;
    private JTextPane currentLayerSelectionTextPane;
    private JComboBox fluidMaterialSelection;
    private JTextField viskosityTextField;
    private JPanel consolePane;
    private JTextPane consoleTextField;
    private JTextField heatFlowText;
    private JCheckBox heatFlowCheckbox;
    private JPanel heatCapacity;
    private JPanel heatConductivity;
    private JTextField fluidHeatCapacityTextField;
    private JTextField fluidHeatConductivityTextField;
    private JTextField nusseltTextField;
    private JTextField solidHeatConductivity;
    private JTextField startingTemperatureField;
    private JCheckBox startingTemperatureCheckbox;
    private JTextField fixedTemperatureField;
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

    private void praeInit(){
        setContentPane(contentPane);
        setModal(true);
        getMaterialsFromJsonFile();
        selectedMaterial = solidMaterials.get(0);
        createSolidMaterialSelection();
        createFluidMaterialSelection();

    }

    public ConstructionWindow(Space space){
        praeInit();
        this.space=space;
        if(space != null){
            int numberCells = space.sizeX * space.sizeY * space.sizeZ;
            this.showTextOnConsole("space with " + numberCells+ " cells loaded.   " + space.sizeX + " x " + space.sizeY + " x "+ space.sizeZ);
        }
        init(space);
    }



    public ConstructionWindow(int sizex, int sizey, int sizez, double startTemperature) {
        praeInit();
        this.space = new Space(sizex, sizey, sizez, startTemperature, solidMaterials.get(0), 4);
        if(this.space != null){
            int numberCells = space.sizeX * space.sizeY * space.sizeZ;
            this.showTextOnConsole("new space with " + numberCells+ " cells created.   " + space.sizeX + " x " + space.sizeY + " x "+ space.sizeZ);
        }
        init(space);
    }

    private void init(Space space){
        this.generationSelecter.setSelectedIndex(0);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        solidGenerateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {onGenerate();}
        });

        axisSelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                newAxisSelection(axisSelection.getModel().getSelectedItem().toString());
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOkay();
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

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSave();
            }
        });

        generationSelecter.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                generationSelecterChanged();
                newTable();
                drawTable();


            }
        });
    }

    private void generationSelecterChanged(){
        if (generationSelecter.getSelectedIndex() == 0){
            this.solidMaterialSelection.setSelectedIndex(0);
            this.selectedMaterial=solidMaterials.get(0);
            changeSolidMaterials();
            }
        if (generationSelecter.getSelectedIndex() == 1){
            this.fluidMaterialSelection.setSelectedIndex(0);
            this.selectedMaterial=fluidMaterials.get(0);
            changeFluidMaterials();
        }
    }

    private void onOkay(){
        NewCalculationConfigurationWindow calcWindow = new NewCalculationConfigurationWindow(this.space);
        calcWindow.pack();
        calcWindow.show();
        //TODO: Create calculation window
        final CalculationProgressWindow progressWindow= new CalculationProgressWindow(this.space);
        progressWindow.pack();

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Boolean res = progressWindow.calculate();
                progressWindow.dispose();
                return res;
            }

        };
        /*
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            public Boolean doInBackground() throws InterruptedException{
            return progressWindow.calculate();
        }
        @Override
        protected void done() {
            calcWindow.dispose();
        }
    };
*/
        ((SwingWorker<Boolean, Void>) worker).execute();
        progressWindow.show();


        this.setVisible(false);
        this.getRootPane().setVisible(false);
        ShowSpaceDialog nextWindow = new ShowSpaceDialog(space);
        nextWindow.pack();
        nextWindow.show();

    }

    private void showTextOnConsole(String text){
        this.consoleTextField.setText(text);
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
        System.exit(0);
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private boolean validGernerateInput(){
        return true;
    }

    private boolean boundariesSelected(){
        if (this.generationSelecter.getSelectedIndex() == 2){
            return true;
        }else{
            return false;
        }

    }
    private void onGenerate(){
        try {
            if (boundariesSelected()) {
                if (validGernerateInput()) {
                    Double heatFlow = -1.;
                    Double fixedTemperature = -1.;
                    Double startingTemperature = -1.;


                    if (this.heatFlowCheckbox.isSelected()) {
                        heatFlow = Double.valueOf(this.heatFlowText.getText());
                    }
                    if (this.fixedTemperatureCheckBox.isSelected()) {
                        fixedTemperature = Double.valueOf(this.fixedTemperatureField.getText());
                    }
                    if (this.startingTemperatureCheckbox.isSelected()) {
                        startingTemperature = Double.valueOf(this.startingTemperatureField.getText());
                    }

                    this.showTextOnConsole(space.setBoundariesForCube(Integer.valueOf(textField1X.getText()),
                            Integer.valueOf(textField1Y.getText()),
                            Integer.valueOf(textField1Z.getText()),
                            Integer.valueOf(textField2X.getText()),
                            Integer.valueOf(textField2Y.getText()),
                            Integer.valueOf(textField2Z.getText()),
                            fixedTemperature, heatFlow, startingTemperature));
                   }
                } else {
                    if (validGernerateInput()) {
                        this.showTextOnConsole(space.createCube(Integer.valueOf(textField1X.getText()),
                                Integer.valueOf(textField1Y.getText()),
                                Integer.valueOf(textField1Z.getText()),
                                Integer.valueOf(textField2X.getText()),
                                Integer.valueOf(textField2Y.getText()),
                                Integer.valueOf(textField2Z.getText()),
                                selectedMaterial));
                    }
                }
            updateTable();
            drawTable();
        }catch (NumberFormatException e){
            this.showTextOnConsole("Wrong number format - please check input");
        }
    }

    private void newTable(){
        System.out.print("\naxisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(),mat" + axisSelection.getModel().getSelectedItem().toString()+ layerSlider.getValue()+ "mat");
        if (drawingTable != null) {
            tablePanel.remove(drawingTable);
        }
        if (generationSelecter.getSelectedIndex() == 2){
            this.drawingTable = new ColorTable(space, axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "heat", consoleTextField);
            this.tablePanel.add(this.drawingTable);
        } else {
            this.drawingTable = new ColorTable(space, axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "mat", consoleTextField);
            this.tablePanel.add(this.drawingTable);


        }
    }

    private void updateTable(){

        if (boundariesSelected()){
            this.drawingTable.setMinAndMaxValues(space.getMinimumTemperature(), space.getMaximumTemperature());
            this.drawingTable.updateTable(axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "heat");
        }else {
            this.drawingTable.updateTable(axisSelection.getModel().getSelectedItem().toString(), layerSlider.getValue(), "mat");
        }
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
                changeSolidMaterials();
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
                changeFluidMaterials();
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

    private void changeSolidMaterials(){
        this.solidHeatCapacityTextField.setText(String.valueOf(selectedMaterial.getHeatCapacity()));
        this.solidHeatConductivity.setText(String.valueOf(selectedMaterial.getheatConductivity()));
    }

    private void changeFluidMaterials(){
        this.fluidHeatCapacityTextField.setText(String.valueOf(selectedMaterial.getHeatCapacity()));
        this.fluidHeatConductivityTextField.setText(String.valueOf(selectedMaterial.getheatConductivity()));
        this.viskosityTextField.setText(String.valueOf(selectedMaterial.getViskosity()));
        this.nusseltTextField.setText(String.valueOf(selectedMaterial.getNusselt()));
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

        } catch (IOException e) {
            e.printStackTrace();
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
            SaveFile fileObject = this.space.createSaveFile();
            objectOut.writeObject(fileObject);
            objectOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
