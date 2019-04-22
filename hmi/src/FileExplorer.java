import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class FileExplorer extends JDialog {
    private JPanel contentPane;
    private JPanel mainPanel;
    private JFileChooser fileChooser;

    public FileExplorer() {
        setContentPane(contentPane);
        setModal(true);


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

        fileChooser = new JFileChooser();
        mainPanel.add(fileChooser);
   }

   public File openSaveDialogAndGetChosenFile() {

       if (fileChooser.showSaveDialog(mainPanel) == 0) {
           return fileChooser.getSelectedFile();
       } else {
            return null;
       }
   }

    public File openLoadDialogAndGetChosenFile() {

        if (fileChooser.showOpenDialog(mainPanel) == 0) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private void onCancel(){
        dispose();
    }


    public static void main(String[] args) {
        FileExplorer dialog = new FileExplorer();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
