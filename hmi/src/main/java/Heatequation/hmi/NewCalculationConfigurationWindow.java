package Heatequation.hmi;

import javax.swing.*;
import java.awt.event.*;
import Heatequation.Space;

public class NewCalculationConfigurationWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField timeField;
    private JTextField deltaTField;
    private JTextField numberThreadsField;
    public Space space;

    public NewCalculationConfigurationWindow(Space space) {
    setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.space = space;

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

    private void onOK() {
        if (!this.validInput()) {
            openError("No valid input. Please enter numbers in the correct format");
        } else {
            try {
                this.space.initialize(Double.parseDouble(this.timeField.getText()), Double.parseDouble(this.deltaTField.getText()),Integer.parseInt(this.numberThreadsField.getText()));
            } catch (Exception e) {
                e.printStackTrace();
                //TODO: Something on failure
            }
            this.dispose();
        }
    }

    private void openError(String errorMessage){
        PopUpWindow errorWindow = new PopUpWindow();
        errorWindow.setMessage(errorMessage, this);
        errorWindow.pack();
        errorWindow.setVisible(true);
    }

    private boolean validInput(){
        try
        {
            Integer.parseInt(this.numberThreadsField.getText());
            Double.parseDouble(this.deltaTField.getText());
            Double.parseDouble(this.timeField.getText());
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;

        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
