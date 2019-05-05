package Heatequation.hmi;

import javax.swing.*;
import java.awt.event.*;

public class NewAreaSizeWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField sizeXField;
    private JTextField sizeYField;
    private JTextField sizeZField;
    private JTextField startingTemperatureField;
    private JTextField cellLengthField;

    public NewAreaSizeWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
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

    private boolean validInput() {
        try {

            if (Integer.valueOf(sizeXField.getText()) > 0) {
                if (Integer.valueOf(sizeYField.getText()) > 0) {
                    if (Integer.valueOf(sizeZField.getText()) > 0) {
                        if (Integer.valueOf(startingTemperatureField.getText()) > 0) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

    private void onFail(){
        this.openError("Enter valid values for the sizes.");
        getRootPane().disable();
    }

    private void openError(String errorMessage){
        PopUpWindow errorWindow = new PopUpWindow();
        errorWindow.setMessage(errorMessage, this);
        errorWindow.pack();
        errorWindow.setVisible(true);
    }

    private void onOK() {
        // add your code here
        if (this.validInput()) {
            dispose();
            ConstructionWindow nextWindow = new ConstructionWindow(Integer.valueOf(sizeXField.getText()),
                    Integer.valueOf(sizeYField.getText()), Integer.valueOf(sizeZField.getText()), Integer.valueOf(startingTemperatureField.getText()));
            nextWindow.pack();
            nextWindow.setVisible(true);

        } else {

            onFail();
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        StartupWindow previousWindow = new StartupWindow();
        previousWindow.pack();
        previousWindow.setVisible(true);

    }

    public static void main(String[] args) {
        NewAreaSizeWindow dialog = new NewAreaSizeWindow();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
