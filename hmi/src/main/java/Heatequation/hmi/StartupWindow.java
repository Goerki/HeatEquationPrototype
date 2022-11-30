package Heatequation.hmi;

import Heatequation.SaveFile;
import Heatequation.Space;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class StartupWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JPanel startupPanel;
    private JTextPane createANewCalculationTextPane;
    private JButton newAreaButton;
    private JButton openAreaButton;
    private double baseAmplificationFactor=0.1;


    private void createNewAreaSizeWindow(){

        NewAreaSizeWindow nextWindow = new NewAreaSizeWindow(this.baseAmplificationFactor);
        nextWindow.pack();
        this.setVisible(false);
        this.getRootPane().setVisible(false);
        nextWindow.setVisible(true);

    }
    public StartupWindow() {

        setContentPane(contentPane);
        setModal(true);


        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        newAreaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {createNewAreaSizeWindow();}
        });

        openAreaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOpenExistingFile();
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

    private void onOpenExistingFile(){
        File loadFile = new FileExplorer().openLoadDialogAndGetChosenFile();
        if (loadFile==null){
            return;
        }
        else{
            Space oldSpace = this.getSpaceFromFile(loadFile);
            if (oldSpace==null){
                return;
            } else {
                openNewConstructionWindow(oldSpace);
            }
        }
    }

    private void openNewConstructionWindow(Space space){

        ConstructionWindow nextWindow = new ConstructionWindow(space);
        nextWindow.pack();
        this.setVisible(false);
        this.getRootPane().setVisible(false);
        nextWindow.setVisible(true);
    }

    private Space getSpaceFromFile(File loadFile){

        try {
            FileInputStream fileOut = new FileInputStream(loadFile);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileOut);
        SaveFile saveFile = (SaveFile) objectInputStream.readObject();
        return new Space(saveFile, this.baseAmplificationFactor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        StartupWindow dialog = new StartupWindow();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
