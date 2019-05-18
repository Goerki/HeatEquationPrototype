package Heatequation.hmi;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PopUpWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane headText;
    private JTextPane bodyText;
    private JDialog parentWindow;

    public PopUpWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        // add your code here
        this.parentWindow.enable();
        dispose();
    }

    public void setMessage(String errorMessage, JDialog main){
        bodyText.setText(errorMessage);
        bodyText.updateUI();
        this.parentWindow = main;
    }

    public static void main(String[] args) {
        PopUpWindow dialog = new PopUpWindow();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
