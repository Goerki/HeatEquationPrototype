import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;


public class ConstructionWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane generationSelecter;
    private JComboBox comboBox1;
    private JSlider slider1;
    private JButton saveButton;
    private JButton generateButton;
    private JTable drawingTable;
    private JTextField textField1X;
    private JTextField textField1Z;
    private JTextField textField1Y;
    private JComboBox materialSelection;
    private JCheckBox fixedTemperatureCheckBox;
    private JTextField textField4;
    private JTextField textFeild2X;
    private JTextField textField2Z;
    private JTextField textField2Y;
    private JPanel consolePanel;
    private JPanel tablePanel;

    private void newArea(){

        getRootPane().updateUI();

    }

    private static Object[] columnName = {"Yes", "No"};
    private static Object[][] data = {
            {"Y", "N"},
            {"N", "Y"},
            {"Y", "N"}
    };

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


        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {onFail();}
        });
        double[][] abc = new double[2][2];
        abc[0][0] =1;
        abc[0][1] =2;
        abc[1][0] =3;
        abc[1][1] =4;

        this.drawTable(abc);

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

    private void drawCell(int row, int collumn){
        ColorModel cm = new ColorModel(254) {
            @Override
            public int getRed(int pixel) {
                return 10;
            }

            @Override
            public int getGreen(int pixel) {
                return 0;
            }

            @Override
            public int getBlue(int pixel) {
                return 0;
            }

            @Override
            public int getAlpha(int pixel) {
                return 0;
            }
        };



    }

    private void drawTable(double[][] values){
        this.setTitle("Tittie");



        drawingTable = new JTable(data,columnName);

        drawingTable.getColumnModel().getColumn(0).setCellRenderer(new CustomRenderer());
        drawingTable.getColumnModel().getColumn(1).setCellRenderer(new CustomRenderer());
        this.tablePanel.add(new JScrollPane(drawingTable));


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
}
