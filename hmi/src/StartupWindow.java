import javax.swing.*;
import java.awt.event.*;

public class StartupWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JPanel startupPanel;
    private JTextPane createANewCalculationTextPane;
    private JButton newAreaButton;


    private void createNewAreaSizeWindow(){

        NewAreaSizeWindow nextWindow = new NewAreaSizeWindow();
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
